package activemq

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

class ActiveMQTest {
  public static void main(String[] args) {
    def producer = new Thread(){
      @Override
      public void run() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        //Create connection.
        Connection connection = factory.createConnection();

        // Start the connection
        connection.start();

        // Create a session which is non transactional
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create Destination queue
        Destination queue = session.createQueue("Test");

        // Create a producer
        MessageProducer producer = session.createProducer(queue);

        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        String msg = "Hello World";

        // insert message
        TextMessage message = session.createTextMessage(msg);
        System.out.println("Producer Sent: " + msg);
        producer.send(message);

        Thread.sleep(10000);
        session.close();
        connection.close();
      }};
    def consumer = new Thread(){
      @Override
      public void run() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

        //Create Connection
        Connection connection = factory.createConnection();

        // Start the connection
        connection.start();

        // Create Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //Create queue
        Destination queue = session.createQueue("Test");

        MessageConsumer consumer = session.createConsumer(queue);

        Message message = consumer.receive(1000);

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("Consumer Received: " + text);
        }

        Thread.sleep(10000);
        session.close();
        connection.close();
      }};
    producer.start();
    consumer.start();
    producer.join();
    consumer.join();
    println("ActiveMQ!");
  }
}
