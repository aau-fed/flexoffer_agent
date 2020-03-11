/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018 The GoFlex Consortium
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 3/19/18 8:49 PM
 */

package org.goflex.wp2.foa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by bijay on 3/19/18.
 */
@Configuration
@ConfigurationProperties(prefix = "foa")
public class FOAProperties {
    private String FOAId;
    private String CloudAPIUrl;
    private int getNoOfUnsuccessfulCon;
    private int connectionHaltDuration;
    private FOGConnectionConfig fogConnectionConfig;
    private FMANConnectionConfig fmanConnectionConfig;
    private SimulatedDeviceServiceConfig simulatedDeviceServiceConfig;
    private MqttServiceConfig mqttServiceConfig;

    public FOGConnectionConfig getFogConnectionConfig() {
        return fogConnectionConfig;
    }

    public void setFogConnectionConfig(FOGConnectionConfig fogConnectionConfig) {
        this.fogConnectionConfig = fogConnectionConfig;
    }

    public SimulatedDeviceServiceConfig getSimulatedDeviceServiceConfig() {
        return simulatedDeviceServiceConfig;
    }

    public void setSimulatedDeviceServiceConfig(SimulatedDeviceServiceConfig simulatedDeviceServiceConfig) {
        this.simulatedDeviceServiceConfig = simulatedDeviceServiceConfig;
    }

    public MqttServiceConfig getMqttServiceConfig() {
        return mqttServiceConfig;
    }

    public void setMqttServiceConfig(MqttServiceConfig mqttServiceConfig) {
        this.mqttServiceConfig = mqttServiceConfig;
    }

    public String getFOAId() {
        return FOAId;
    }

    public void setFOAId(String FOAId) {
        this.FOAId = FOAId;
    }

    public String getCloudAPIUrl() {
        return CloudAPIUrl;
    }

    public void setCloudAPIUrl(String cloudAPIUrl) {
        CloudAPIUrl = cloudAPIUrl;
    }

    public FMANConnectionConfig getFmanConnectionConfig() {
        return fmanConnectionConfig;
    }

    public void setFmanConnectionConfig(FMANConnectionConfig fmanConnectionConfig) {
        this.fmanConnectionConfig = fmanConnectionConfig;
    }

    public int getGetNoOfUnsuccessfulCon() {
        return getNoOfUnsuccessfulCon;
    }

    public void setGetNoOfUnsuccessfulCon(int getNoOfUnsuccessfulCon) {
        this.getNoOfUnsuccessfulCon = getNoOfUnsuccessfulCon;
    }

    public int getConnectionHaltDuration() {
        return connectionHaltDuration;
    }

    public void setConnectionHaltDuration(int connectionHaltDuration) {
        this.connectionHaltDuration = connectionHaltDuration;
    }

    public static class FOGConnectionConfig {
        private String generateDeviceFOUrl;
        private String shouldGenerateDeviceFOUrl;

        public String getGenerateDeviceFOUrl() {
            return generateDeviceFOUrl;
        }

        public void setGenerateDeviceFOUrl(String generateDeviceFOUrl) {
            this.generateDeviceFOUrl = generateDeviceFOUrl;
        }

        public String getShouldGenerateDeviceFOUrl() {
            return shouldGenerateDeviceFOUrl;
        }

        public void setShouldGenerateDeviceFOUrl(String shouldGenerateDeviceFOUrl) {
            this.shouldGenerateDeviceFOUrl = shouldGenerateDeviceFOUrl;
        }
    }

    public static class FMANConnectionConfig {

        private String FmanProxyUrl;
        private String URItoSendFO;
        private String HeartbeatURI;
        private String MeasurementURI;
        private String LoginURI;
        private String RegisterURI;
        private String RegisterAllUsersURI;
        private String GetAllUsersURI;
        private String ContractURI;
        private String BillURI;

        public String getFmanProxyUrl() {
            return FmanProxyUrl;
        }

        public void setFmanProxyUrl(String fmanProxyUrl) {
            FmanProxyUrl = fmanProxyUrl;
        }

        public String getURItoSendFO() {
            return this.getFmanProxyUrl() + URItoSendFO;
        }

        public void setURItoSendFO(String URItoSendFO) {
            this.URItoSendFO = URItoSendFO;
        }

        public String getHeartbeatURI() {
            return this.getFmanProxyUrl() + HeartbeatURI;
        }

        public void setHeartbeatURI(String heartbeatURI) {
            HeartbeatURI = heartbeatURI;
        }

        public String getMeasurementURI() {
            return this.getFmanProxyUrl() + MeasurementURI;
        }

