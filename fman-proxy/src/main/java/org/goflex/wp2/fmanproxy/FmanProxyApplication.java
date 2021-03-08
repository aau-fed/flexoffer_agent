package org.goflex.wp2.fmanproxy;

import org.goflex.wp2.fmanproxy.common.exception.CustomException;
import org.goflex.wp2.fmanproxy.fmaninstance.FmanInstanceRepository;
import org.goflex.wp2.fmanproxy.fmaninstance.FmanInstanceT;
import org.goflex.wp2.fmanproxy.fmaninstance.InstanceStatus;
import org.goflex.wp2.fmanproxy.user.UserRepository;
import org.goflex.wp2.fmanproxy.user.UserRole;
import org.goflex.wp2.fmanproxy.user.UserT;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class FmanProxyApplication {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FmanInstanceRepository fmanInstanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(FmanProxyApplication.class, args);
    }

    @Bean
    InitializingBean initDatabase() {
        return () -> {

            if (userRepository.count() == 0) {

                UserT u = new UserT();
                u.setUserName("AAU");
                u.setFirstName("foa");
                u.setLastName("admin");
                u.setEmail("foa@goflex.org");
                u.setPassword(passwordEncoder.encode("password"));
                u.setRole(UserRole.ROLE_ADMIN);

                userRepository.save(u);
            }

            if (fmanInstanceRepository.count() == 0) {
                FmanInstanceT fmanInstanceT = new FmanInstanceT();

                fmanInstanceT.setInstanceName("Test FMAN");
                fmanInstanceT.setAuthorizedRole(UserRole.ROLE_BROKER);
                fmanInstanceT.setBrokerId(1L);
                fmanInstanceT.setInstanceUrl("http://localhost:8085");
                fmanInstanceT.setActivationDate(new Date());
                fmanInstanceT.setInstanceStatus(InstanceStatus.ACTIVE);
                fmanInstanceT.setRegistrationDate(new Date());

                fmanInstanceRepository.save(fmanInstanceT);
            }
        };
    }


    @Bean(name = "threadPoolTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("Async-");
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(600);
        executor.initialize();
        return executor;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean(name = "scheduleDetail")
    public ConcurrentHashMap<UUID, Object> scheduleDetailTable() {
        ConcurrentHashMap<UUID, Object> scheduleDetail = new ConcurrentHashMap<>();
        return scheduleDetail;
    }
}
