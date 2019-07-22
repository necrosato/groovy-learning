package synchronization;
import synchronization.Producer;
import synchronization.Consumer;

class SynchronizationTest {

  public static void main(String[] args) {
    ProducerSingleConsumerWorks();
    ProducerMultiConsumerWorks();
  }

  public static ProducerSingleConsumerWorks() {
    to_produce_delayed = 0;
    def producer = new Producer<Integer>(this.&ProduceIntDelayed, [10], 100);
    def consumed = [];
    def consumer = new Consumer(producer, consumed);
    producer.start();
    consumer.start();
    producer.join();
    consumer.join();

    assert (consumed == 0..99);
  }

  public static ProducerMultiConsumerWorks() {
    to_produce_delayed = 0;
    def producer = new Producer<Integer>(this.&ProduceIntDelayed, [10], 100);
    def consumed = [];
    def consumer1 = new Consumer(producer, consumed);
    def consumer2 = new Consumer(producer, consumed);
    producer.start();
    consumer1.start();
    consumer2.start();
    producer.join();
    consumer1.join();
    consumer2.join();

    assert (consumed == 0..99);
  }

  private static int to_produce_delayed = 0;
  public static ProduceIntDelayed(int ms) {
    Thread.currentThread().sleep(ms);
    return to_produce_delayed++;
  }
}
