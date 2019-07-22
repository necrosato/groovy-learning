package loading_cache;
import doubly_linked_list.DoublyLinkedList;
import doubly_linked_list.DLLNode;

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
  private Map<T,DLLNode<Tuple>> node_map = Collections.synchronizedMap([:]);
  private DoublyLinkedList<Tuple> dll = new DoublyLinkedList<Tuple>();

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
        node_map[key] = dll.InsertHead(new Tuple(key, val));
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

