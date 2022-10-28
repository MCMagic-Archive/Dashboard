package us.mcmagic.dashboard.utils;

import us.mcmagic.dashboard.slack.SlackAttachment;
import us.mcmagic.dashboard.slack.SlackMessage;
import us.mcmagic.dashboard.slack.SlackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 9/12/16
 */
public class SlackUtil {
    public SlackService s = new SlackService();
    private boolean disabled = false;

    public void sendDashboardMessage(SlackMessage msg) {
        sendDashboardMessage(msg, new ArrayList<SlackAttachment>());
    }

    public void sendDashboardMessage(SlackMessage msg, List<SlackAttachment> attachments) {
        if (disabled) {
            return;
        }
        String webhook = "https://hooks.slack.com/services/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";
        try {
            s.push(webhook, msg, attachments);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}