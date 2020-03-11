package org.goflex.wp2.app.swiss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.swiss.MqttBase;
import org.goflex.wp2.foa.swiss.MqttTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MqttSubscriber extends MqttBase {

    private final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriber.class);

    @Resource(name = "mqttDataMap")
    private ConcurrentHashMap<String, Map<String, Map<String, Object>>> mqttDataMap;

    private MqttTopics mqttTopics;
    private ApplicationContext applicationContext;
    private FOAProperties foaProperties;

    private List<String> topics;

    @Autowired
    public MqttSubscriber(MqttTopics mqttTopics,
                          ApplicationContext applicationContext,
                          FOAProperties foaProperties) {
        this.mqttTopics = mqttTopics;
        this.applicationContext = applicationContext;
        this.foaProperties = foaProperties;
        this.topics = prepareTopics();
    }


    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }


    /**
     * MessageProducer creates a new connection adapter to a broker by setting the clientID,
     * MqttClientFactory, the topic(s) to subscribe to, and optionally Completion Timeout,
     * Converter, Qos, and the Output Channel for numMessages to go to.
     *
     * @return adapter for the mqttInbound connection
     */
    @Bean(name = "mqttInboundBean")
    private MessageProducerSupport mqttInbound() {
        String clientId = foaProperties.getMqttServiceConfig().getClientId() + UUID.randomUUID().getLeastSignificantBits();
//        List<String> topics = prepareTopics();
//        for (String topic : topics) LOGGER.error(topic);
        int qos = foaProperties.getMqttServiceConfig().getQos();
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId, mqttClientFactory(), this.topics.toArray(new String[0]));
        adapter.setCompletionTimeout(5000); // TODO: read from properties file
        adapter.setRecoveryInterval(5000); // the interval at which the adapter will attempt to reconnect after a failure
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannelName("mqttInputChannel");
//        adapter.setErrorChannelName("mqttErrorChannel");
        return adapter;
    }


    public void refreshTopicsAndMqttSubscriber() {
        MqttPahoMessageDrivenChannelAdapter adapter = (MqttPahoMessageDrivenChannelAdapter) applicationContext
                .getBean("mqttInboundBean");
        List<String> oldTopics = this.topics;
        this.topics = prepareTopics();
        if (!oldTopics.equals(this.topics)) {
            adapter.removeTopic(oldTopics.toArray(new String[0]));
            adapter.addTopic(this.topics.toArray(new String[0]));
            LOGGER.info("refreshed mqtt topics and subscriber");
        }
    }

    private List<String> prepareTopics() {

        List<String> allTopics = new ArrayList<>();
        for (String house : mqttTopics.getHouses()) {

            List<String> deviceStatusTopics = new ArrayList<>();
            for (String node : mqttTopics.getDeviceStatusNodes()) {
                for (String object : mqttTopics.getDeviceStatusObjects()) {
                    for (String attribute : mqttTopics.getDeviceStatusAttributes()) {
                        deviceStatusTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            allTopics.addAll(deviceStatusTopics);


            List<String> ambientTemperatureTopics = new ArrayList<>();
            for (String node : mqttTopics.getAmbientTemperatureNodes()) {
                for (String object : mqttTopics.getAmbientTemperatureObjects()) {
                    for (String attribute : mqttTopics.getAmbientTemperatureAttributes()) {
                        ambientTemperatureTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            allTopics.addAll(ambientTemperatureTopics);


            List<String> boilerTemperatureTopics = new ArrayList<>();
            for (String node : mqttTopics.getBoilerTemperatureNodes()) {
                for (String object : mqttTopics.getBoilerTemperatureObjects()) {
                    for (String attribute : mqttTopics.getBoilerTemperatureAttributes()) {
                        boilerTemperatureTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            allTopics.addAll(boilerTemperatureTopics);

            List<String> devicePowerMetersTopics = new ArrayList<>();
            for (String node : mqttTopics.getDevicePowerMetersNodes()) {
                for (String object : mqttTopics.getDevicePowerMetersObjects()) {
                    for (String attribute : mqttTopics.getDevicePowerMetersAttributes()) {
                        devicePowerMetersTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            allTopics.addAll(devicePowerMetersTopics);
        }

        return allTopics;
    }


    /**
     * Message handler gets the messages from the subscribed topics.
     *
     * @return message is the numMessages amount
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    private MessageHandler messageHandler() {
        return message -> {
            try {
                parseMqttMessage(message);
            } catch (Exception e) {
                LOGGER.error("Error in messageHandler(): " + Arrays.toString(e.getStackTrace()));
            }
        };
    }


    /**
     * parse the received message from mqtt input channel and adds the message to {@code mqttDataMap}
     *
     * @param message
     */
    private void parseMqttMessage(Message message) throws IOException, IndexOutOfBoundsException {

        //displayMqttMessage(message);

        String receivedTopic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();

        String houseId = receivedTopic.split("/")[1];
        this.mqttDataMap.computeIfAbsent(houseId, k -> new HashMap<>());

        String paramType = receivedTopic.split("/")[3];
        String paramSubType = receivedTopic.split("/")[5];

        // parse message payload to extract data
        String paramName = paramType.concat("_").concat(paramSubType);
        this.mqttDataMap.get(houseId).computeIfAbsent(paramName, k -> new HashMap<>());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(message.getPayload().toString());
        double value = Math.max(0D,jsonNode.get("value").asDouble());

        String attribute = receivedTopic.split("/")[7];

        this.mqttDataMap.get(houseId).get(paramName).put(attribute, value);
        this.mqttDataMap.get(houseId).get(paramName).put("timestamp", new Date());
    }


    /**
     * Receive {@link MqttSubscribedEvent} event, which are published when a topic is subscribed.
     *
     * @return the event message
     */
    @Bean
    public ApplicationListener<MqttSubscribedEvent> subscribedEventListener() {
        //return event -> event.getMessage();
        return MqttSubscribedEvent::getMessage;
    }

    private void displayMqttMessage(Message message) {
        LOGGER.info("");
        LOGGER.info("Received a message from MQTT");
        LOGGER.info(message.getHeaders().get("mqtt_receivedTopic").toString());
        LOGGER.info(message.getPayload().toString());
        LOGGER.info("");
        LOGGER.info("");
    }

}

