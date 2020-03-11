package org.goflex.wp2.app;

import org.goflex.wp2.core.entities.DeviceDetailData;
import org.goflex.wp2.core.entities.DeviceState;
import org.goflex.wp2.core.entities.OrganizationLoadControlState;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.ContractRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.repository.UserRepository;
import org.goflex.wp2.foa.config.FOAProperties;
import org.goflex.wp2.foa.implementation.OrganizationalConsumptionService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.app"})
@EnableJpaRepositories(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.app"})
@IntegrationComponentScan({"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.app"})
@EnableRetry
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    @Resource(name = "directControlMode")
    ConcurrentHashMap<Long, OrganizationLoadControlState> directControlMode;

    @Resource(name = "defaultFlexibilitySettings")
    Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings;

    @Resource(name = "orgAccEnergyData")
    LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData;

    private final UserRepository userRepository;
    private final UserService userService;
    private final DeviceDetailService deviceDetailService;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final OrganizationalConsumptionService organizationalConsumptionService;

    @Autowired
    public ApplicationConfig(UserRepository userRepository, UserService userService,
                             DeviceDetailService deviceDetailService, ContractRepository contractRepository,
                             PasswordEncoder passwordEncoder,
                             OrganizationRepository organizationRepository,
                             OrganizationalConsumptionService organizationalConsumptionService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.deviceDetailService = deviceDetailService;
        this.contractRepository = contractRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.organizationalConsumptionService = organizationalConsumptionService;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean(name = "InitializingBean")
    InitializingBean doInitialization() {
        return () -> {


            if (organizationRepository.findByOrganizationName("AAU") == null) {
                Organization orgAAU = new Organization();
                orgAAU.setOrganizationId(10001);
                orgAAU.setOrganizationName("AAU");
                orgAAU.setDirectControlMode(OrganizationLoadControlState.Active);
                organizationRepository.save(orgAAU);
            }

            if (organizationRepository.findByOrganizationName("SWISS") == null) {
                Organization orgSWISS = new Organization();
                orgSWISS.setOrganizationId(10004);
                orgSWISS.setOrganizationName("SWISS");
                orgSWISS.setDirectControlMode(OrganizationLoadControlState.Paused);
                organizationRepository.save(orgSWISS);
            }

            if (organizationRepository.findByOrganizationName("SWW") == null) {
                Organization orgSWW = new Organization();
                orgSWW.setOrganizationId(10006);
                orgSWW.setOrganizationName("SWW");
                orgSWW.setDirectControlMode(OrganizationLoadControlState.Paused);
                organizationRepository.save(orgSWW);
            }

            if (organizationRepository.findByOrganizationName("CYPRUS") == null) {
                Organization orgCYPRUS = new Organization();
                orgCYPRUS.setOrganizationId(10007);
                orgCYPRUS.setOrganizationName("CYPRUS");
                orgCYPRUS.setDirectControlMode(OrganizationLoadControlState.Paused);
                organizationRepository.save(orgCYPRUS);
            }

            List<Organization> organizations = organizationRepository.findAll();
            for (Organization organization : organizations) {
                directControlMode.put(organization.getOrganizationId(), organization.getDirectControlMode());
            }
            for (Organization organization : organizations
            ) {
                String userName = organization.getOrganizationName().toLowerCase() + "Admin";
                if (userService.getUser(userName) == null) {
                    // add user for swiss case if not exists
                    String userPass = "";
                    UserT adminUser = new UserT();
                    adminUser.setUserName(userName);
                    adminUser.setPassword(passwordEncoder.encode(userPass));
                    adminUser.setRole(UserRole.ROLE_ADMIN);
                    UserAddress address = new UserAddress();
                    adminUser.setUserAddress(address);
                    adminUser.setRegistrationDate(new Date());
                    adminUser.setOrganization(organization.getOrganizationId());
                    adminUser.setEmail(userName + "@please-set.email");
                    userService.save(adminUser);
                }

                userName = organization.getOrganizationName().toLowerCase() + "";
                if (userService.getUser(userName) == null) {
                    // add user for swiss case if not exists
                    String userPass = "";
                    UserT adminUser = new UserT();
                    adminUser.setUserName(userName);
                    adminUser.setPassword(passwordEncoder.encode(userPass));
                    adminUser.setRole(UserRole.ROLE_ADMIN);
                    UserAddress address = new UserAddress();
                    adminUser.setUserAddress(address);
                    adminUser.setRegistrationDate(new Date());
                    adminUser.setOrganization(organization.getOrganizationId());
                    adminUser.setEmail(userName + "@please-set.email");
                    userService.save(adminUser);
                }
            }

            // add admin user if not exists
            String adminUserName = "";
            String adminUserPass = "";
            Organization org = organizationRepository.findByOrganizationName("AAU");
            if (userService.getUser("") == null) {
                UserT userAdmin = new UserT();
                userAdmin.setUserName(adminUserName);
                userAdmin.setPassword(passwordEncoder.encode(adminUserPass));
                userAdmin.setRole(UserRole.ROLE_ADMIN);
                UserAddress address = new UserAddress();
                userAdmin.setUserAddress(address);
                userAdmin.setOrganization(org.getOrganizationId());
                userAdmin.setRegistrationDate(new Date());
                userAdmin.setOrganization(organizationRepository.findByOrganizationName("AAU").getOrganizationId());
                userAdmin.setEmail(adminUserName + "@please-set.email");
                userService.save(userAdmin);
            }

            // for testing
            String prosumerUserName = "";
            String prosumerUserPass = "";
            if (userService.getUser(prosumerUserName) == null) {
                UserT userProsumer = new UserT();
                userProsumer.setUserName(prosumerUserName);
                userProsumer.setPassword(passwordEncoder.encode(prosumerUserPass));
                userProsumer.setRole(UserRole.ROLE_PROSUMER);
                userProsumer.setTpLinkUserName("");
                userProsumer.setTpLinkPassword("");
                userProsumer.setRegistrationDate(new Date());
                userProsumer.setOrganization(organizationRepository.findByOrganizationName("AAU").getOrganizationId());
                userProsumer.setEmail(prosumerUserName + "@please-set.email");
                userService.save(userProsumer);
            }

            // so that contract table is not empty
            if (contractRepository.count() == 0) {
                Contract contract = new Contract();
                contract.setUserName("");
                contract.setFixedReward(0.02);
                contract.setDefault(true);
                contract.setValid(true);
                contractRepository.saveAndFlush(contract);
            }

            // load cumulative power form db
            List<OrganizationalConsumption> cumulativeOrgEnergy =
                    this.organizationalConsumptionService.getCumulativeEnergyByOrganization();
            for (OrganizationalConsumption c : cumulativeOrgEnergy) {
                Map<Date, Double> data = new HashMap<>();
                data.put(c.getTimestamp(), c.getCumulativeEnergy());
                orgAccEnergyData.put(c.getOrganizationName(), data);
            }

            LOGGER.info("InitializingBean execution complete!");

        };

    }


    @Bean
    @DependsOn("InitializingBean")
    InitializingBean populateDefaultFlexibilityMap() {
        return () -> {
            organizationRepository.findAll().forEach(organization -> {
                if (organization.getOrganizationName().equals("SWW")) {
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceFlexibilityDetail.setDailyControlStart(0);
                    deviceFlexibilityDetail.setDailyControlEnd(24);
                    deviceFlexibilityDetail.setNoOfInterruptionInADay(6);
                    deviceFlexibilityDetail.setMaxInterruptionLength(1);
                    deviceFlexibilityDetail.setMinInterruptionInterval(8);
                    deviceFlexibilityDetail.setMaxInterruptionDelay(1);
                    deviceFlexibilityDetail.setLatestAcceptanceTime(1); // todo: FMAN throws this error: acceptanceBeforeInterval is after startBeforeInterval)
                    defaultFlexibilitySettings.put(organization.getOrganizationId(), deviceFlexibilityDetail);
                }

                if (organization.getOrganizationName().equals("SWISS")) {
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceFlexibilityDetail.setDailyControlStart(0);
                    deviceFlexibilityDetail.setDailyControlEnd(24);
                    deviceFlexibilityDetail.setNoOfInterruptionInADay(8);
                    deviceFlexibilityDetail.setMaxInterruptionLength(1);
                    deviceFlexibilityDetail.setMinInterruptionInterval(8);
                    deviceFlexibilityDetail.setMaxInterruptionDelay(1);
                    deviceFlexibilityDetail.setLatestAcceptanceTime(1);
                    defaultFlexibilitySettings.put(organization.getOrganizationId(), deviceFlexibilityDetail);
                }

                if (organization.getOrganizationName().equals("CYPRUS")) {
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceFlexibilityDetail.setDailyControlStart(0);
                    deviceFlexibilityDetail.setDailyControlEnd(24);
                    deviceFlexibilityDetail.setNoOfInterruptionInADay(1);
                    deviceFlexibilityDetail.setMaxInterruptionLength(1);
                    deviceFlexibilityDetail.setMinInterruptionInterval(8);
                    deviceFlexibilityDetail.setMaxInterruptionDelay(1);
                    deviceFlexibilityDetail.setLatestAcceptanceTime(1);
                    defaultFlexibilitySettings.put(organization.getOrganizationId(), deviceFlexibilityDetail);
                }

                if (organization.getOrganizationName().equals("AAU")) {
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceFlexibilityDetail.setDailyControlStart(0);
                    deviceFlexibilityDetail.setDailyControlEnd(24);
                    deviceFlexibilityDetail.setNoOfInterruptionInADay(8);
                    deviceFlexibilityDetail.setMaxInterruptionLength(4);
                    deviceFlexibilityDetail.setMinInterruptionInterval(6);
                    deviceFlexibilityDetail.setMaxInterruptionDelay(4);
                    deviceFlexibilityDetail.setLatestAcceptanceTime(1);
                    defaultFlexibilitySettings.put(organization.getOrganizationId(), deviceFlexibilityDetail);
                }
            });
        };

    }


    // for testing
    //@Bean
    //@Profile("test")
    //@DependsOn("InitializingBean")
    InitializingBean populateDatabase() {
        return () -> {
            LOGGER.info("profile is test, populating db with test devices and data...");

            // Aalborg Ny Kastetvej
            double lat = 57.0538549;
            double lon = 9.902846199999999;
            double smallStep = 0.001;
            int numDevices = 15;
            int numDataPoints = 100;

            UserT usr = userService.getUser("");
            List<DeviceDetail> deviceDetailList = userRepository.findDeviceList(usr.getUserName());
            //Set<DeviceDetail> deviceDetailSet = usr.getDeviceDetail();

            if (deviceDetailList.size() < numDevices) {
                for (int i = 1; i <= numDevices; i++) {
                    String deviceId = "testdevice" + i;
                    LOGGER.debug(deviceId);
                    DeviceDetail deviceDetail = new DeviceDetail(deviceId);
                    deviceDetail.setAlias("testdevice" + i);
                    ConsumptionTsEntity consumptionTs = new ConsumptionTsEntity();
                    deviceDetail.setConsumptionTs(consumptionTs);
                    GroupingDetail groupingDetail = new GroupingDetail();
                    deviceDetailService.addGroupingDetail(groupingDetail);
                    deviceDetail.setGroupingDetail(groupingDetail);
                    deviceDetail.setRegistrationDate(new Date());
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceDetail.setDeviceFlexibilityDetail(deviceFlexibilityDetail);
                    Date lastConnected = new Date();
                    deviceDetail.setLastConnectedTime(lastConnected);

                    Random random = new Random();
                    //LOGGER.debug(random.nextDouble());

                    deviceDetail.setLatitude(lat + random.nextDouble() * smallStep);
                    deviceDetail.setLongitude(lon + random.nextDouble() * smallStep);

                    userService.updateDeviceList(usr.getUserName(), deviceDetail);

                    DeviceDetail deviceDetail1 = deviceDetailService.getDevice(deviceId);

                    for (int j = 0; j < numDataPoints; j++) {
                        DeviceDetailData dd = new DeviceDetailData();
                        Date currentDt = new Date(new Date().getTime() + j * 10000);
                        dd.setTime(currentDt);
                        dd.setDeviceData(new DeviceData());
                        // prepare DeviceData object
                        DeviceData deviceData = new DeviceData();
                        deviceData.setCurrent(1.2);
                        deviceData.setVoltage(220.0);
                        deviceData.setPower(10.2);
                        deviceData.setEnergy(5.2);
                        deviceData.setDate(currentDt);
                        dd.setState(DeviceState.Idle);
                        dd.setValue(0.2);
                        dd.setDeviceData(deviceData);
                        userService.storeDeviceConsumption(deviceDetail1, dd);
                        userService.updateDeviceState(deviceDetail1, dd);
                    }

                }
            }

            usr = userService.getUser("");
            deviceDetailList = userRepository.findDeviceList(usr.getUserName());
            if (deviceDetailList.size() < numDevices) {
                for (int i = 1; i <= numDevices; i++) {
                    String deviceId = "testdevice" + i;
                    DeviceDetail deviceDetail = new DeviceDetail(deviceId);
                    deviceDetail.setAlias("testdevice" + i);
                    ConsumptionTsEntity consumptionTs = new ConsumptionTsEntity();
                    deviceDetail.setConsumptionTs(consumptionTs);
                    GroupingDetail groupingDetail = new GroupingDetail();
                    deviceDetailService.addGroupingDetail(groupingDetail);
                    deviceDetail.setGroupingDetail(groupingDetail);
                    DeviceFlexibilityDetail deviceFlexibilityDetail = new DeviceFlexibilityDetail();
                    deviceDetail.setDeviceFlexibilityDetail(deviceFlexibilityDetail);
                    Date lastConnected = new Date();
                    deviceDetail.setLastConnectedTime(lastConnected);

                    Random random = new Random();
                    deviceDetail.setLatitude(lat + random.nextDouble() * smallStep);
                    deviceDetail.setLongitude(lon + random.nextDouble() * smallStep);

                    userService.updateDeviceList(usr.getUserName(), deviceDetail);

                    DeviceDetail deviceDetail1 = deviceDetailService.getDevice(deviceId);
                    for (int j = 0; j < numDataPoints; j++) {
                        DeviceDetailData dd = new DeviceDetailData();
                        Date currentDt = new Date(new Date().getTime() + j * 10000);
                        dd.setTime(currentDt);
                        dd.setDeviceData(new DeviceData());
                        // prepare DeviceData object
                        DeviceData deviceData = new DeviceData();
                        deviceData.setCurrent(1.2);
                        deviceData.setVoltage(220.0);
                        deviceData.setPower(10.2);
                        deviceData.setEnergy(5.2);
                        deviceData.setDate(currentDt);
                        dd.setState(DeviceState.Idle);
                        dd.setValue(0.2);
                        dd.setDeviceData(deviceData);
                        userService.storeDeviceConsumption(deviceDetail, dd);
                        userService.updateDeviceState(deviceDetail1, dd);
                    }

                }
            }
        };
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("DeviceListExecuter-");
        executor.initialize();
        return executor;
    }
}
