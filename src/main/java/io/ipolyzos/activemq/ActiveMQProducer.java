package io.ipolyzos.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ActiveMQProducer {
    private static final String BROKER_URL = ActiveMQConnection.DEFAULT_BROKER_URL;
    private static final String MESSAGE_QUEUE = "test_queue";

    public static void main(String[] args) throws JMSException {
        // Getting JMS connection from the server and starting it
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        //Creating a non-transactional session to send/receive JMS message.
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination destination = session.createQueue(MESSAGE_QUEUE);
        MessageProducer producer = session.createProducer(destination);

        for (int i = 1; i <= 1000; i++) {
            TextMessage message = session
                    .createTextMessage("This is message " + i);
            producer.send(message);
            System.out.println("Send message: " + message.getText());
        }
        connection.close();
    }
}
