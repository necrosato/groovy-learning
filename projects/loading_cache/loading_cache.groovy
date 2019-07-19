import doubly_linked_list.DoublyLinkedList;
import doubly_linked_list.DLLNode;
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LoadingCacheTest {
  static void main(String[] args) {
    TestLoadingCacheWorks();
    TestLoadingCacheWorksConcurrent();
  }

  static void TestLoadingCacheWorks() {
    def cache_size = 3;
    def cache = new LoadingCache<Integer,Integer>(new LeastRecentlyUsed<String,Integer>(cache_size));
    def compute_counts = [:];
    cache.set(1, this.&compute, [1, compute_counts]);
    cache.set(2, this.&compute, [2, compute_counts]);
    cache.set(3, this.&compute, [3, compute_counts]);
    cache.set(4, this.&compute, [4, compute_counts]);

    assert(cache.get(1) == 1);
    assert(cache.get(1) == 1);
    assert(cache.get(2) == 2);
    assert(cache.get(2) == 2);
    assert(cache.get(3) == 3);
    assert(cache.get(3) == 3);
    assert(cache.get(4) == 4);
    assert(cache.get(4) == 4);
    assert(cache.get(2) == 2);
    assert(cache.get(1) == 1);
    assert(cache.get(4) == 4);

    assert(compute_counts[1] == 2)
    assert(compute_counts[2] == 1)
    assert(compute_counts[3] == 1)
    assert(compute_counts[4] == 1)

    assert(cache.CachedSize() <= cache_size);
  }

  static void TestLoadingCacheWorksConcurrent() {
    def cache_size = 2;
    def cache = new LoadingCache<Integer,Integer>(new LeastRecentlyUsed<Integer,Integer>(cache_size));
    def es = Executors.newCachedThreadPool();
    def compute_counts = [:];
    int nkeys = 8;
    for (int i = 0; i < nkeys; i++) {
      cache.set(i, this.&compute_delayed, [i, 100, compute_counts]);
    }
    def result_map = [:];
    for (int i = 0; i < nkeys; i++) {
      result_map[i] = 0;
    }
    int niters = 1000;
    for (int j = 0; j < niters; j++) {
      for (int i = 0; i < nkeys; i++) {
        // Cannot use i because the value is not captured until the function runs, as opposed to when the thread is created.
        // This way we force cache.get(ic) to get the value of ic when the loop iterates, not the value of i when the thread executes (which may be after loop termination).
        int ic = i;
        es.execute(new Runnable() {
          @Override
          public void run() {
            def val = cache.get(ic);
            synchronized(result_map) {
              result_map[val]++;
            }
          }
        });
      }
    }
    es.shutdown();
    assert(es.awaitTermination(10, TimeUnit.SECONDS));
    for (int i = 0; i < nkeys; i++) {
      assert(result_map[i] == niters);
    }
    assert(cache.CachedSize() <= cache_size);
  }

  static int compute(int i, Map<Integer,Integer> compute_counts) {
    synchronized(compute_counts) {
      if (!compute_counts.containsKey(i)) {
        compute_counts[i] = 1;
      } else {
        compute_counts[i]++;
      }
    }
    return i;
  }

  static int compute_delayed(int i, int ms, Map<Integer,Integer> compute_counts) {
    synchronized(compute_counts) {
      if (!compute_counts.containsKey(i)) {
        compute_counts[i] = 1;
      } else {
        compute_counts[i]++;
      }
    }
    Thread.sleep(ms);
    return i;
  }

}

/**
 * Base eviction policy class. This abstract class specifies a method to take a key and load a value.
 * All children of this class should be thread safe.
 */
abstract class EvictionPolicy<T,U> {
  protected Map<T,Closure> compute_map = [:];
  protected Map<T,List> params_map = [:];
  
  /**
   * Register a key to load a value for.
   * The passed closure and parameter list will be used to compute the value when the key is loaded.
   */
  public synchronized void set(T key, Closure c, List params) {
    compute_map[key] = c;
    params_map[key] = params;
  }

  /**
   * abstract method to remove cached objects according to eviction policy.
   * This should be thread safe.
   */
  abstract protected void unload();
  
  /**
   * protected method to compute and return the value for key.
   * This should be thread safe.
   */
  abstract protected T compute_load(T key);

  /**
   * Method to load a value for key. Value is only computed if not already in memory.
   * This should be thread safe.
   */
  abstract public U load(T key);

  /**
   * Return the number of currently cached values.
   */
  abstract public int CachedSize();
}

/**
 * A LRU eviction policy. Only the n most recently used keys will stay loaded in the cache. 
 * Uses a DoublyLinkedList as the value storage data structure to back the cache.
 */
class LeastRecentlyUsed<T,U> extends EvictionPolicy<T,U> {
  private int size;
  private Map<T,DLLNode<Tuple<T,U>>> node_map = Collections.synchronizedMap([:]);
  private DoublyLinkedList<Tuple<T,U>> dll = new DoublyLinkedList<Tuple<T,U>>();

  /**
   * Constructor
   * params:
   *   size: the max number of values to keep loaded in memory
   */
  LeastRecentlyUsed(int size) {
    this.size = size;
  }

  protected void unload() {
    // Lock the dll so that compute_load cannot insert while this is deleting
    synchronized (dll) {
      while (dll.Size() > size) {
        def key = dll.Tail().GetVal()[0];
        // Wait for computations to finish so that compute_load dose not attempt to use null values
        synchronized (compute_map[key]) {
          node_map.remove(key);
          dll.Delete(dll.Tail());
        }
      }
    }
  }

  protected T compute_load(T key) {
    T val;
    assert(compute_map.containsKey(key));
    synchronized (compute_map[key]) {
      if (!this.node_map.containsKey(key)) {
        val = compute_map[key](*params_map[key]);
        node_map[key] = dll.InsertHead(new Tuple<T,U>(key, val));
        assert(node_map[key] != null);
      } else {
        val = node_map[key].GetVal()[1];
      }
    }
    return val;
  }

  /**
   * Load a value for a key.
   * Concurrent loads consider the most recently computed value as the most recently used.
   * Note that this means that even though load(key1) was called before load(key2),
   * the value for key1 may be the most recently used if its computation finishes after the computation for key2.
   */
  public U load(T key) {
    def val = compute_load(key);
    unload();
    return val;
  }

  public int CachedSize() {
    return dll.Size();
  }
}

/**
 * A loading cache that uses an EvictionPolicy to determine how data is stored, loaded, and retained.
 * This is really just a wrapper around the EvictionPolicy which does all the heavy lifting.
 */
public class LoadingCache<T,U> {
  private EvictionPolicy<T,U> ep;

  LoadingCache(EvictionPolicy ep) {
    this.ep = ep;
  }

  public void set(T key, Closure f, List params) {
    ep.set(key, f, params);
  }

  public U get(T key) {
    return ep.load(key);
  }

  public int CachedSize() {
    return ep.CachedSize();
  }
}