        public void setMeasurementURI(String measurementURI) {
            MeasurementURI = measurementURI;
        }

        public String getLoginURI() {
            return this.getFmanProxyUrl() + LoginURI;
        }

        public void setLoginURI(String loginURI) {
            LoginURI = loginURI;
        }

        public String getRegisterURI() {
            return this.getFmanProxyUrl() + RegisterURI;
        }

        public void setRegisterURI(String registerURI) {
            RegisterURI = registerURI;
        }

        public String getContractURI() {
            return this.getFmanProxyUrl() + ContractURI;
        }

        public void setContractURI(String contractURI) {
            ContractURI = contractURI;
        }

        public String getBillURI() {
            return this.getFmanProxyUrl() + BillURI;
        }

        public void setBillURI(String billURI) {
            BillURI = billURI;
        }

        public String getGetAllUsersURI() {
            return this.getFmanProxyUrl() + GetAllUsersURI;
        }

        public void setGetAllUsersURI(String getAllUsersURI) {
            GetAllUsersURI = getAllUsersURI;
        }

        public String getRegisterAllUsersURI() {
            return this.getFmanProxyUrl() + RegisterAllUsersURI;
        }

        public void setRegisterAllUsersURI(String registerAllUsersURI) {
            RegisterAllUsersURI = registerAllUsersURI;
        }
    }

    public static class SimulatedDeviceServiceConfig {
        private String baseURI;
        private String usersUri;
        private String newTokenUri;
        private String devicesUri;
        private String startDeviceUri;
        private String stopDeviceUri;
        private String devicePowerUri;
        private String deviceEnergyUri;
        private String deviceStatusUri;
        private String deviceConsumptionUri;
        private String apiToken;
        private String user;
        private String password;

        public String getBaseURI() {
            return baseURI;
        }

        public void setBaseURI(String baseURI) {
            this.baseURI = baseURI;
        }

        public String getUsersUri() {
            return usersUri;
        }

        public void setUsersUri(String usersUri) {
            this.usersUri = usersUri;
        }

        public String getNewTokenUri() {
            return newTokenUri;
        }

        public void setNewTokenUri(String newTokenUri) {
            this.newTokenUri = newTokenUri;
        }

        public String getDevicesUri() {
            return devicesUri;
        }

        public void setDevicesUri(String devicesUri) {
            this.devicesUri = devicesUri;
        }

        public String getStartDeviceUri() {
            return startDeviceUri;
        }

        public void setStartDeviceUri(String startDeviceUri) {
            this.startDeviceUri = startDeviceUri;
        }

        public String getStopDeviceUri() {
            return stopDeviceUri;
        }

        public void setStopDeviceUri(String stopDeviceUri) {
            this.stopDeviceUri = stopDeviceUri;
        }

        public String getDevicePowerUri() {
            return devicePowerUri;
        }

        public void setDevicePowerUri(String devicePowerUri) {
            this.devicePowerUri = devicePowerUri;
        }

        public String getDeviceEnergyUri() {
            return deviceEnergyUri;
        }

        public void setDeviceEnergyUri(String deviceEnergyUri) {
            this.deviceEnergyUri = deviceEnergyUri;
        }

        public String getDeviceStatusUri() {
            return deviceStatusUri;
        }

        public void setDeviceStatusUri(String deviceStatusUri) {
            this.deviceStatusUri = deviceStatusUri;
        }

        public String getDeviceConsumptionUri() {
            return deviceConsumptionUri;
        }

