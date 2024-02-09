package dev.techsphere.sftpconfigurer.config.scheduler;

import dev.techsphere.sftpconfigurer.model.IntegrationFlowRequest;
import dev.techsphere.sftpconfigurer.service.SFTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SchedulerRegistry {

    @Autowired
    private Environment env;

    @Autowired
    private SFTPService service;

    public void scheduleWorkflow() {
        System.out.println("========Scheduling Workflow========");

        Map<String, Map<String, Object>> serverProps = Binder.get(env)
                .bind("sftp.server", Map.class)
                .get();

        for (Map.Entry<String, Map<String, Object>> entry: serverProps.entrySet()) {

            Map<String, Object> serverDetails = entry.getValue();
            String serverName = String.valueOf(serverDetails.get("name"));

            if (serverDetails.containsKey("inbound")) {
                Map<String, Map<String, String>> inboundDetails = (Map<String, Map<String, String>>) serverDetails.get("inbound");

                for (Map.Entry<String, Map<String, String>> inboundProps: inboundDetails.entrySet()) {

                    Map<String, String> inboundPropDetails = inboundProps.getValue();

                    ThreadPoolTaskScheduler exec = new ThreadPoolTaskScheduler();
                    exec.initialize();
                    exec.schedule(new Runnable() {
                        @Override
                        public void run() {
                            service.copyFilesFromRemoteDir(inboundPropDetails.get("remoteDir"), inboundPropDetails.get("localDir"), serverName);
                        }
                    }, new CronTrigger(inboundPropDetails.get("schedule")));

                }
            }

            if (serverDetails.containsKey("outbound")) {
                Map<String, Map<String, String>> outboundDetails = (Map<String, Map<String, String>>) serverDetails.get("outbound");

                for (Map.Entry<String, Map<String, String>> outboundProps: outboundDetails.entrySet()) {


                    Map<String, String> outboundPropDetails = outboundProps.getValue();
                    ThreadPoolTaskScheduler exec = new ThreadPoolTaskScheduler();
                    exec.initialize();
                    exec.schedule(new Runnable() {
                        @Override
                        public void run() {
                            service.transferFilesToRemote(outboundPropDetails.get("remoteDir"), outboundPropDetails.get("localDir"), outboundPropDetails.get("tempDir"), outboundPropDetails.get("archDir"), serverName);
                        }
                    }, new CronTrigger(outboundPropDetails.get("schedule")));

                }
            }

        }

        System.out.println("========Scheduling Workflow Completed========");
    }


}
