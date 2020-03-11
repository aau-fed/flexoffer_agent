package org.goflex.wp2.fogenerator;

import org.goflex.wp2.fogenerator.services.FOGenerationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableScheduling
@EnableTransactionManagement
@EntityScan(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa"})
@EnableJpaRepositories(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.fogenerator"})
@IntegrationComponentScan(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.fogenerator"})
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Autowired
    private FOGenerationService foGenerationService;

    @Resource(name = "startGeneratingFo")
    private ConcurrentHashMap<String, Integer> startGeneratingFo;

    @Bean
    InitializingBean populateFOCount(){
        return () -> {
            this.foGenerationService.populateFOCount();
            startGeneratingFo.put("start", 1);
       };
    }
}
