package io.ipolyzos.pulsar;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.auth.AuthenticationToken;

public class PulsarProducer {
    private static final String BROKER_URL = "pulsar://localhost:6650";
    private static final String FALLBACK_BROKER_URL = "pulsar://localhost:6650";
    private static final int totalMessages = 300;
    private static int messagesSoFar = 0;
    private static final String token = "";
    private static final String topicName = "persistent://testt/testns/t1";


    public static void main(String[] args) throws PulsarClientException {
        AuthenticationToken authenticationToken = new AuthenticationToken(token);
        PulsarClient pulsarClient = null;
        try {
            pulsarClient = PulsarClient.builder()
                    .serviceUrl(BROKER_URL)
                    .authentication(authenticationToken)
                    .build();
            runProducer(pulsarClient);
        } catch (PulsarClientException exception) {
            System.out.println("Connection lost with west cluster at '" + BROKER_URL + "'");
            System.out.println("Attemping connection with central cluster at '" + FALLBACK_BROKER_URL + "'");
            pulsarClient = PulsarClient.builder()
                    .serviceUrl(FALLBACK_BROKER_URL)
                    .authentication(authenticationToken)
                    .build();
            runProducer(pulsarClient);
        }
    }

    private static void runProducer(PulsarClient pulsarClient) throws PulsarClientException {
        Producer<byte[]> producer = pulsarClient.newProducer(Schema.BYTES)
                .topic(topicName)
                .create();

        for (int i = messagesSoFar; i < totalMessages; i++) {
            System.out.println("Sending msg-" + i);
            producer.newMessage()
                    .value(("msg-" + i).getBytes())
                    .send();
            messagesSoFar += 1;
        }
        producer.close();
        pulsarClient.close();
    }
}
