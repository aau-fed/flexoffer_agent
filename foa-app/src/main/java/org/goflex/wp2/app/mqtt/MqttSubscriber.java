package org.goflex.wp2.app.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.mqtt.MqttTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(value="mqtt.cloud.enabled", havingValue = "true")
public class MqttSubscriber {

    private final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriber.class);

    @Resource(name = "mqttDataMap")
    private ConcurrentHashMap<String, Map<String, Map<String, Object>>> mqttDataMap;

    private final MqttTopics mqttTopics;
    private final FOAProperties foaProperties;
    private List<String> topics;

    private MqttClient client;

    @Autowired
    public MqttSubscriber(MqttTopics mqttTopics, FOAProperties foaProperties) {
        this.mqttTopics = mqttTopics;
        this.foaProperties = foaProperties;
        this.topics = new ArrayList<>();
        this.startSubscriber();
    }


    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setServerURIs(new String[]{foaProperties.getMqttServiceConfig().getHost()});
        mqttConnectOptions.setUserName(foaProperties.getMqttServiceConfig().getUser());
        mqttConnectOptions.setPassword(foaProperties.getMqttServiceConfig().getPassword().toCharArray());
        // if false, the adapter will keep the subscription active so that messages arriving while the adapter
        // is stopped will be delivered on the next start.
        mqttConnectOptions.setCleanSession(true);

        mqttConnectOptions.setAutomaticReconnect(true);

        Properties sslProps = new Properties();
        sslProps.setProperty("com.ibm.ssl.keyStoreType", "JKS");
        sslProps.setProperty("com.ibm.ssl.trustStore", foaProperties.getMqttServiceConfig().getTrustStore());
        sslProps.setProperty("com.ibm.ssl.trustStorePassword", foaProperties.getMqttServiceConfig().getTrustStorePassword());

        mqttConnectOptions.setSSLProperties(sslProps);
        return mqttConnectOptions;
    }

    private void startSubscriber() {
        try {

            String mqttHost = this.foaProperties.getMqttServiceConfig().getHost();
            if (mqttHost == null || mqttHost.equals(""))
            {
                LOGGER.warn("MQTT server url not valid");
                return;
            }

            // You can send an empty ClientId, if you don’t need a state to be held by the broker.
            // The empty ClientID results in a connection without any state.
            // In this case, the clean session flag must be set to true or the broker will reject the connection.
            client = new MqttClient(mqttHost, MqttClient.generateClientId(), new MemoryPersistence());

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                    LOGGER.error("Connection to MQTT lost");
                    LOGGER.error(cause.toString());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                        parseMqttMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                    LOGGER.info("Mqtt message published");
                }
            });

            client.connect(getMqttConnectOptions());
            subscribeToTopicForEachHouse();
        } catch (MqttException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void subscribeToTopicForEachHouse() {
        for (String house : mqttTopics.getHouses()) {

            List<String> houseTopics = this.prepareHouseTopics(house);
            final String[] topics = new String[houseTopics.size()];
            final int[] qos = new int[houseTopics.size()];
            for (int j = 0; j < houseTopics.size(); j++) {
                topics[j] = houseTopics.get(j);
                qos[j] = 0;
            }

            try {
                client.subscribe(topics, qos);
                //client.subscribe(houseTopics.toArray(new String[0]));
                LOGGER.info(String.format("Subscribed to mqtt topics for house: %s", house));
                this.topics.addAll(Arrays.asList(topics));
            } catch (MqttException e) {
                LOGGER.info(String.format("Error subscribing to mqtt topics for house: %s", house));
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void refreshTopicsAndMqttSubscriber() throws MqttException {
        List<String> oldTopics = this.topics;
        this.topics = prepareTopicsSpecific();
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        if (!oldTopics.equals(this.topics) || (hour == 0 && minute == 0)) {
            client.unsubscribe(oldTopics.toArray(new String[0]));
            subscribeToTopicForEachHouse();
            //client.subscribe(this.topics.toArray(new String[0]));
            LOGGER.info("refreshed mqtt topics and subscriber");
        }
    }

    private List<String> prepareTopicsAll() {
        List<String> allTopics = new ArrayList<>();
        for (String house : mqttTopics.getHouses()) {
            allTopics.add("@update/" + house + "/nodes/#");
        }
        return allTopics;
    }

    private List<String> prepareHouseTopics(String house) {
        List<String> houseTopics = new ArrayList<>();

        List<String> deviceStatusTopics = new ArrayList<>();
        for (String node : mqttTopics.getDeviceStatusNodes()) {
            for (String object : mqttTopics.getDeviceStatusObjects()) {
                for (String attribute : mqttTopics.getDeviceStatusAttributes()) {
                    deviceStatusTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                }
            }
        }
        houseTopics.addAll(deviceStatusTopics);

        List<String> ambientTemperatureTopics = new ArrayList<>();
        for (String node : mqttTopics.getAmbientTemperatureNodes()) {
            for (String object : mqttTopics.getAmbientTemperatureObjects()) {
                for (String attribute : mqttTopics.getAmbientTemperatureAttributes()) {
                    ambientTemperatureTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                }
            }
        }
        houseTopics.addAll(ambientTemperatureTopics);

        List<String> boilerTemperatureTopics = new ArrayList<>();
        for (String node : mqttTopics.getBoilerTemperatureNodes()) {
            for (String object : mqttTopics.getBoilerTemperatureObjects()) {
                for (String attribute : mqttTopics.getBoilerTemperatureAttributes()) {
                    boilerTemperatureTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                }
            }
        }
        houseTopics.addAll(boilerTemperatureTopics);

        List<String> devicePowerMetersTopics = new ArrayList<>();
        for (String node : mqttTopics.getDevicePowerMetersNodes()) {
            for (String object : mqttTopics.getDevicePowerMetersObjects()) {
                for (String attribute : mqttTopics.getDevicePowerMetersAttributes()) {
                    devicePowerMetersTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                }
            }
        }
        houseTopics.addAll(devicePowerMetersTopics);
        return houseTopics;
    }

    private List<String> prepareTopicsSpecific() {
        List<String> specificTopics = new ArrayList<>();
        for (String house : mqttTopics.getHouses()) {
            List<String> deviceStatusTopics = new ArrayList<>();
            for (String node : mqttTopics.getDeviceStatusNodes()) {
                for (String object : mqttTopics.getDeviceStatusObjects()) {
                    for (String attribute : mqttTopics.getDeviceStatusAttributes()) {
                        deviceStatusTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            specificTopics.addAll(deviceStatusTopics);

            List<String> ambientTemperatureTopics = new ArrayList<>();
            for (String node : mqttTopics.getAmbientTemperatureNodes()) {
                for (String object : mqttTopics.getAmbientTemperatureObjects()) {
                    for (String attribute : mqttTopics.getAmbientTemperatureAttributes()) {
                        ambientTemperatureTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            specificTopics.addAll(ambientTemperatureTopics);

            List<String> boilerTemperatureTopics = new ArrayList<>();
            for (String node : mqttTopics.getBoilerTemperatureNodes()) {
                for (String object : mqttTopics.getBoilerTemperatureObjects()) {
                    for (String attribute : mqttTopics.getBoilerTemperatureAttributes()) {
                        boilerTemperatureTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            specificTopics.addAll(boilerTemperatureTopics);

            List<String> devicePowerMetersTopics = new ArrayList<>();
            for (String node : mqttTopics.getDevicePowerMetersNodes()) {
                for (String object : mqttTopics.getDevicePowerMetersObjects()) {
                    for (String attribute : mqttTopics.getDevicePowerMetersAttributes()) {
                        devicePowerMetersTopics.add("@update/" + house + "/nodes/" + node + "/objects/" + object + "/attributes/" + attribute);
                    }
                }
            }
            specificTopics.addAll(devicePowerMetersTopics);
        }
        return specificTopics;
    }


    /**
     * parse the received message from mqtt input channel and adds the message to {@code mqttDataMap}
     *
     * @param message
     */
    private void parseMqttMessage(String topic, MqttMessage message) {

        try {

            if (this.mqttDataMap == null) {
                return;
            }

            //displayMqttMessage(message);

            String houseId = topic.split("/")[1];
            this.mqttDataMap.computeIfAbsent(houseId, k -> new HashMap<>());

            String paramType = topic.split("/")[3];
            String paramSubType = topic.split("/")[5];

            // parse message payload to extract data
            String paramName = paramType.concat("_").concat(paramSubType);
            this.mqttDataMap.get(houseId).computeIfAbsent(paramName, k -> new HashMap<>());

            ObjectMapper mapper = new ObjectMapper();

            JsonNode jsonNode = mapper.readTree(message.toString());
            double value = Math.max(0D,jsonNode.get("value").asDouble());

            String attribute = topic.split("/")[7];

            this.mqttDataMap.get(houseId).get(paramName).put(attribute, value);
            this.mqttDataMap.get(houseId).get(paramName).put("timestamp", new Date());

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
}


    private void displayMqttMessage(MqttMessage message) {
        LOGGER.info("");
        LOGGER.info("Received a message from MQTT");
        LOGGER.info(message.toString());
        LOGGER.info("");
        LOGGER.info("");
    }
}

