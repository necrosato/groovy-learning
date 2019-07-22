package synchronization;
import synchronization.Consumer;

class Consumer extends Thread {
  private Producer p; 
  private List consumed;

  Consumer(Producer p, List consumed) {
    this.p = p;
    this.consumed = consumed;
  }

  public List Consumed() {
    return consumed;
  } 

  @Override
  public void run() {
    synchronized(p) {
      while (p.Producing() || p.Ready()) {
        if (p.Ready()) {
          consumed.add(p.Next());
        } else {
          p.wait();
        }
      }
    }
  }
}
