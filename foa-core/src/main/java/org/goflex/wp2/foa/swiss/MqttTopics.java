package org.goflex.wp2.foa.swiss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.foa.config.FOAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MqttTopics {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttTopics.class);

    @Autowired
    private FOAProperties foaProperties;

    @Autowired
    private RestTemplate restTemplate;

    private List<String> houses;

    private List<String> deviceStatusNodes;
    private List<String> deviceStatusObjects;
    private List<String> deviceStatusAttributes;

    private List<String> ambientTemperatureNodes;
    private List<String> ambientTemperatureObjects;
    private List<String> ambientTemperatureAttributes;

    private List<String> boilerTemperatureNodes;
    private List<String> boilerTemperatureObjects;
    private List<String> boilerTemperatureAttributes;

    private List<String> devicePowerMetersNodes;
    private List<String> devicePowerMetersObjects;
    private List<String> devicePowerMetersAttributes;


    @Autowired
    private void prepareTopicVariables() {
        setHouses(fetchHouses());

        setDeviceStatusNodes(foaProperties.getMqttServiceConfig().getDeviceStatus().getNodes());
        setDeviceStatusObjects(foaProperties.getMqttServiceConfig().getDeviceStatus().getObjects());
        setDeviceStatusAttributes(foaProperties.getMqttServiceConfig().getDeviceStatus().getAttributes());

        setAmbientTemperatureNodes(foaProperties.getMqttServiceConfig().getAmbientTemperature().getNodes());
        setAmbientTemperatureObjects(foaProperties.getMqttServiceConfig().getAmbientTemperature().getObjects());
        setAmbientTemperatureAttributes(foaProperties.getMqttServiceConfig().getAmbientTemperature().getAttributes());

        setBoilerTemperatureNodes(foaProperties.getMqttServiceConfig().getBoilerTemperature().getNodes());
        setBoilerTemperatureObjects(foaProperties.getMqttServiceConfig().getBoilerTemperature().getObjects());
        setBoilerTemperatureAttributes(foaProperties.getMqttServiceConfig().getBoilerTemperature().getAttributes());

        setDevicePowerMetersNodes(foaProperties.getMqttServiceConfig().getDevicePowerMeters().getNodes());
        setDevicePowerMetersObjects(foaProperties.getMqttServiceConfig().getDevicePowerMeters().getObjects());
        setDevicePowerMetersAttributes(foaProperties.getMqttServiceConfig().getDevicePowerMeters().getAttributes());

    }

    private List<String> fetchHouses() {

        try {
            ResponseEntity<String> response = restTemplate.exchange(foaProperties.getMqttServiceConfig().getHouseListUrl(), HttpMethod.GET, null, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.getBody());
            List<String> houses = new ArrayList<>();
            jsonNode.forEach(node -> {
                houses.add(node.get("house").asText());
            });

            return houses;

        } catch (HttpClientErrorException e) {
            LOGGER.error("Error retrieving house list from swiss cloud: " + e.getMessage());
            LOGGER.error("Response body: " + e.getResponseBodyAsString());
            LOGGER.error("Response status code: " + e.getStatusCode());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }

    public List<String> getHouses() {
        //return houses;
        return this.fetchHouses();
    }

    public void setHouses(List<String> houses) {
        this.houses = houses;
    }

    public List<String> getDeviceStatusNodes() {
        return deviceStatusNodes;
    }

    public void setDeviceStatusNodes(List<String> deviceStatusNodes) {
        this.deviceStatusNodes = deviceStatusNodes;
    }

    public List<String> getDeviceStatusObjects() {
        return deviceStatusObjects;
    }

    public void setDeviceStatusObjects(List<String> deviceStatusObjects) {
        this.deviceStatusObjects = deviceStatusObjects;
    }

    public List<String> getDeviceStatusAttributes() {
        return deviceStatusAttributes;
    }

    public void setDeviceStatusAttributes(List<String> deviceStatusAttributes) {
        this.deviceStatusAttributes = deviceStatusAttributes;
    }

    public List<String> getAmbientTemperatureNodes() {
        return ambientTemperatureNodes;
    }

    public void setAmbientTemperatureNodes(List<String> ambientTemperatureNodes) {
        this.ambientTemperatureNodes = ambientTemperatureNodes;
    }

    public List<String> getAmbientTemperatureObjects() {
        return ambientTemperatureObjects;
    }

    public void setAmbientTemperatureObjects(List<String> ambientTemperatureObjects) {
        this.ambientTemperatureObjects = ambientTemperatureObjects;
    }

    public List<String> getAmbientTemperatureAttributes() {
        return ambientTemperatureAttributes;
    }

    public void setAmbientTemperatureAttributes(List<String> ambientTemperatureAttributes) {
        this.ambientTemperatureAttributes = ambientTemperatureAttributes;
    }

    public List<String> getBoilerTemperatureNodes() {
        return boilerTemperatureNodes;
    }

    public void setBoilerTemperatureNodes(List<String> boilerTemperatureNodes) {
        this.boilerTemperatureNodes = boilerTemperatureNodes;
    }

    public List<String> getBoilerTemperatureObjects() {
        return boilerTemperatureObjects;
    }

    public void setBoilerTemperatureObjects(List<String> boilerTemperatureObjects) {
        this.boilerTemperatureObjects = boilerTemperatureObjects;
    }

    public List<String> getBoilerTemperatureAttributes() {
        return boilerTemperatureAttributes;
    }

    public void setBoilerTemperatureAttributes(List<String> boilerTemperatureAttributes) {
        this.boilerTemperatureAttributes = boilerTemperatureAttributes;
    }

    public List<String> getDevicePowerMetersNodes() {
        return devicePowerMetersNodes;
    }

    public void setDevicePowerMetersNodes(List<String> devicePowerMetersNodes) {
        this.devicePowerMetersNodes = devicePowerMetersNodes;
    }

    public List<String> getDevicePowerMetersObjects() {
        return devicePowerMetersObjects;
    }

    public void setDevicePowerMetersObjects(List<String> devicePowerMetersObjects) {
        this.devicePowerMetersObjects = devicePowerMetersObjects;
    }

    public List<String> getDevicePowerMetersAttributes() {
        return devicePowerMetersAttributes;
    }

    public void setDevicePowerMetersAttributes(List<String> devicePowerMetersAttributes) {
        this.devicePowerMetersAttributes = devicePowerMetersAttributes;
    }
}
