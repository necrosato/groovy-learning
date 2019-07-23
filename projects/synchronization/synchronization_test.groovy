package synchronization;
import synchronization.*;

class SynchronizationTest {

  public static void main(String[] args) {
    SingleProducerSingleConsumerWorks();
    SingleProducerMultiConsumerWorks();
    MultiProducerSingleConsumerWorks();
    MultiProducerMultiConsumerWorks();
  }

  public static SingleProducerSingleConsumerWorks() {
    to_produce_delayed = 0;
    def producer = new ProducerGroup<Integer>(this.&ProduceIntDelayed, [0], 100, 1);
    def consumer = new ConsumerGroup(producer, 1);
    producer.start();
    consumer.start();
    producer.join();
    consumer.join();

    assert (producer.Produced().size() == 0);
    assert (consumer.Consumed() == 0..99);
  }

  public static SingleProducerMultiConsumerWorks() {
    to_produce_delayed = 0;
    def producer = new ProducerGroup<Integer>(this.&ProduceIntDelayed, [0], 100, 1);
    def consumers = new ConsumerGroup(producer, 4);
    producer.start();
    consumers.start();
    producer.join();
    consumers.join();

    assert (consumers.Consumed() == 0..99);
  }

  public static MultiProducerSingleConsumerWorks() {
    to_produce_delayed = 0;
    def producers = new ProducerGroup<Integer>(this.&ProduceIntDelayed, [10], 100, 16);
    def consumer = new ConsumerGroup(producers, 1);
    producers.start();
    consumer.start();
    producers.join();
    consumer.join();

    assert (consumer.Consumed() == 0..99);
  }

  public static MultiProducerMultiConsumerWorks() {
    to_produce_delayed = 0;
    def producers = new ProducerGroup<Integer>(this.&ProduceIntDelayed, [10], 100, 16);
    def consumers = new ConsumerGroup(producers, 4);
    producers.start();
    consumers.start();
    producers.join();
    consumers.join();

    assert (consumers.Consumed() == 0..99);
  }

  private static int to_produce_delayed = 0;
  public static ProduceIntDelayed(int ms) {
    Thread.currentThread().sleep(ms);
    synchronized (to_produce_delayed) {
      return to_produce_delayed++;
    }
  }
}
