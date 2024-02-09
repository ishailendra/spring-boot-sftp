package dev.techsphere.sftpconfigurer.config.sftp;

import dev.techsphere.sftpconfigurer.config.OnPropertyArrayCondition;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import java.util.Map;

@Configuration
@Conditional(OnPropertyArrayCondition.class)
public class SessionFactoryRegistry {

    Environment env;

    @Bean
    public static BeanDefinitionRegistrar beanDefinitionRegistrar(Environment env) {
        return new BeanDefinitionRegistrar(env);
    }

    public static class BeanDefinitionRegistrar implements BeanDefinitionRegistryPostProcessor {

        private Map<String, Map<String, Object>> server;
        public BeanDefinitionRegistrar(Environment env) {
            server = Binder.get(env).bind("sftp.server", Map.class).get();
        }
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

            for (Map.Entry<String, Map<String, Object>> serverProps: server.entrySet()) {
                try {
                    Map<String, Object> server = serverProps.getValue();

                    String host = String.valueOf(server.get("host"));
                    String portStr = String.valueOf(server.get("port"));
                    int port = NumberUtils.isParsable(portStr) ? Integer.parseInt(portStr) : 22;
                    String user = String.valueOf(server.get("user"));
                    String pwd = String.valueOf(server.get("password"));
                    String privateKeyPath = String.valueOf(server.get("privateKeyPath"));
                    String privateKeyPassphrase = String.valueOf(server.get("privateKeyPassphrase"));
                    String serverName = String.valueOf(server.get("name"));

                    SessionFactory<SftpClient.DirEntry> session = getSftpSessionFactory(host, port, user, pwd,privateKeyPath, privateKeyPassphrase);

                    beanFactory.registerSingleton("session-" + serverName, session);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
}