        public void setDeviceConsumptionUri(String deviceConsumptionUri) {
            this.deviceConsumptionUri = deviceConsumptionUri;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    public static class MqttServiceConfig {
        private String foaAccountUser;
        private String foaAccountPassword;
        private String user;
        private String password;
        private String clientId;
        private String host;
        private int port;
        private int waitTime;
        private int qos;
        private String trustStore;
        private String trustStorePassword;
        private String houseListUrl;
        private List<String> houses;
        private DeviceStatus deviceStatus;
        private DeviceControl deviceControl;
        private AmbientTemperature ambientTemperature;
        private BoilerTemperature boilerTemperature;
        private DevicePowerMeters devicePowerMeters;

        public String getFoaAccountUser() {
            return foaAccountUser;
        }

        public void setFoaAccountUser(String foaAccountUser) {
            this.foaAccountUser = foaAccountUser;
        }

        public String getFoaAccountPassword() {
            return foaAccountPassword;
        }

        public void setFoaAccountPassword(String foaAccountPassword) {
            this.foaAccountPassword = foaAccountPassword;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getWaitTime() {
            return waitTime;
        }

        public void setWaitTime(int waitTime) {
            this.waitTime = waitTime;
        }

        public int getQos() {
            return qos;
        }

        public void setQos(int qos) {
            this.qos = qos;
        }

        public String getTrustStore() {
            return trustStore;
        }

        public void setTrustStore(String trustStore) {
            this.trustStore = trustStore;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public String getHouseListUrl() {
            return houseListUrl;
        }

        public void setHouseListUrl(String houseListUrl) {
            this.houseListUrl = houseListUrl;
        }

        public List<String> getHouses() {
            return houses;
        }

        public void setHouses(List<String> houses) {
            this.houses = houses;
        }

        public DeviceStatus getDeviceStatus() {
            return deviceStatus;
        }

        public void setDeviceStatus(DeviceStatus deviceStatus) {
            this.deviceStatus = deviceStatus;
        }

        public DeviceControl getDeviceControl() {
            return deviceControl;
        }

        public void setDeviceControl(DeviceControl deviceControl) {
            this.deviceControl = deviceControl;
        }

        public AmbientTemperature getAmbientTemperature() {
            return ambientTemperature;
        }

        public void setAmbientTemperature(AmbientTemperature ambientTemperature) {
            this.ambientTemperature = ambientTemperature;
        }

        public BoilerTemperature getBoilerTemperature() {
            return boilerTemperature;
        }

        public void setBoilerTemperature(BoilerTemperature boilerTemperature) {
            this.boilerTemperature = boilerTemperature;
        }

        public DevicePowerMeters getDevicePowerMeters() {
            return devicePowerMeters;
        }

        public void setDevicePowerMeters(DevicePowerMeters devicePowerMeters) {
            this.devicePowerMeters = devicePowerMeters;
        }

        public static class DeviceStatus {
            private List<String> nodes;
            private List<String> objects;
            private List<String> attributes;

            public List<String> getNodes() {
                return nodes;
            }

            public void setNodes(List<String> nodes) {
                this.nodes = nodes;
            }

            public List<String> getObjects() {
                return objects;
            }

            public void setObjects(List<String> objects) {
                this.objects = objects;
            }

            public List<String> getAttributes() {
                return attributes;
            }

            public void setAttributes(List<String> attributes) {
                this.attributes = attributes;
            }
        }

        public static class DeviceControl {
            private List<String> nodes;
            private List<String> objects;
            private List<String> attributes;

            public List<String> getNodes() {
                return nodes;
            }

            public void setNodes(List<String> nodes) {
                this.nodes = nodes;
            }

            public List<String> getObjects() {
                return objects;
            }

            public void setObjects(List<String> objects) {
                this.objects = objects;
            }

            public List<String> getAttributes() {
                return attributes;
            }

            public void setAttributes(List<String> attributes) {
                this.attributes = attributes;
            }
        }

        public static class AmbientTemperature {
            private List<String> nodes;
            private List<String> objects;
            private List<String> attributes;

            public List<String> getNodes() {
                return nodes;
            }

            public void setNodes(List<String> nodes) {
                this.nodes = nodes;
            }

            public List<String> getObjects() {
                return objects;
            }

            public void setObjects(List<String> objects) {
                this.objects = objects;
            }

            public List<String> getAttributes() {
                return attributes;
            }

            public void setAttributes(List<String> attributes) {
                this.attributes = attributes;
            }
        }

        public static class BoilerTemperature {
            private List<String> nodes;
            private List<String> objects;
            private List<String> attributes;

            public List<String> getNodes() {
                return nodes;
            }

            public void setNodes(List<String> nodes) {
                this.nodes = nodes;
            }

            public List<String> getObjects() {
                return objects;
            }

            public void setObjects(List<String> objects) {
                this.objects = objects;
            }

            public List<String> getAttributes() {
                return attributes;
            }

            public void setAttributes(List<String> attributes) {
                this.attributes = attributes;
            }
        }

        public static class DevicePowerMeters {
            private List<String> nodes;
            private List<String> objects;
            private List<String> attributes;

            public List<String> getNodes() {
                return nodes;
            }

            public void setNodes(List<String> nodes) {
                this.nodes = nodes;
            }

            public List<String> getObjects() {
                return objects;
            }

            public void setObjects(List<String> objects) {
                this.objects = objects;
            }

            public List<String> getAttributes() {
                return attributes;
            }

            public void setAttributes(List<String> attributes) {
                this.attributes = attributes;
            }
        }

    }
}
