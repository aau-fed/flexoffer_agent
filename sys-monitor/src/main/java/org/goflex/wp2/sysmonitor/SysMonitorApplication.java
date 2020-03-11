package org.goflex.wp2.sysmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"org.goflex.wp2.sysmonitor", "org.goflex.wp2.core", "org.goflex.wp2.foa"})
@EntityScan(basePackages = {"org.goflex.wp2.sysmonitor", "org.goflex.wp2.core", "org.goflex.wp2.foa"})
@EnableJpaRepositories(basePackages = {"org.goflex.wp2.sysmonitor", "org.goflex.wp2.core", "org.goflex.wp2.foa"})
@IntegrationComponentScan(basePackages = {"org.goflex.wp2.sysmonitor", "org.goflex.wp2.core", "org.goflex.wp2.foa"})
public class SysMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SysMonitorApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }

}
