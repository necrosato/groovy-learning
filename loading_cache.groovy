import DoublyLinkedList

class LoadingCacheTest {
  static void main(String[] args) {
    TestLoadingCacheWorks();
  }

  static def TestLoadingCacheWorks() {
    def cache = new LoadingCache<String,Integer>(new LeastRecentlyUsed<String,Integer>(3));
    cache.set("test", this.&compute);
    println(cache.get("test"));
    println(cache.get("test"));
  }

  static Integer compute() {
    println("Computing 100");
    return 100;
  }

}

abstract class EvictionPolicy<T,U> {
  protected Map<T,Closure> compute_map = [:];
  
  public void set(T key, Closure c) {
    compute_map[key] = f;
  }

  abstract public U load(T key);
}

class LeastRecentlyUsed<T,U> extends EvictionPolicy<T,U> {
  private int size;
  protected Map<T,DLLNode<Tuple<T,U>>> node_map = [:];
  private DoublyLinkedList<Tuple<T,U>> dll = new DoublyLinkedList<Tuple<T,U>>();

  LeastRecentlyUsed(int size) {
    this.size = size;
  }

  public U load(T key) {
    if (!this.node_map.containsKey(key)) {
      dll.Insert(new Tuple<T,U>(key, compute_map[key]()));
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

public class LoadingCache<T,U> {
  private EvictionPolicy<T,U> ep;

  LoadingCache(EvictionPolicy ep) {
    this.ep = ep;
  }

  public void set(T key, Closure f) {
    ep.set(key, f);
  }

  public U get(T key) {
    return ep.load(key);
  }
}

