package org.goflex.wp2.foa.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.goflex.wp2.foa.config.FOAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value="mqtt.cloud.enabled", havingValue = "true")
public class MqttPublisher extends MqttBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublisher.class);
    private final FOAProperties foaProperties;
    private final ApplicationContext context;

    @Autowired
    MqttPublisher(FOAProperties foaProperties, ApplicationContext context) {
        this.foaProperties = foaProperties;
        this.context = context;
    }

    public String publishMessage(String topic, byte[] message) {
        LOGGER.debug("Publishing mqtt message: " + message.toString() + " to topic: '" + topic + "'");
        try {
            MqttGateway gateway = context.getBean(MqttGateway.class);
            gateway.sendToMqtt(topic, message);
            LOGGER.debug("Successfully published mqtt message...");
            return "success";
        } catch (Exception e) {
            LOGGER.error(e.getStackTrace().toString());
            return "error";
        }
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        try {
            // clientId has to be unique
            String clientId = MqttClient.generateClientId();
            MqttPahoClientFactory factory = mqttClientFactory();
            MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, factory);
            messageHandler.setAsync(true);
            //messageHandler.setDefaultTopic("testTopic");
            return messageHandler;
        } catch (Exception ex)
        {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttGateway {
        void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, byte[] message);
    }
}