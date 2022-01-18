package io.ipolyzos.pulsar;

import java.nio.charset.Charset;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.impl.auth.AuthenticationToken;

public class FailoverConsumer {
    private static final String token = "";
    private static final String BROKER_URL = "pulsar://localhost:6650";
    private static final String topicName = "persistent://testt/testns/t1";

    public static void main(String[] args) throws PulsarClientException {
        AuthenticationToken authenticationToken = new AuthenticationToken(token);
        PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl(BROKER_URL)
                .authentication(authenticationToken)
                .build();

        Consumer<byte[]> consumer = pulsarClient.newConsumer(Schema.BYTES)
                .topic(topicName)
                .consumerName("test-consumer")
                .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                .subscriptionName("test-subscription")
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
