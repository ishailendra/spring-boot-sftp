package dev.techsphere.sftpconfigurer.scheduler;

import dev.techsphere.sftpconfigurer.config.scheduler.SchedulerRegistry;
import dev.techsphere.sftpconfigurer.config.sftp.IntegrationFlowRegistry;
import dev.techsphere.sftpconfigurer.model.IntegrationFlowRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class SftpScheduler {

    @Autowired
    private IntegrationFlowRegistry flowRegistry;

    @Autowired
    private SchedulerRegistry schedulerRegistry;

    @EventListener(ApplicationReadyEvent.class)
    public void startSftpService() {
        System.out.println("======== Starting Sftp Service ========");

        System.out.println("======== Registering Integration Flow ========");
        flowRegistry.registerIntegrationFlow();

        System.out.println("======== Registering Schedulers ========");
        schedulerRegistry.scheduleWorkflow();
    }


}
