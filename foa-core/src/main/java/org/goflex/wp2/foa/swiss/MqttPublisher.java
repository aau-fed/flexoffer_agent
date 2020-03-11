package org.goflex.wp2.foa.swiss;

import org.goflex.wp2.foa.config.FOAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class MqttPublisher extends MqttBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPublisher.class);

    @Autowired
    private MqttGateway gateway;

    @Autowired
    private FOAProperties foaProperties;

    public String publishMessage(String topic, byte[] message) {
        LOGGER.debug("Publishing mqtt message: " + message.toString() + " to topic: '" + topic + "'");
        try {
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
        // clientId has to be unique
        String clientId = foaProperties.getMqttServiceConfig().getClientId() + UUID.randomUUID().getLeastSignificantBits();
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(clientId, mqttClientFactory());
        messageHandler.setAsync(true);
        //messageHandler.setDefaultTopic("testTopic");
        return messageHandler;
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