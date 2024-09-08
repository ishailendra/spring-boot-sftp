package dev.techsphere.sftp.config;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.filters.SftpRegexPatternFileListFilter;

import java.io.File;
import java.util.regex.Pattern;

@Configuration
public class SftpInboundConfig {

    @Autowired
    private SessionFactory<SftpClient.DirEntry> sessionFactory;

    @Autowired
    private SftpServerInfo serverInfo;

    @Value("${sftp.inbound.remote.dir}")
    private String remoteDir;

    @Value("${sftp.inbound.local.dir}")
    private String localDir;

    @Value("${sftp.inbound.schedule}")
    private String inboundSchedule;

    @Bean
    public IntegrationFlow inboundConfig() {
        return IntegrationFlow.from(Sftp.inboundAdapter(sessionFactory)
                                .preserveTimestamp(false)
                                .remoteDirectory(remoteDir)
                                .filter(new SftpRegexPatternFileListFilter(Pattern.compile("^.*\\.txt$", Pattern.CASE_INSENSITIVE)))
                                .deleteRemoteFiles(true)
                                .localDirectory(new File(localDir))
                                .localFilter(new AcceptOnceFileListFilter<>())
                                .maxFetchSize(-1),
                        e -> e.id("inboundAdapter")
                                .autoStartup(true)
                                .poller(Pollers.cron(inboundSchedule).maxMessagesPerPoll(-1)))
                .handle(msg -> {
                    try {
                        File file = (File) msg.getPayload();
                    } catch (Exception e) {
                        //log your error
                    }
                }).get();


    }
}
