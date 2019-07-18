import doubly_linked_list.DoublyLinkedList;
import doubly_linked_list.DLLNode;


class LoadingCacheTest {
  static void main(String[] args) {
    TestLoadingCacheWorks();
    TestLoadingCacheWorksConcurrent();
  }

  static void TestLoadingCacheWorks() {
    def cache = new LoadingCache<String,Integer>(new LeastRecentlyUsed<String,Integer>(3));
    cache.set("test1", this.&compute, [1]);
    cache.set("test2", this.&compute, [2]);
    cache.set("test3", this.&compute, [3]);
    cache.set("test4", this.&compute, [4]);

    println(cache.get("test1"));
    println(cache.get("test1"));
    println(cache.get("test2"));
    println(cache.get("test2"));
    println(cache.get("test3"));
    println(cache.get("test3"));
    println(cache.get("test4"));
    println(cache.get("test4"));
    println(cache.get("test2"));
    println(cache.get("test1"));
    println(cache.get("test4"));
  }

  static void TestLoadingCacheWorksConcurrent() {
    def cache = new LoadingCache<Integer,Integer>(new LeastRecentlyUsed<Integer,Integer>(2));
    for (int i = 0; i < 4; i++) {
      cache.set(i, this.&compute_delayed, [i, 1000]);
    }
    for (int i = 0; i < 4; i++) {
      // Cannot use i because the value is not captured until the function runs, as opposed to when the thread is created.
      // This way we force cache.get(ic) to get the value of ic when the loop iterates, not the value of i when the thread executes (which may be after loop termination).
      int ic = i;
      def t = new Thread() {
        @Override
        public void run() {
          println(cache.get(ic));
        }
      }
      t.start();
    }
  }

  static int compute(int i) {
    println("Computing ${i}");
    return i;
  }

  static int compute_delayed(int i, int ms) {
    println("Computing ${i} with ${ms} millisecond delay");
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
   * abstract method to start the computation for a load if a computation is not already started and the value is not loaded.
   * This should be thread safe.
   */
  abstract protected void compute_load(T key);

  /**
   * Method to load a value for key. Value is only computed if not already in memory.
   * This should be thread safe.
   */
  abstract public U load(T key);
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

  /**
   * protected method to remove cached objects from the tail of the dll until the dll is <= size.
   */
  protected synchronized void unload() {
    while (dll.Size() > size) {
      node_map.remove(dll.Tail().GetVal()[0]);
      dll.Delete(dll.Tail());
    }
  }

  /**
   * protected method to start the computation for a load in a new thread.
   */
  protected void compute_load(T key) {
    if (!this.node_map.containsKey(key)) {
      def val = compute_map[key](*params_map[key]);
      dll.InsertHead(new Tuple<T,U>(key, val));
      node_map[key] = dll.Head();
      unload();
      assert(node_map[key] != null);
      assert(dll.Size() <= size);
    }
  }

  /**
   * Load a value for a key.
   * Concurrent loads consider the most recently computed value as the most recently used.
   * Note that this means that even though load(key1) was called before load(key2),
   * the value for key1 may be the most recently used if its computation finishes after the computation for key2.
   */
  public synchronized U load(T key) {
    compute_load(key);
    assert(node_map.containsKey(key));
    assert(node_map[key] != null);
    assert(node_map.size() <= size);
    return node_map[key].GetVal()[1];
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
}

