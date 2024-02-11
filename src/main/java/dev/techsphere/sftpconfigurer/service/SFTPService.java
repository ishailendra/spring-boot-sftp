package dev.techsphere.sftpconfigurer.service;

import dev.techsphere.sftpconfigurer.config.gateway.MessagingGateway;
import dev.techsphere.sftpconfigurer.config.sftp.IntegrationFlowRegistry;
import dev.techsphere.sftpconfigurer.model.IntegrationFlowRequest;
import dev.techsphere.sftpconfigurer.model.ServerInfoRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class SFTPService {

    @Autowired
    private ConfigurableApplicationContext appContext;

    @Autowired
    private IntegrationFlowContext flowContext;

    @Autowired
    private IntegrationFlowRegistry flowRegistry;

    @Autowired
    private MessagingGateway gateway;

    public void copyFilesFromRemoteDir(String remoteDir, String localDir, String serverName) {
        System.out.println("Fetching remote files:: Remote Dir -  " + remoteDir + "  Local Dir - " + localDir + " server name:: " + serverName);

        String channelName = "inboundChannel-" + serverName;
        MessageChannel currentChannel = appContext.getBean(channelName, MessageChannel.class);
        List<File> result = gateway.downloadFiles(remoteDir, localDir, currentChannel);

        System.out.println("Result download files:: " + result);
        System.out.println("Completed fetching remote files:: Remote Dir -  " + remoteDir + "  Local Dir - " + localDir);
    }

    public void transferFilesToRemote(String remoteDir, String localDir, String tempDir, String archDir, String serverName) {
        System.out.println("Transferring files to Remote:: Remote Dir -  " + remoteDir + "  Local Dir - " + localDir + "server name:: " + serverName);

        File files = new File(localDir);
        File[] listFiles = files.listFiles((dir, name) -> name.matches(".*\\.json"));

        String channelName = "outboundChannel-" + serverName;
        MessageChannel currentChannel = appContext.getBean(channelName, MessageChannel.class);

        var result = gateway.uploadMultiFiles(remoteDir, Arrays.stream(listFiles).toList(), currentChannel);
        System.out.println("Transferred files:: " + result);
        System.out.println("Transferring files to Remote:: Remote Dir -  " + remoteDir + "  Local Dir - " + localDir);
    }

    public String registerSessionFactory(ServerInfoRequest server) {
        SessionFactory<SftpClient.DirEntry> session = getSftpSessionFactory(server.getHost(),
                                                                                server.getPort(),
                                                                                server.getUser(),
                                                                                server.getPassword(),
                                                                                server.getPrivateKeyPath(),
                                                                                server.getPrivateKeyPassphrase());

        appContext.getBeanFactory()
                .registerSingleton("session-" + server.getServerName(), session);

        return "SUCCESS";
    }

    public String registerInboundFlow(IntegrationFlowRequest request) {
        String serverName = request.getServerName();

        SessionFactory<SftpClient.DirEntry> session = (SessionFactory<SftpClient.DirEntry>) appContext.getBean("session-" + request.getServerName());

        IntegrationFlow inboundFlow = flowRegistry.inboundConfig(session, serverName);

        flowContext.registration(inboundFlow).register();

        scheduleSFTPService(request, "INBOUND");
        return "SUCCESS";
    }

    public String registerOutboundFlow(IntegrationFlowRequest request) {
        String serverName = request.getServerName();

        SessionFactory<SftpClient.DirEntry> session = (SessionFactory<SftpClient.DirEntry>) appContext.getBean("session-" + request.getServerName());

        IntegrationFlow outboundFlow = flowRegistry.outboundConfig(session, serverName);

        flowContext.registration(outboundFlow).register();

        scheduleSFTPService(request, "OUTBOUND");
        return "SUCCESS";
    }

    public void scheduleSFTPService(IntegrationFlowRequest request, String type) {
        ThreadPoolTaskScheduler exec = new ThreadPoolTaskScheduler();
        exec.initialize();
        exec.schedule(() -> {
            if ("OUTBOUND".equalsIgnoreCase(type))
                transferFilesToRemote(request.getRemoteDir(), request.getLocalDir(), request.getTempDir(), request.getArchDir(), request.getServerName());
            else if ("INBOUND".equalsIgnoreCase(type))
                copyFilesFromRemoteDir(request.getRemoteDir(), request.getLocalDir(), request.getServerName());
        }, new CronTrigger(request.getSchedule()));
    }

    private SessionFactory<SftpClient.DirEntry> getSftpSessionFactory(String host, int port, String user, String pwd, String privateKeyPath, String privateKeyPassphdrase) {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(user);
        factory.setAllowUnknownKeys(true);

        Resource res = getPrivateKey(privateKeyPath);
        if (res != null) {
            factory.setPrivateKey(res);
            factory.setPrivateKeyPassphrase(privateKeyPassphdrase);
        } else {
            factory.setPassword(pwd);
        }
        return new CachingSessionFactory<>(factory);
    }

    private Resource getPrivateKey(String privateKeyPath) {
        Resource res = null;
        try {
            if (StringUtils.isBlank(privateKeyPath)) {
                return res;
            }
            res = new FileSystemResource(privateKeyPath);
            return res.isFile() && res.exists() ? res : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

}
