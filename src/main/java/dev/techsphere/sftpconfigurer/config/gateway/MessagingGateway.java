package dev.techsphere.sftpconfigurer.config.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;

@Component
public class MessagingGateway {

    @Autowired
    private MessagingTemplate messagingTemplate;

    public Boolean uploadFile(String remoteDirectory, File file, MessageChannel currentChannel) {
        Message<?> message = MessageBuilder.withPayload(file).setHeader("remote-target-dir", remoteDirectory).build();
        return currentChannel.send(message);
    }

    public List<File> downloadFiles(String remoteDirectory, String localDir, MessageChannel currentChannel) {
        Message<?> msg = MessageBuilder.withPayload(new Date()).setHeader("remote-target-dir", remoteDirectory).setHeader("local-target-dir", localDir).build();
        var result =  messagingTemplate.sendAndReceive(currentChannel, msg);
        return (List<File>) result.getPayload();
    }

    public Object uploadMultiFiles(String remoteDirectory, List<File> files, MessageChannel currentChannel) {
        Message<?> message = MessageBuilder.withPayload(files).setHeader("remote-target-dir", remoteDirectory).build();
        var result = messagingTemplate.sendAndReceive(currentChannel, message);
        return result.getPayload();
    }
}
