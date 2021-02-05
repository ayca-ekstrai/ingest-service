package io.ekstrai.poc.ingestservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {


    private static final Logger LOG = LoggerFactory.getLogger(MessageConsumer.class);


    @Scheduled(fixedRate = 1000)
    public void ingest() {
        LOG.info("Got triggered! ");
    }

}
