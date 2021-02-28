package org.goflex.wp2.app;

import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.models.*;
import org.goflex.wp2.core.repository.ContractRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.repository.UserRepository;
import org.goflex.wp2.foa.implementation.OrganizationalConsumptionService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.wrapper.PoolSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.app"})
@EnableJpaRepositories(basePackages = {"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.app"})
@IntegrationComponentScan({"org.goflex.wp2.core", "org.goflex.wp2.foa", "org.goflex.wp2.app"})
@EnableRetry
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final String TEST_ORG_NAME = "TEST";

    private final UserService userService;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final OrganizationalConsumptionService organizationalConsumptionService;
    private final DeviceFlexOfferGroup deviceFlexOfferGroup;

    @Resource(name = "directControlMode")
    ConcurrentHashMap<Long, OrganizationLoadControlState> directControlMode;

    @Resource(name = "defaultFlexibilitySettings")
    Map<Long, DeviceFlexibilityDetail> defaultFlexibilitySettings;

    @Resource(name = "orgAccEnergyData")
    LinkedHashMap<String, Map<Date, Double>> orgAccEnergyData;

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    @Resource(name = "poolScheduleMap")
    private ConcurrentHashMap<String, Map<Long, PoolSchedule>> poolScheduleMap;

    @Resource(name = "poolTurnedOffDevices")
    private ConcurrentHashMap<String, Map<String, Date>> poolTurnedOffDevices;

    @Autowired
    public ApplicationConfig(UserService userService, ContractRepository contractRepository,
                             PasswordEncoder passwordEncoder,
                             OrganizationRepository organizationRepository,
                             OrganizationalConsumptionService organizationalConsumptionService,
                             DeviceFlexOfferGroup deviceFlexOfferGroup) {
        this.userService = userService;
        this.contractRepository = contractRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.organizationalConsumptionService = organizationalConsumptionService;
        this.deviceFlexOfferGroup = deviceFlexOfferGroup;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean(name = "InitializingBean")
    InitializingBean doInitialization() {
        return () -> {

            if (organizationRepository.findByOrganizationName(TEST_ORG_NAME) == null) {
                Organization orgTEST = new Organization();
                orgTEST.setOrganizationId(10001);
                orgTEST.setOrganizationName(TEST_ORG_NAME);
                orgTEST.setDirectControlMode(OrganizationLoadControlState.Stopped);
                orgTEST.setPoolBasedControl(false);
                orgTEST.setPoolDeviceCoolingPeriod(180);
                organizationRepository.save(orgTEST);
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
                    String userPass = "password";
                    UserT adminUser = new UserT();
                    adminUser.setUserName(userName);
                    adminUser.setPassword(passwordEncoder.encode(userPass));
                    adminUser.setRole(UserRole.ROLE_ADMIN);
                    UserAddress address = new UserAddress();
                    adminUser.setUserAddress(address);
                    adminUser.setRegistrationDate(new Date());
                    adminUser.setOrganization(organization.getOrganizationId());
                    adminUser.setEmail(userName + "@please-set-email.com");
                    userService.save(adminUser);
                }

                userName = organization.getOrganizationName().toLowerCase() + "SysAdmin";
                if (userService.getUser(userName) == null) {
                    // add user for swiss case if not exists
                    String userPass = "pwd4SysAdmin!";
                    UserT adminUser = new UserT();
                    adminUser.setUserName(userName);
                    adminUser.setPassword(passwordEncoder.encode(userPass));
                    adminUser.setRole(UserRole.ROLE_ADMIN);
                    UserAddress address = new UserAddress();
                    adminUser.setUserAddress(address);
                    adminUser.setRegistrationDate(new Date());
                    adminUser.setOrganization(organization.getOrganizationId());
                    adminUser.setEmail(userName + "@please-set-email.com");
                    userService.save(adminUser);
                }
            }

            // add admin user if not exists
            String adminUserName = "admin";
            String adminUserPass = "password";
            Organization org = organizationRepository.findByOrganizationName(TEST_ORG_NAME);
            if (userService.getUser(adminUserName) == null) {
                UserT userAdmin = new UserT();
                userAdmin.setUserName(adminUserName);
                userAdmin.setPassword(passwordEncoder.encode(adminUserPass));
                userAdmin.setRole(UserRole.ROLE_ADMIN);
                UserAddress address = new UserAddress();
                userAdmin.setUserAddress(address);
                userAdmin.setOrganization(org.getOrganizationId());
                userAdmin.setRegistrationDate(new Date());
                userAdmin.setOrganization(organizationRepository.findByOrganizationName(TEST_ORG_NAME).getOrganizationId());
                userAdmin.setEmail(adminUserName + "@please-set-email.com");
                userService.save(userAdmin);
            }

            // so that contract table is not empty
            if (contractRepository.count() == 0) {
                Contract contract = new Contract();
                contract.setUserName("admin");
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
    InitializingBean populatePoolDeviceModelMap() {
        return () -> {
            List<Organization> organizations = organizationRepository.findAll();
            for (Organization org : organizations) {

                if (!this.poolDeviceDetail.containsKey(org.getOrganizationName())) {
                    this.poolDeviceDetail.put(org.getOrganizationName(), new HashMap<>());
                }

                if (!this.poolTurnedOffDevices.containsKey(org.getOrganizationName())) {
                    this.poolTurnedOffDevices.put(org.getOrganizationName(), new HashMap<>());
                }

                if (!poolScheduleMap.containsKey(org.getOrganizationName())) {
                    poolScheduleMap.put(org.getOrganizationName(), new HashMap<>());
                }


                List<DeviceDetail> devices = userService.getDeviceListforOrganization(org.getOrganizationId());

                // add device to in memory device models
                devices.stream()
                       .filter(d -> deviceFlexOfferGroup.getDeviceFOGroupType(d.getDeviceType()) == FlexibilityGroupType.ThermostaticControlLoad)
                       .collect(Collectors.toList()).forEach(device -> {
                               PoolDeviceModel poolDeviceModel = new PoolDeviceModel(org.getOrganizationId(),
                                       device.getDeviceId(), device.getDeviceType().toString(),
                                       null,
                                       device.getDeviceType() == DeviceType.Boiler ? 30 : 20,
                                       device.getDeviceType() == DeviceType.Boiler ? 40 : 25,
                                       -1.0,
                                       null, 0, false, null,
                                       false, -1, -1,
                                       org.getOrganizationName().equals(TEST_ORG_NAME) ? 22.5 : -1,
                                       null, null);
                               this.poolDeviceDetail.get(org.getOrganizationName()).put(device.getDeviceId(), poolDeviceModel);
                        }
                );
            }
        };
    }

    @Bean
    @DependsOn("InitializingBean")
    InitializingBean populateDefaultFlexibilityMap() {
        return () -> {
            organizationRepository.findAll().forEach(organization -> {

                if (organization.getOrganizationName().equals(TEST_ORG_NAME)) {
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
