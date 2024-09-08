package dev.techsphere.sftp.service;

import dev.techsphere.sftp.config.SftpGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;

@Component
public class FileTransferScheduler {

    @Value("${sftp.outbound.local.dir}")
    private String srcPath;

    @Value("${sftp.outbound.remote.dir}")
    private String destPath;

    @Autowired
    private SftpGateway gateway;

    @Scheduled(cron = "${sftp.outbound.schedule}")
    public void outboundFileTransfer() {
        try {
            File fileDir = new File(srcPath);
            String fileNames[] = fileDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return StringUtils.endsWithIgnoreCase(name, ".txt");
                }
            });

            for (String fileName: fileNames) {
                File file = new File(srcPath + "/" +fileName);
                if (file.exists() && file.isFile()) {
                    gateway.sendFile(file, destPath);
                }
            }
        } catch (Exception e) {
            //log your errors
            e.printStackTrace();
        }
    }
}
