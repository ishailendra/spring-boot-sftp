package dev.techsphere.sftp.config;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.messaging.Message;

import java.io.File;

@Configuration
public class SftpOutboundConfig {

    @Autowired
    private SessionFactory<SftpClient.DirEntry> sessionFactory;

    @Bean
    public IntegrationFlow outboundConfig() {
        return IntegrationFlow.from("outboundChannel")
                .handle(Sftp.outboundAdapter(sessionFactory, FileExistsMode.REPLACE)
                        .remoteDirectoryExpression("headers['remote-target-dir']")
                        .autoCreateDirectory(false)
                        .fileNameGenerator(new FileNameGenerator() {
                            @Override
                            public String generateFileName(Message<?> message) {
                                return ((File) message.getPayload()).getName();
                            }
                        })
                ).get();
    }
}
