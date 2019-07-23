package synchronization;

/**
 * A group of consumers that consumes from a ProducerGroup.
 * All consumers in the group consume concurrently.
 */
class ConsumerGroup {
  private List<Consumer> consumers;
  private List consumed;

  ConsumerGroup(ProducerGroup p, int num_consumers) {
    this.consumed = [];
    this.consumers = [];
    for (int i : num_consumers) {
      consumers.add(new Consumer(p, consumed));
    }
  }

  public List Consumed() {
    return consumed;
  }

  public void start() {
    for (Consumer consumer : consumers) {
      consumer.start();
    }
  }

  public void join() {
    for (Consumer consumer : consumers) {
      consumer.join();
    }
  }
}

/**
 * A single consumer on a single thread.
 */
class Consumer extends Thread {
  private ProducerGroup p; 
  private List consumed;

  Consumer(ProducerGroup p, List consumed) {
    this.p = p;
    this.consumed = consumed;
  }

  public List Consumed() {
    return consumed;
  } 

  @Override
  public void run() {
    synchronized(p.Produced()) {
      while (p.Producing() || p.Ready()) {
        if (p.Ready()) {
          consumed.add(p.Next());
        } else {
          p.Produced().wait();
        }
      }
    }
  }
}
