package org.goflex.wp2.foa.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.goflex.wp2.foa.config.FOAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@ConditionalOnProperty(value="mqtt.cloud.enabled", havingValue = "true")
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
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = getMqttConnectOptions();
        if (options == null) return null;
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
        String host = foaProperties.getMqttServiceConfig().getHost();
        String user = foaProperties.getMqttServiceConfig().getUser();
        String password = foaProperties.getMqttServiceConfig().getPassword();

        if (host == null || host.equals("") || user == null || user.equals("") || password == null || password.equals(""))
        {
            LOGGER.warn("MQTT connection info or credentials are invalid");
        }

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setServerURIs(new String[]{host});
        mqttConnectOptions.setUserName(user);
        mqttConnectOptions.setPassword(password.toCharArray());
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
