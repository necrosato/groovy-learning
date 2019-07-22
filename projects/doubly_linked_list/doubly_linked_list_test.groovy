package doubly_linked_list;
import doubly_linked_list.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for DoublyLinkedList
 */
class DLLTest {
  public static void main(String[] args) {
    TestInsert();
    TestDelete();
    TestDeleteHead();
    TestDeleteTail();
    TestDeleteHeadTail();
    TestMoveToHead();
    TestMoveToHeadSingle();
    TestConcurrentDLLCreate();
    TestConcurrentDLLInsert();
    TestConcurrentDLLDelete();
  }

  public static void TestInsert() {
    def dll = new DoublyLinkedList<String>();    
    dll.Insert("hello");
    dll.Insert("world");
    dll.Insert("!");
    assert(dll.Size() == 3);
  }

  public static void TestDelete() {
    def dll = new DoublyLinkedList<String>();    
    dll.Insert("hello");
    dll.Insert("world");
    dll.Insert("!");
    def node = dll.Head();
    def next = node.GetNext();
    def size = dll.Size();
    for (int i = 0; i < size; i++) {
      dll.Delete(node);
      if (next != null) {
        node = next;
        next = node.GetNext();
      }
    }
    assert(dll.Size() == 0);
  }

  public static void TestDeleteHead() {
    def dll = new DoublyLinkedList<String>();    
    dll.Insert("hello");
    dll.Insert("world");
    dll.Delete(dll.Head());
    assert (dll.Head().GetVal() == "world");
    assert (dll.Head() == dll.Tail());
    assert(dll.Size() == 1);
  }

  public static void TestDeleteTail() {
    def dll = new DoublyLinkedList<String>();    
    dll.Insert("hello");
    dll.Insert("world");
    dll.Delete(dll.Tail());
    assert (dll.Tail().GetVal() == "hello");
    assert (dll.Head() == dll.Tail());
    assert(dll.Size() == 1);
  }

  public static void TestDeleteHeadTail() {
    def dll = new DoublyLinkedList<String>();    
    dll.Insert("hello");
    assert(dll.Head() != null);
    assert(dll.Tail() != null);
    dll.Delete(dll.Head());
    assert (dll.Head() == null);
    assert (dll.Tail() == null);
    assert(dll.Size() == 0);
    dll.Insert("hello");
    assert(dll.Head() != null);
    assert(dll.Tail() != null);
    dll.Delete(dll.Tail());
    assert (dll.Head() == null);
    assert (dll.Tail() == null);
    assert(dll.Size() == 0);
  }

  public static void TestMoveToHead() {
    def dll = new DoublyLinkedList<String>();
    dll.Insert("hello");
    dll.Insert("world");
    dll.Insert("!");
    dll.MoveToHead(dll.Tail());
    assert(dll.Head().GetVal() == "!");
    assert(dll.Head().GetNext().GetVal() == "hello");
    assert(dll.Tail().GetVal() == "world");
  }

  public static void TestMoveToHeadSingle() {
    def dll = new DoublyLinkedList<String>();
    dll.Insert("hello");
    dll.MoveToHead(dll.Tail());
    assert(dll.Head().GetVal() == "hello");
    assert(dll.Tail().GetVal() == "hello");
  }

  public static void TestConcurrentDLLCreate() {
    def es = Executors.newCachedThreadPool();
    def id_set = Collections.synchronizedSet(new HashSet<Integer>());
    for (int i = 0; i < 8; i++) {
      es.execute(new Runnable() { @Override
                                  public void run() {
                                    def dll = new DoublyLinkedList<Integer>();
                                    assert(id_set.add(dll.GetID()));
                                  }});
    }
    es.shutdown();
    assert(es.awaitTermination(10, TimeUnit.SECONDS));
  }

  public static void TestConcurrentDLLInsert() {
    def es = Executors.newCachedThreadPool();
    def val_set = new HashSet<Integer>();
    def c = { node -> assert(val_set.add(node.GetVal())); };
    def dll = new DoublyLinkedList<Integer>();
    for (int i = 0; i < 8; i++) {
      es.execute(new Runnable() { @Override
                                  public void run() {
                                    dll.Insert(Thread.currentThread().getId());
                                  }});
    }
    es.shutdown();
    assert(es.awaitTermination(10, TimeUnit.SECONDS));
    assert(dll.Size() == 8);
    dll.walk(c);
  }

  public static void TestConcurrentDLLDelete() {
    def es = Executors.newCachedThreadPool();
    def val_set = new HashSet<Integer>();
    def c = { node -> assert(val_set.add(node.GetVal())); };
    def dll = new DoublyLinkedList<Integer>();
    for (int i = 0; i < 8; i++) {
      dll.Insert(i);
    }
    for (int i = 0; i < 8; i++) {
      es.execute(new Runnable() { @Override
                                  public void run() {
                                    dll.Delete(dll.Head());
                                  }});
    }
    es.shutdown();
    assert(es.awaitTermination(10, TimeUnit.SECONDS));
    assert(dll.Size() == 0);
  }
}
