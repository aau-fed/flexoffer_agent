package org.goflex.wp2.fogenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.Executor;

@SpringBootApplication
@ComponentScan(
    basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.fogenerator"}
//    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"org.goflex.wp2.foa.swiss.*"})
)
@EnableAsync
public class GeneratorStandaloneApp {

    private static final Logger logger = LoggerFactory.getLogger(GeneratorStandaloneApp.class);

    public static void main(String[] args) {
        for (String arg : args) {
            logger.debug(arg);
        }
        SpringApplication.run(GeneratorStandaloneApp.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("GithubLookup-");
        executor.initialize();
        return executor;
    }
}
