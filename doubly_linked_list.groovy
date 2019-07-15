//package doubly_linked_list

class DLLTest {
  public static void main(String[] args) {
    TestInsert();
    TestDelete();
    TestDeleteHead();
    TestDeleteTail();
    TestDeleteHeadTail();
    TestMoveToHead();
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
}

/**
  Node in a doubly linked list
*/
class DLLNode<T> {
  private DLLNode<T> prev;
  private DLLNode<T> next;
  private T val;

  DLLNode(DLLNode<T> prev, DLLNode<T> next, T val) {
    this.prev = prev;
    this.next = next;
    this.val = val;
  }

  public DLLNode<T> GetPrev() {
    return prev;
  }

  public void SetPrev(DLLNode<T> prev) {
    this.prev = prev;
  }

  public DLLNode<T> GetNext() {
    return next;
  }

  public void SetNext(DLLNode<T> next) {
    this.next = next;
  }

  public T GetVal() {
    return val;
  }

  public void SetVal(DLLNode<T> val) {
    this.val = val;
  }

  public String toString() {
    return val.toString();
  }
}

/**
  Implementation of a generic DoublyLinkedList
*/
class DoublyLinkedList<T> {
  private DLLNode<T> head;
  private DLLNode<T> tail;
  private int size = 0;

  public DLLNode<T> Head() {
    return head;
  }

  public DLLNode<T> Tail() {
    return tail;
  }

  public void Insert(T val) {
    if (head == null && tail == null) {
      head = new DLLNode(null, null, val);
      tail = head;
    } else {
      def NewNode = new DLLNode<T>(tail, null, val);
      tail.SetNext(NewNode);
      tail = NewNode;
    }
    size++;
  }
  
  public void Delete(DLLNode<T> toDelete) {
    def toDeletePrev = toDelete.GetPrev();
    def toDeleteNext = toDelete.GetNext();
    if (toDeletePrev != null) {
      toDeletePrev.SetNext(toDeleteNext);
    }
    if (toDeleteNext != null) {
      toDeleteNext.SetPrev(toDeletePrev);
    }
    toDelete.SetPrev(null);
    toDelete.SetNext(null);
    size--;

    if (toDelete == head) {
      head = toDeleteNext;
    }
    if (toDelete == tail) {
      tail = toDeletePrev;
    }
  }

  public void MoveToHead(DLLNode<T> node) {
    Delete(node);
    node.SetNext(head);
    if (head != null) {
      head.SetPrev(node);
    }
    head = node;
  }

  public void InsertHead(T val) {
    Insert(val);
    MoveToHead(tail);
  }

  public int Size() {
    return size;
  }
}
