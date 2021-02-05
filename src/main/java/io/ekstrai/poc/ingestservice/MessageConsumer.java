package io.ekstrai.poc.ingestservice;


import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
public class MessageConsumer {


    private static final Logger LOG = LoggerFactory.getLogger(MessageConsumer.class);

    private static final Slack slack = Slack.getInstance();

    @Value("${slack.webhook}")
    private String SLACK_WEBHOOK_URL;


    @Scheduled(fixedRate = 10000)
    public void ingest() {
        LOG.info("Got triggered! ");
        final Payload payload = Payload.builder().text("Hello world!").build();
        try {

            final WebhookResponse response = slack.send(SLACK_WEBHOOK_URL, payload);
            LOG.info(response.toString());
        } catch (IOException e ) {
            LOG.error("IO Exception failed to send");
            e.printStackTrace();
        }
    }





}
