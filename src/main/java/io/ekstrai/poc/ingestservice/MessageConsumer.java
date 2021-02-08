package io.ekstrai.poc.ingestservice;


import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;


import java.io.IOException;
import java.time.Duration;

@Component
public class MessageConsumer {


    private final Logger LOG = LoggerFactory.getLogger(MessageConsumer.class);
    private final ObjectMapper MAPPER = new ObjectMapper();

    private final Slack slack = Slack.getInstance();

    @Value("${azure.servicebus.pKey}")
    private String primaryConnectionKey;

    @Value("${azure.servicebus.topicName}")
    private String topicName;

    @Value("${azure.servicebus.subName}")
    private String subName;

    @Value("${slack.webhook}")
    private String SLACK_WEBHOOK_URL;


    @Scheduled(fixedRate = 2000)
    public void ingest() {
        final ServiceBusReceiverClient client = constructClient();

        client.receiveMessages(3).stream().forEach(m -> {
            LOG.info("executed");
            notifyMachineEvent(eventConverter(m));
        });
        client.close();
    }


    private ServiceBusReceiverClient constructClient() {

        return new ServiceBusClientBuilder()
                .connectionString(primaryConnectionKey)
                .receiver()
                .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                .maxAutoLockRenewDuration(Duration.ofMinutes(1))
                .topicName(topicName)
                .subscriptionName(subName)
                .buildClient();
    }


    private MachineEvent eventConverter(ServiceBusReceivedMessage message) {
         try{

            return MAPPER.readValue(message.getBody().toString(), MachineEvent.class);
        } catch (Exception e) {

            LOG.error("ERROR  @ eventConverter " + e.toString());
            return null;
        }
    }

    private void notifyMachineEvent(MachineEvent event) {
        try {

           final WebhookResponse response =  slack.send(
                   SLACK_WEBHOOK_URL,
                   Payload.builder().blocks(
                           asBlocks(
                                   header(h -> h.text(plainText(":pager: New Machine Event "))),
                                   divider(),
                                   section(s -> s.text(markdownText("*`"+ event.getTimeStamp()+"`*"))),
                                   section(s -> s.text(markdownText("Machine: "+ event.getMachine()))),
                                   section(s -> s.text(markdownText((event.getParameter().contains("stop") ?
                                           ":name_badge: *STOPPED* due to Yarnbreak #error" :
                                           ":white_check_mark: *STARTED* Expected until counter #" + event.getValue())))),
                                   section(s -> s.text(markdownText("_session ID: "+ event.getSession()+"_"))),
                                   divider(),
                                   divider()
                           )).build());
           LOG.info(response.toString());
        } catch (IOException e ) {

            LOG.error("IO Exception failed to send");
            e.printStackTrace();
        }
    }



}
