package synchronization;

/**
 * Class to provide shared int references, pass by reference, etc.
 */
class IntWrapper {
  public int val;
  IntWrapper(int n) { this.val = n; }
}

/**
 * A group of concurrent producers.
 */
class ProducerGroup<T> {
  private List<Producer<T>> producers;
  private List<T> produced;
  IntWrapper n;
  IntWrapper total;
  IntWrapper computations;

  ProducerGroup(Closure c, List params, int n, int num_producers) {
    this.producers = [];
    this.n = new IntWrapper(n);
    this.total = new IntWrapper(0);
    this.computations = new IntWrapper(0);
    this.produced = [];
    for (int i : n) {
      producers.add(new Producer<T>(c, params, this.n, this.total, this.computations, this.produced));
    }
  }

  public List<Producer<T>> Producers() {
    return producers;
  }

  public List<T> Produced() {
    return produced;
  }

  public boolean Ready() {
    synchronized (produced) {
      return produced.size() > 0;
    }
  }

  public boolean Producing() {
    synchronized (produced) {
      return total.val < n.val;
    }
  }

  public T Next() {
    synchronized (produced) {
      assert (Ready());
      return produced.removeAt(0);
    }
  }

  public void start() {
    for (Producer<T> producer : producers) {
      producer.start();
    }
  }

  public void join() {
    for (Producer<T> producer : producers) {
      producer.join();
    }
  }
}

class Producer<T> extends Thread {
  private List produced;
  private Closure c;
  private List params;
  private IntWrapper n;
  private IntWrapper total;
  private IntWrapper computations;

  /**
   * Constructor for single producer.
   * Takes a producer closure and its parameters, and n objects to produce.
   */
  public Producer(Closure c, List params, int n) {
    this.c = c;
    this.params = params;
    this.total = new IntWrapper(0);
    this.n = new IntWrapper(n);
    this.produced = [];
  }

  /**
   * Constructor with produced parameter.
   * For multiple producers to produce into a shared list.
   * This constructor should only be used by ProducerGroup, but since java has no notion of friend classes it is public.
   */
  public Producer(Closure c, List params, IntWrapper n, IntWrapper total, IntWrapper computations, List produced) {
    this.c = c;
    this.params = params;
    this.n = n;
    this.total = total;
    this.computations = computations;
    this.produced = produced;
  }

  public boolean Ready() {
    synchronized (produced) {
      return produced.size() > 0;
    }
  }

  public boolean Producing() {
    synchronized (produced) {
      return total.val < n.val;
    }
  }

  /**
   * Producers can obtain a boolean whether thay can compute the next object.
   * Returns false when the total number of computations finished, or scheduled by other threads, has reached n.
   */
  private boolean ObtainComputePass() {
    synchronized (produced) {
      if (computations.val < n.val) {
        computations.val++;
        return true;
      }
      return false;
    }
  }
  
  @Override
  public void run() {
    while (ObtainComputePass()) {
      def next = c(*params);
      synchronized (produced) {
        produced.add(next);
        total.val++;
        produced.notifyAll();
      }
    }
  }
}
