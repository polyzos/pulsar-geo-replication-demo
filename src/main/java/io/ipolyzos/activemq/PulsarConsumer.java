package io.ipolyzos.activemq;

import java.nio.charset.Charset;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;

public class PulsarConsumer {
    public static void main(String[] args) throws PulsarClientException {
        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://localhost:6650")
                .build();

        Consumer<byte[]> consumer = pulsarClient.newConsumer(Schema.BYTES)
                .topic("amqp_topic")
                .consumerName("amqp-consumer")
                .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                .subscriptionName("amqp-subscription")
                .subscribe();

        int messageCount = 0;
        while (true) {
            Message<byte[]> message = consumer.receive();
            messageCount += 1;
            System.out.println("Received message: " + new String(message.getData(), Charset.defaultCharset()) + " - Total messages " + messageCount);
            try {
                consumer.acknowledge(message);
            } catch (Exception e) {
                consumer.negativeAcknowledge(message);
            }
        }
    }
}
