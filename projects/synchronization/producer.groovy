package synchronization;

class Producer<T> extends Thread {
  private List produced = [];
  private Closure c;
  private List params;
  private int n;
  private int total = 0;

  public Producer(Closure c, List params, int n) {
    this.c = c;
    this.params = params;
    this.n = n;
  }

  public synchronized T Next() {
    assert (Ready());
    return produced.removeAt(0);
  }


  public synchronized boolean Ready() {
    return produced.size() > 0;
  }

  public synchronized boolean Producing() {
    return total < n;
  }

  @Override
  public void run() {
    while (Producing()) {
      def next = c(*params);
      synchronized (this) {
        produced.add(next);
        total++;
        this.notifyAll();
      }
    }
  }
}
