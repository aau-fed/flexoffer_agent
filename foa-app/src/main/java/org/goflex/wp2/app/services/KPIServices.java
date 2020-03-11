package org.goflex.wp2.app.services;

import org.goflex.wp2.app.common.CustomException;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.DeviceDetail;
import org.goflex.wp2.core.models.FoaKPI;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.FoaKPIRepository;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.core.wrappers.KPIWrapper;
import org.goflex.wp2.foa.implementation.ConsumptionTsServiceImpl;
import org.goflex.wp2.foa.interfaces.ConsumptionTsService;
import org.goflex.wp2.foa.interfaces.DeviceDetailService;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class KPIServices {

    public static final int numSecondsPerInterval = 15 * 60;
    private static final Logger logger = LoggerFactory.getLogger(KPIServices.class);
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceDetailService deviceDetailService;
    @Autowired
    private FOAService foaService;
    @Autowired
    private ConsumptionTsService consumptionTsService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private FoaKPIRepository foaKPIRepository;

    @Resource(name = "flexibilityRatio")
    private ConcurrentHashMap<Date, Map<String, Double>> flexibilityRatio;

    public List<KPIWrapper> getKPIs(Long userId, Long organizationId, Collection<GrantedAuthority> roles, Date today, Date yesterday) {

        if (userId == null) {
            throw new CustomException("User Not Found", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (roles != null) {
            //if (roles.contains(UserRole.ROLE_ADMIN)) {
            if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return foaKPIRepository.findLast2KPIforOrganization(organizationId, yesterday, today);
            } else {
                return foaKPIRepository.findLast2KPIForUser(organizationId, userId, yesterday, today);
            }
        }
        return null;
    }

    public KPIWrapper getKPI(Long userId, Long organizationId, Collection<GrantedAuthority> roles, Date date) {

        if (userId == null) {
            throw new CustomException("User Not Found", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (roles != null) {
            //if (roles.contains(UserRole.ROLE_ADMIN)) {
            if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return foaKPIRepository.findKPIforOrganization(organizationId, date);
            } else {
                return foaKPIRepository.findKPIForUser(organizationId, userId, date);
            }
        }
        return null;
    }

    private void processForProsumer(Organization organization, UserT user, Date today, Date tomorrow) {
        Collection<UserRole> roles = new ArrayList<>();
        roles.add(user.getRole());

        //get all FOs for today
        List<Object> params = new ArrayList<>();
        params.add(user.getUserName());
        params.add(today);
        params.add(tomorrow);
        List<FlexOffer> flexOffers;

        //Check if KPI for the prosumer already exists, if yes delete
        try {
            List<FoaKPI> kpis = foaKPIRepository.findAllByUserIdAndDateofAggregation(user.getId(), today);
            if (kpis != null) {
                foaKPIRepository.deleteAll(kpis);
            }
        } catch (Exception ex) {
            logger.info(ex.toString());
        }

        //Get all flexoffers generated for the prosumer
        flexOffers = foaService.getFlexOfferByClientIDScheduleStartTime(params);

        //Active prosumer is always 1
        int activeProsumers = 1;

        //Get All devices for the prosumer
        List<DeviceDetail> devices = deviceDetailService.getActiveDeviceByUser(user.getId());

        // Get all active devices for prosumer
        int activeDevices = devices.size();

        //Get Total Consumption for the prosumer devices
        double totalConsumption = devices.stream()
                .mapToDouble(deviceDetail -> consumptionTsService.getConsumptionForDate(deviceDetail.getConsumptionTs().getId(), today, tomorrow))
                .sum();

        //Get total flexdemand for the prosumer flexoffers
        double flexDemand = flexOffers.stream()
                .mapToDouble(flexOffer -> flexOffer.getSumEnergyConstraints().getUpper()
                        - flexOffer.getSumEnergyConstraints().getLower()).sum();

        //Get change in flexibility ratio
        double flexRatio = 0.0;
        if (totalConsumption > 0.0) {
            flexRatio = (flexDemand / totalConsumption) * 100.0;
            flexRatio = Math.round((flexRatio) * 1000D) / 1000D;
        }

        //get change in generated flexoffers for prosumer
        int foCount = -1;
        try {
            foCount = flexOffers.size();//this.getFOCount(user.getUserName(), yesterday, "day", roles);
        } catch (Exception ex) {
        }

        FoaKPI foaKPI = new FoaKPI(organization.getOrganizationId(), user.getId(), today,
                activeDevices, activeProsumers, flexRatio, foCount);
        foaKPIRepository.save(foaKPI);
    }

    private void processForOrganization(Organization organization, Date today, Date yesterday) {
        userService.getActiveUsersForOrganization(organization.getOrganizationId()).stream()
                .filter(userT -> userT.getRole() == UserRole.ROLE_PROSUMER)
                .forEach(user -> this.processForProsumer(organization, user, today, yesterday));
    }

    @Scheduled(cron = "0 59 * * * *")
    //@Scheduled(fixedRate = 60000)
    public void calculateKPIs() {
        logger.info("running KPI calculation cron job...");
        Calendar cal = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            final Date today = formatter.parse(formatter.format(new Date()));
            cal.setTime(today);
            cal.add(Calendar.DATE, 1);
            organizationRepository.findAll().stream().forEach(organization -> this.processForOrganization(organization, today, cal.getTime()));
        } catch (Exception ex) {

        }

    }


    /**
     * Absolute code can be removed
     */
    public Integer getActiveProsumers(String userName, Long organizationId, Collection<GrantedAuthority> roles) {
        if (userName == null) {
            throw new CustomException("User Not Found", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (roles != null) {
            //if (roles.contains(UserRole.ROLE_ADMIN)) {
            if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                //return userService.getActiveUserCount(organizationId);
                return userService.getActiveProsumerCount(organizationId);
            } else {
                return 1;
            }
        }
        return 0;
    }

    public int getFOCount(String userName, long organizationId, Date today, Date yesterday, String resolution, Collection<GrantedAuthority> roles) {
        if (userName == null) {
            throw new CustomException("User Not Found", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        List<Object> params = new ArrayList<>();
        params.add(organizationId);
        params.add(today);
        params.add(yesterday);
        if (roles != null) {
            //if (roles.contains(UserRole.ROLE_ADMIN)) {
            if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                return foaService.getFlexOfferByOrganizationIDScheduleStartTime(params).size();
            } else {
                params.set(0, userName);
                return foaService.getFlexOfferByClientIDScheduleStartTime(params).size();
            }
        }
        return 0;

    }

    public int getFOCount(String userName, Date today, String resolution, Collection<GrantedAuthority> roles) {
        if (userName == null) {
            throw new CustomException("User Not Found", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return foaService.getFlexOffersCountForDate(userName, today, resolution);

    }


    public int getActiveDevices(String userName, long organizationId, long userId, Collection<GrantedAuthority> roles) {
        if (userName == null) {
            throw new CustomException("User Not Found", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        //if (roles.contains(UserRole.ROLE_ADMIN)) {
        if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            return userService.getDevicesforOrganization(organizationId);
        }
        return deviceDetailService.getActiveDeviceByUser(userId).size();
    }

    public Map<String, Double> getFlexibilityRatio(UserT user, long organizationId, Collection<GrantedAuthority> roles) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        //To return default list with 0 value
        Map<String, Double> isEmpty = new HashMap<>();
        isEmpty.put("flexibleDemand", 0.0);
        isEmpty.put("totalConsumption", 0.0);
        try {
            //today = formatter.parse("2018-06-27");
            today = formatter.parse(formatter.format(today.getTime()));
        } catch (Exception ex) {
            return isEmpty;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        //to get data for yesterday
        c.add(Calendar.DATE, 1);
        List<Object> params = new ArrayList<>();
        params.add(organizationId);
        params.add(today);
        params.add(c.getTime());
        List<FlexOffer> flexOffers;
        List<Long> tsIds;
        Map<String, Double> data = new HashMap<>();

        if (flexibilityRatio.containsKey(today)) {
            //if (roles.contains(UserRole.ROLE_ADMIN)) {
            if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                if (flexibilityRatio.get(today).containsKey(Long.toString(organizationId))) {
                    data.put(Long.toString(organizationId), flexibilityRatio.get(today).get(organizationId));
                    return data;
                }
            }
            if (flexibilityRatio.get(today).containsKey(user.getUserName())) {
                data.put(user.getUserName(), flexibilityRatio.get(today).get(organizationId));
                return data;
            }
        }

        //if (roles.contains(UserRole.ROLE_ADMIN)) {
        if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            flexOffers = foaService.getFlexOfferByOrganizationIDScheduleStartTime(params);
            tsIds = userService.getDeviceListforOrganization(organizationId).stream()
                    .map(deviceDetail -> deviceDetail.getConsumptionTs().getId()).collect(Collectors.toList());
        } else {
            params.set(0, user.getUserName());
            flexOffers = foaService.getFlexOfferByClientIDScheduleStartTime(params);
            tsIds = userService.getDevices(user.getUserName()).stream()
                    .map(deviceDetail -> deviceDetail.getConsumptionTs().getId()).collect(Collectors.toList());
        }
        double totalConsumption = consumptionTsService.getConsumptionForDate(tsIds, today, c.getTime());
        double flexDemand = flexOffers.stream().mapToDouble(flexOffer ->
                flexOffer.getSumEnergyConstraints().getUpper() - flexOffer.getSumEnergyConstraints().getLower()).sum();
        double ratio = 0.0;
        if (totalConsumption > 0) {
            ratio = flexDemand * 100 / totalConsumption;
        }

        //if (roles.contains(UserRole.ROLE_ADMIN)) {
        if (roles.stream().anyMatch(role -> role.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
            data.put(Long.toString(organizationId), ratio);
        } else {
            data.put(user.getUserName(), ratio);
        }
        return data;
    }

}
