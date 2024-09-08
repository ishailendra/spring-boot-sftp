package dev.techsphere.sftp.config;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.File;

@MessagingGateway
public interface SftpGateway {

    @Gateway(requestChannel = "outboundChannel")
    public void sendFile(@Payload File file, @Header("remote-target-dir") String targetDir);
}
