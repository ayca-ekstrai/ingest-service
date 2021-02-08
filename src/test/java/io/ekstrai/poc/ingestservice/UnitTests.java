package io.ekstrai.poc.ingestservice;

import com.azure.core.util.serializer.TypeReference;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class UnitTests {


    private static String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";
    private static final Logger LOG = LoggerFactory.getLogger(UnitTests.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String primaryConnectionKey =
            "Endpoint=sb://ekstraisb00.servicebus.windows.net/;" +
                    "SharedAccessKeyName=RootManageSharedAccessKey;" +
                    "SharedAccessKey=GpTjswshvclzsEKyM92bJRk0qH7l0m8QbmS/UHrvWaQ=";
    private final String queueName = "machineevents";
    private final String subName = "machineeventsub";
    private final String topicName = "machineevents";
    private final String subName1 = "nosessionsub";

    @Test
    void slackWebhook() throws IOException {
        Slack.getInstance().send(SLACK_WEBHOOK_URL, Payload.builder().text("Hello world").build());
    }

    @Test
    void slackWebhookFormatting() throws IOException {
        Slack.getInstance().send(SLACK_WEBHOOK_URL, Payload.builder().text("Hello world").build());
    }


    @Test
    void receiveMessagedfromsb() throws InterruptedException {
        final ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(primaryConnectionKey)
                .sessionProcessor()
                .topicName(topicName)
                .subscriptionName(subName1)
                .processMessage(messageProcessor)
                .processError(processError)
                .buildProcessorClient();

        System.out.println("Starting the processor");
        processorClient.start();


        //TimeUnit.SECONDS.sleep(1000);
        LOG.info("**********  Stopping and closing the processor ********");
        //processorClient.close();
    }

    private Consumer<ServiceBusReceivedMessageContext> messageProcessor = context -> {
        ServiceBusReceivedMessage message = context.getMessage();
        LOG.info("Received message: " + message.getBody().toString() + " from the subscription: " + subName);
    };

    // Consumer that handles any errors that occur when receiving messages
    private Consumer<ServiceBusErrorContext> processError = errorContext ->
            LOG.error("Error occurred while receiving message: " + errorContext.getException());


    @Test
    void messageReceiverClient() {

        final ServiceBusReceiverClient client = new ServiceBusClientBuilder()
                .connectionString(primaryConnectionKey)
                .receiver()
                .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .maxAutoLockRenewDuration(Duration.ofMinutes(1))
                .topicName(topicName)
                .subscriptionName(subName1)
                .buildClient();

        client.receiveMessages(5).stream().forEach(m -> {
            LOG.info("executed");
            LOG.info(m.getBody().toString());
        });

    }


    @Test
    void messageToMachineEventMapping() {


        final ServiceBusReceiverClient client = new ServiceBusClientBuilder()
                .connectionString(primaryConnectionKey)
                .receiver()
                .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .maxAutoLockRenewDuration(Duration.ofMinutes(1))
                .topicName(topicName)
                .subscriptionName(subName1)
                .buildClient();

        client.receiveMessages(1).stream().forEach(m -> {
            LOG.info("executed");
            final String body = m.getBody().toString();
            LOG.info(body);
            final MachineEvent event = machineEventMapper(body);
            assertNotNull(event);
        });



    }

    private static String prettyMapper(Object o) {

        try {
            LOG.info("mapped");
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            LOG.error("not mapped");
            return e.getMessage();
        }
    }


    private static MachineEvent machineEventMapper(String s) {

        try {
            LOG.info("mapped");
            MAPPER.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
            return MAPPER.readValue(s, MachineEvent.class);
        } catch (JsonProcessingException e) {
            LOG.error("not mapped");
            LOG.error(e.getMessage());
            return null;
        }
    }


}
