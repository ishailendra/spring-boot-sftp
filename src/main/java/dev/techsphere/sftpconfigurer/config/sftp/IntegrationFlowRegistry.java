package dev.techsphere.sftpconfigurer.config.sftp;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class IntegrationFlowRegistry {

    @Autowired
    private IntegrationFlowContext flowContext;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private Environment env;

    public void registerIntegrationFlow() {
        try {
            Map<String, Map<String, Object>> serverProps = Binder.get(env)
                    .bind("sftp.server", Map.class)
                    .get();

            for (Map.Entry<String, Map<String, Object>> entry: serverProps.entrySet()) {

                Map<String, Object> serverDetails = entry.getValue();

                String serverName = String.valueOf(serverDetails.get("name"));

                SessionFactory<SftpClient.DirEntry> session = (SessionFactory<SftpClient.DirEntry>) appContext.getBean("session-" + serverName);

                if (serverDetails.containsKey("inbound")) {
                    IntegrationFlow inboundFlow = inboundConfig(session, serverName);
                    flowContext.registration(inboundFlow).register();
                }

                if (serverDetails.containsKey("outbound")) {
                    IntegrationFlow outboundFlow = outboundConfig(session, serverName);
                    flowContext.registration(outboundFlow).register();
                }
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IntegrationFlow inboundConfig(SessionFactory<SftpClient.DirEntry> session, String serverName) {
        return IntegrationFlow.from("inboundChannel-" + serverName)
                .handle(Sftp.outboundGateway(session,
                                AbstractRemoteFileOutboundGateway.Command.MGET,
                                "headers['remote-target-dir']")
                        .localDirectoryExpression("headers['local-target-dir']")
                        //.options(AbstractRemoteFileOutboundGateway.Option.DELETE)
                        .fileExistsMode(FileExistsMode.REPLACE))
                .get();
    }

    public IntegrationFlow outboundConfig(SessionFactory<SftpClient.DirEntry> session, String serverName) {
        return IntegrationFlow.from("outboundChannel-" + serverName)
                .handle(Sftp.outboundGateway(session,
                                AbstractRemoteFileOutboundGateway.Command.MPUT,
                                "headers['remote-target-dir']")
                        .remoteDirectoryExpression("headers['remote-target-dir']")
                        .useTemporaryFileName(false)
                        .fileExistsMode(FileExistsMode.REPLACE))
                .get();
    }
}
