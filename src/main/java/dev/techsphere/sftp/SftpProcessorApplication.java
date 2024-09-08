package dev.techsphere.sftp;

import dev.techsphere.sftp.config.SftpServerInfo;
import io.micrometer.common.util.StringUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableIntegration
@EnableScheduling
public class SftpProcessorApplication {

	@Autowired
	private SftpServerInfo serverInfo;

	public static void main(String[] args) {
		SpringApplication.run(SftpProcessorApplication.class, args);
	}

	@Bean
	public SessionFactory<SftpClient.DirEntry> sessionFactory() {
		DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
		factory.setHost(serverInfo.getHost());
		factory.setPort(serverInfo.getPort());
		factory.setUser(serverInfo.getUsername());
		factory.setAllowUnknownKeys(true);

		Resource res = getPrivateKeyRes();
		if (res != null) {
			factory.setPrivateKey(res);
			factory.setPrivateKeyPassphrase(serverInfo.getPrivateKeyPassphrase());
		} else {
			factory.setPassword(serverInfo.getPassword());
		}

		return new CachingSessionFactory<>(factory);
	}

	private Resource getPrivateKeyRes() {

		if (StringUtils.isBlank(serverInfo.getPrivateKeyPath())) {
			return null;
		}
		Resource res = new FileSystemResource(serverInfo.getPrivateKeyPath());

		if(res.isFile() && res.exists()) {
			return res;
		}
		return null;
	}
}
