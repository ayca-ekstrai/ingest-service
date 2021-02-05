package io.ekstrai.poc.ingestservice;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import org.junit.jupiter.api.Test;
import java.io.IOException;


class UnitTests {


    private static String SLACK_WEBHOOK_URL = "https://hooks.slack.com/services/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";



    @Test
    void slackWebhook() throws IOException {
        Slack.getInstance().send(SLACK_WEBHOOK_URL, Payload.builder().text("Hello world").build());
    }
}
