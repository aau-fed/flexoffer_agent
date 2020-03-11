package org.goflex.wp2.foa.swiss;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.goflex.wp2.foa.config.FOAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MqttBase {

    private final Logger LOGGER = LoggerFactory.getLogger(MqttBase.class);

    @Autowired
    private FOAProperties foaProperties;


    /**
     * MqttPahoClientFactory method establishes the following details for connecting to the broker
     * 1. URL of the mqtt server (host and port),
     * 2. The username, the password for connecting to the broker.
     * 3. SSL connection with the correct keyStore and trustStore.
     *
     * @return factory with given variables
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());
        return factory;
    }


    private MqttConnectOptions getMqttConnectOptionsForLocalhost() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setServerURIs(new String[]{foaProperties.getMqttServiceConfig().getHost()});
        mqttConnectOptions.setCleanSession(true);
        return mqttConnectOptions;
    }


    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setServerURIs(new String[]{foaProperties.getMqttServiceConfig().getHost()});
        mqttConnectOptions.setUserName(foaProperties.getMqttServiceConfig().getUser());
        mqttConnectOptions.setPassword(foaProperties.getMqttServiceConfig().getPassword().toCharArray());
        mqttConnectOptions.setCleanSession(true); // if false, the adapter will keep the subscription active so that
        // messages arriving while the adapter is stopped will be delivered
        // on the next start.

        Properties sslProps = new Properties();
        sslProps.setProperty("com.ibm.ssl.keyStoreType", "JKS");
        sslProps.setProperty("com.ibm.ssl.trustStore", foaProperties.getMqttServiceConfig().getTrustStore());
        sslProps.setProperty("com.ibm.ssl.trustStorePassword", foaProperties.getMqttServiceConfig().getTrustStorePassword());

        mqttConnectOptions.setSSLProperties(sslProps);
        return mqttConnectOptions;
    }


    /**
     * Receive {@link MqttConnectionFailedEvent} event, which are published when the connection/subscription fails.
     *
     * @return cause of connection failure and the corresponding message
     */
    @Bean
    public ApplicationListener<MqttConnectionFailedEvent> connectionFailedEventListener() {
        return event -> LOGGER.error("MqttConnectionFailedEvent: " + event.getCause().getMessage());
    }

}
