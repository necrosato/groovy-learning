import doubly_linked_list.DoublyLinkedList;
import doubly_linked_list.DLLNode;


class LoadingCacheTest {
  static void main(String[] args) {
    TestLoadingCacheWorks();
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

  static int compute(int i) {
    println("Computing ${i}");
    return i;
  }

}

/**
Base eviction policy class. This abstract class specifies a method to take a key and load a value.
The actual implementation of the load method is defined by subclasses
*/
abstract class EvictionPolicy<T,U> {
  protected Map<T,Closure> compute_map = [:];
  protected Map<T,List> params_map = [:];
  
  /**
  Register a key to load a value for.
  The passed closure and parameter list will be used to compute the value when the key is loaded.
  */
  public void set(T key, Closure c, List params) {
    compute_map[key] = c;
    params_map[key] = params;
  }

  /**
  Method to load a value for key. Value is only computed if not already in memory
  */
  abstract public U load(T key);
}

/**
A LRU eviction policy. Only the n most recently used keys will stay loaded in the cache. 
Uses a DoublyLinkedList as the value storage data structure to back the cache.
*/
class LeastRecentlyUsed<T,U> extends EvictionPolicy<T,U> {
  private int size;
  private Map<T,DLLNode<Tuple<T,U>>> node_map = [:];
  private DoublyLinkedList<Tuple<T,U>> dll = new DoublyLinkedList<Tuple<T,U>>();

  /**
  Constructor
  params:
    size: the max number of values to keep loaded in memory
  */
  LeastRecentlyUsed(int size) {
    this.size = size;
  }

  public U load(T key) {
    if (!this.node_map.containsKey(key)) {
      dll.Insert(new Tuple<T,U>(key, compute_map[key](*params_map[key])));
      node_map[key] = dll.Tail();
    }
    dll.MoveToHead(node_map[key]);
    while (dll.Size() > size) {
      node_map.remove(dll.Tail().GetVal()[0]);
      dll.Delete(dll.Tail());
    }
    return node_map[key].GetVal()[1];
  }
}

/**
A loading cache that uses an EvictionPolicy to determine how data is stored, loaded, and retained.
This is really just a wrapper around the EvictionPolicy which does all the heavy lifting.
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

