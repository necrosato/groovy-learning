package doubly_linked_list;

class DLLTest {
  public static void main(String[] args) {
    TestInsert();
    TestDelete();
    TestDeleteHead();
    TestDeleteTail();
    TestDeleteHeadTail();
    TestMoveToHead();
    TestMoveToHeadSingle();
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
}

/**
 * Node in a doubly linked list.
 */
class DLLNode<T> {
  private DLLNode<T> prev;
  private DLLNode<T> next;
  private T val;
  private int dll_id;

  /**
   * Constructor:
   * Params:
   *  prev: The previous node
   *  next: The next node
   *  val: The value of the node to be constructed
   *  dll_id: The numeric id of the DoublyLinkedList instance this node is a part of
   */
  DLLNode(DLLNode<T> prev, DLLNode<T> next, T val, int dll_id) {
    this.dll_id = dll_id;
    this.val = val;
    SetPrev(prev);
    SetNext(next);
  }

  public DLLNode<T> GetPrev() {
    return prev;
  }

  public void SetPrev(DLLNode<T> prev) {
    if (prev != null) {
      assert(prev.GetID() == dll_id);
    }
    this.prev = prev;
  }

  public DLLNode<T> GetNext() {
    return next;
  }

  public void SetNext(DLLNode<T> next) {
    if (next != null) {
      assert(next.GetID() == dll_id);
    }
    this.next = next;
  }

  public T GetVal() {
    return val;
  }

  public void SetVal(DLLNode<T> val) {
    this.val = val;
  }

  public int GetID() {
    return dll_id;
  }

  public String toString() {
    return val.toString();
  }
}

/**
 * Implementation of a generic DoublyLinkedList
 */
class DoublyLinkedList<T> {
  // The id of the next DoublyLinkedList will be set to this value, then next_id will be incremented.
  static int next_id = 0;
  private DLLNode<T> head;
  private DLLNode<T> tail;
  private int size = 0;
  // Unique identifier of this DLL. All DLLNode instances that are a part of this DLL will have the same unique ID.
  private int id;

  public DoublyLinkedList() {
    this.id = DoublyLinkedList.next_id;
    DoublyLinkedList.next_id++;
  }

  public DLLNode<T> Head() {
    return head;
  }

  public DLLNode<T> Tail() {
    return tail;
  }

  /**
   * Create a node for val and insert it on the tail of the DLL.
   */
  public void Insert(T val) {
    if (head == null && tail == null) {
      head = new DLLNode(null, null, val, id);
      tail = head;
    } else {
      assert head != null;
      assert tail != null;
      def NewNode = new DLLNode<T>(tail, null, val, id);
      tail.SetNext(NewNode);
      tail = NewNode;
    }
    size++;
  }
  
  /**
   * Delete a given node from the DLL.
   */
  public void Delete(DLLNode<T> toDelete) {
    assert (toDelete.GetID() == id);
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

  /**
   * Move a node in the DLL to the head of the list.
   */
  public void MoveToHead(DLLNode<T> node) {
    assert (node.GetID() == id);
    if (head == node) {
      return
    }
    Delete(node);
    size++;
    node.SetNext(head);
    head.SetPrev(node);
    head = node;
  }

  /**
   * Insert a value at the head of the list.
   */
  public void InsertHead(T val) {
    Insert(val);
    MoveToHead(tail);
  }

  /**
   * The current size of the DLL.
   */
  public int Size() {
    return size;
  }

  public int GetID() {
    return id;
  }
}
