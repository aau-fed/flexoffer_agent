/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
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
 *  Last Modified 2/16/18 1:52 PM
 */

package org.goflex.wp2.foa.implementation;


import org.springframework.stereotype.Component;

/**
 * Created by bijay on 7/26/17.
 */
@Component
public class FlexOfferGeneratorImpl_abs {

    /*public static final long HOUR = 3600*1000; // in milli-seconds.
    public static final long interval = 900*1000;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private FOAService foaService;

    @Resource(name = "startGeneratingFo")
    private ConcurrentHashMap<String, Integer> startGeneratingFo;


    public String foaId;

    @Override
    public String getFoaId() {
        return foaId;
    }

    public DeviceDetailService deviceDetailService;

    private ConcurrentHashMap<String, TpLinkDeviceService> foaConnectedPlugs;

    @Override
    public String getName(){
        return null;
    }

    @Override
    public String getDescription(){
        return null;
    }

    @Override
    public String getConfigurationURL(){
        return null;
    }


    @Override
    public FlexOffer[] createFlexOffer(String deviceID, String foaId, int number){ //Hashtable will be replaced by userid and plugid

        FlexOffer[] newFlexOffers = new FlexOffer[number];
        Random randDevice = new Random(1000);
        Random flexHour = new Random(1001);
        Random startHour = new Random(1003);
        Random sliceSize = new Random(1002);
        Random sliceMinAmount = new Random(1004);
        Random sliceMaxAmount = new Random(1005);


        for(int num=0;num < number;num++){

            *//**
     * This is just to test multiple devices need to remove.
     *//*

     *//*String deviceID = "";
            for (TpLinkDeviceService plug : plugs.values()) {
                int val = deviceDetailService.getAllDevices().size();
                int idx = randDevice.nextInt(val);
                deviceID = deviceDetailService.getAllDevices().get(idx).getDeviceId();
            }*//*
            ////////////////////////////////////////////////////////

            FlexOffer flexOffer = new FlexOffer();
            flexOffer.setId(UUID.randomUUID());
            flexOffer.setState(FlexOfferState.Initial);
            flexOffer.setStateReason("FlexOffer Initialized");

            Date date = new Date(); //Get current date ( can be set to desired time )
            //Formatter outputs string
            *//*SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z' ");
            date = dateFormatter.format(date);*//*



            int acceptHours = flexHour.nextInt(12) + 1;

            flexOffer.setCreationTime(date);
            flexOffer.setOfferedById(deviceID);
            flexOffer.setLocationId("123");
            flexOffer.setAcceptBeforeTime(new Date(date.getTime() + HOUR/4));
            flexOffer.setAssignmentBeforeTime(new Date(date.getTime() + (acceptHours + 1) * HOUR));

            flexOffer.setTotalEnergyConstraint(new FlexOfferConstraint(45.46, 50.56));
            flexOffer.setTotalCostConstraint(new FlexOfferConstraint(45.46, 50.56));
            flexOffer.setStartAfterTime(date);



            int startHours = startHour.nextInt(5) + 1;

            flexOffer.setStartBeforeTime(new Date(date.getTime() + (acceptHours + startHours + 1) * HOUR));




            int randSlices = sliceSize.nextInt(4) + 2;

            FlexOfferSlice[] flexOfferProfileConstraints = new FlexOfferSlice[randSlices];

            for (int i = 0; i < flexOfferProfileConstraints.length; i++) {
                flexOfferProfileConstraints[i] = new FlexOfferSlice();
                flexOfferProfileConstraints[i].setMinDuration(1);
                flexOfferProfileConstraints[i].setMaxDuration(1);
                FlexOfferConstraint[] energyConstraintList = new FlexOfferConstraint[1];
                for (int j = 0; j < energyConstraintList.length; j++) {
                    energyConstraintList[j] = new FlexOfferConstraint();
                    double minAmount = sliceMinAmount.nextInt(2);
                    double maxAmount = sliceMinAmount.nextInt(4)+2;
                    energyConstraintList[j].setLower(minAmount);
                    energyConstraintList[j].setUpper(maxAmount);
                }
                flexOfferProfileConstraints[i].setEnergyConstraintList(energyConstraintList);
                flexOfferProfileConstraints[i].setTariffConstraint(new FlexOfferTariffConstraint(0.03, 0.15));
            }

            flexOffer.setFlexOfferProfileConstraints(flexOfferProfileConstraints);

            TariffConstraintProfile tariffConstraintProfile = new TariffConstraintProfile();
            tariffConstraintProfile.setStartTime(date);
            tariffConstraintProfile.setStartTime(date);
            TariffSlice[] tariffConstraintSlices = new TariffSlice[randSlices];
            for (int i = 0; i < tariffConstraintSlices.length; i++) {
                tariffConstraintSlices[i] = new TariffSlice();
                tariffConstraintSlices[i] = new TariffSlice(1, 0.03, 0.15);
            }
            tariffConstraintProfile.setTariffSlices(tariffConstraintSlices);

            flexOffer.setFlexOfferTariffConstraint(tariffConstraintProfile);

            FlexOfferSchedule defaultSchedule = new FlexOfferSchedule();
            FlexOfferScheduleSlice[] flexOfferScheduleSlices = new FlexOfferScheduleSlice[randSlices];
            for (int i = 0; i < flexOfferScheduleSlices.length; i++) {
                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice();
                flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice((flexOfferProfileConstraints[i].getEnergyConstraint(0).getLower()+
                                                                            flexOfferProfileConstraints[i].getEnergyConstraint(0).getUpper())/2, 1.0);
                flexOfferScheduleSlices[i].setDuration(1);
            }
            defaultSchedule.setScheduleSlices(flexOfferScheduleSlices);
            defaultSchedule.setStartTime(new Date(date.getTime() + (acceptHours + startHours - 1) * HOUR));

            flexOffer.setDefaultSchedule(defaultSchedule);

            FlexOfferSchedule flexOfferSchedule = new FlexOfferSchedule(defaultSchedule);
            flexOfferSchedule.setScheduleId(12345);
            flexOfferSchedule.setUpdateId(1);
            flexOfferSchedule.setStartTime(new Date(date.getTime() + (acceptHours + startHours - 1) * HOUR));

            flexOffer.setInitialFlexOfferSchedule(flexOfferSchedule);
            newFlexOffers[num] = flexOffer;
        }
        return newFlexOffers;
    }

    @Override
    public FlexOffer createOnOffFlexOffer(DeviceDetail device){

        Random flexHour = new Random(1001);
        Random startHour = new Random(1003);
        Random sliceSize = new Random(1002);
        Random sliceMinAmount = new Random(1004);

        FlexOffer flexOffer = new FlexOffer();
        flexOffer.setId(UUID.randomUUID());
        flexOffer.setState(FlexOfferState.Initial);
        flexOffer.setStateReason("FlexOffer Initialized");

        Date date = new Date();

        //flex-offer should be accepted within 15 mins
        int acceptInterval = 1;

        flexOffer.setCreationTime(date);
        flexOffer.setOfferedById(device.getDeviceId());
        flexOffer.setLocationId("123");

        flexOffer.setAcceptBeforeTime(new Date(flexOffer.getCreationTime().getTime() + acceptInterval * interval));
        flexOffer.setAssignmentBeforeTime(new Date(flexOffer.getCreationTime().getTime() + acceptInterval * interval));
        flexOffer.setStartAfterTime(new Date(flexOffer.getCreationTime().getTime() + (acceptInterval+1) * interval));
        flexOffer.setStartBeforeTime(new Date(flexOffer.getCreationTime().getTime() + (acceptInterval+1) * interval));


        double max_energy = device.getConsumptionTs().getDefaultValue();
        if(device.getPlugType() == 0){
            max_energy = max_energy * 20/4000; //multiply load by 20
        }else{
            max_energy = max_energy/4000; //converting to kW
        }


        flexOffer.setTotalEnergyConstraint(new FlexOfferConstraint(0, max_energy));


        flexOffer.setTotalCostConstraint(new FlexOfferConstraint(0, 3));


        FlexOfferSlice[] flexOfferProfileConstraints = new FlexOfferSlice[1];

        for (int i = 0; i < flexOfferProfileConstraints.length; i++) {
            flexOfferProfileConstraints[i] = new FlexOfferSlice();
            flexOfferProfileConstraints[i].setMinDuration(1);
            flexOfferProfileConstraints[i].setMaxDuration(1);
            FlexOfferConstraint[] energyConstraintList = new FlexOfferConstraint[1];
            for (int j = 0; j < energyConstraintList.length; j++) {
                energyConstraintList[j] = new FlexOfferConstraint();
                double minAmount = 0;
                double maxAmount = max_energy;

                energyConstraintList[j].setLower(minAmount);
                energyConstraintList[j].setUpper(maxAmount);
            }
            flexOfferProfileConstraints[i].setEnergyConstraintList(energyConstraintList);
            flexOfferProfileConstraints[i].setTariffConstraint(new FlexOfferTariffConstraint(0.03, 0.15));
        }

        flexOffer.setFlexOfferProfileConstraints(flexOfferProfileConstraints);

        TariffConstraintProfile tariffConstraintProfile = new TariffConstraintProfile();
        tariffConstraintProfile.setStartTime(new Date(flexOffer.getCreationTime().getTime() + (acceptInterval+1) * interval));
        TariffSlice[] tariffConstraintSlices = new TariffSlice[1];
        for (int i = 0; i < tariffConstraintSlices.length; i++) {
            tariffConstraintSlices[i] = new TariffSlice();
            tariffConstraintSlices[i] = new TariffSlice(1, 0.03, 0.15);
        }
        tariffConstraintProfile.setTariffSlices(tariffConstraintSlices);

        flexOffer.setFlexOfferTariffConstraint(tariffConstraintProfile);

        FlexOfferSchedule defaultSchedule = new FlexOfferSchedule();
        FlexOfferScheduleSlice[] flexOfferScheduleSlices = new FlexOfferScheduleSlice[1];
        for (int i = 0; i < flexOfferScheduleSlices.length; i++) {
            flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice();
            flexOfferScheduleSlices[i] = new FlexOfferScheduleSlice(max_energy, 1.0);
            flexOfferScheduleSlices[i].setDuration(1);
        }
        defaultSchedule.setScheduleSlices(flexOfferScheduleSlices);
        defaultSchedule.setStartTime(new Date(date.getTime() + (acceptInterval+1) * interval));

        flexOffer.setDefaultSchedule(defaultSchedule);

        FlexOfferSchedule flexOfferSchedule = new FlexOfferSchedule(defaultSchedule);
        flexOfferSchedule.setScheduleId(12345);
        flexOfferSchedule.setUpdateId(1);
        flexOfferSchedule.setStartTime(new Date(date.getTime() + (acceptInterval+1) * interval));

        flexOffer.setInitialFlexOfferSchedule(flexOfferSchedule);


        return flexOffer;
    }

    @Override
    @Scheduled(fixedRate = HOUR/2)
    public void generateFO()
    {
        List<UserT> users = userRepository.findAll();
        Set<DeviceDetail> deviceDetails;
        if(users!=null && users.size() > 0 && startGeneratingFo.containsKey("start"))
        {
            for(UserT user:users) {
                deviceDetails = user.getDeviceDetail();
                if(deviceDetails!=null && deviceDetails.size() > 0){
                    for(DeviceDetail deviceDetail:deviceDetails){
                        if(deviceDetail.getDeviceState()== DeviceState.Idle || deviceDetail.getDeviceState()== DeviceState.Operating) {
                            if(deviceDetail.getConsumptionTs().getDefaultValue()>0) {
                                FlexOffer flexOffer1 = this.createOnOffFlexOffer(deviceDetail);
                                FlexOffer[] flexOffer2 = this.createFlexOffer(deviceDetail.getDeviceId(), "AAUCloud", 1);


                                FlexOfferT fo = new FlexOfferT(deviceDetail.getUser().getUserName(), deviceDetail.getDevicePlugId(),
                                        flexOffer1.getCreationTime(), flexOffer1.getFlexOfferSchedule().getStartTime(), flexOffer1.getState(), flexOffer1.getId().toString(), flexOffer1);
                                foaService.save(fo);

                            *//*foaMemoryDOA.put(flexOffer1.getId(), flexOffer1);*//*
                                FlexOfferGeneratedEvent foGeneratedEvent = new FlexOfferGeneratedEvent(this, "FlexOffer Generation Request Received", "", flexOffer1); //Create new event
                                applicationEventPublisher.publishEvent(foGeneratedEvent); //Publish an event

                            *//*foaMemoryDOA.put(flexOffer2[0].getId(), flexOffer2[0]);
                            FlexOfferGeneratedEvent foGeneratedEvent2 = new FlexOfferGeneratedEvent(this, "FlexOffer Generation Request Received", "", flexOffer2[0]); //Create new event
                            applicationEventPublisher.publishEvent(foGeneratedEvent2); //Publish an event
*//*
                            }
                        }
                   }

                }
            }
        }
    }

    @Override
    public void generateFO2()
    {
        List<UserT> users = userRepository.findAll();
        Set<DeviceDetail> deviceDetails;
        if(users!=null && users.size() > 0)
        {
            for(UserT user:users) {
                deviceDetails = user.getDeviceDetail();
                if(deviceDetails!=null && deviceDetails.size() > 0){
                    for(DeviceDetail deviceDetail:deviceDetails){
                        if(deviceDetail.getDeviceState()== DeviceState.Idle || deviceDetail.getDeviceState()== DeviceState.Operating) {
                            FlexOffer flexOffer1 = this.createOnOffFlexOffer(deviceDetail);
                            FlexOffer[] flexOffer2 = this.createFlexOffer(deviceDetail.getDeviceId(), "AAUCloud", 1);


                            FlexOfferT fo = new FlexOfferT(deviceDetail.getUser().getUserName(), deviceDetail.getDevicePlugId(),
                                    flexOffer1.getCreationTime(), flexOffer1.getFlexOfferSchedule().getStartTime(), flexOffer1.getState(), flexOffer1.getId().toString(), flexOffer1);
                            foaService.save(fo);

                            *//*foaMemoryDOA.put(flexOffer1.getId(), flexOffer1);*//*
                            FlexOfferGeneratedEvent foGeneratedEvent = new FlexOfferGeneratedEvent(this, "FlexOffer Generation Request Received", "", flexOffer1); //Create new event
                            applicationEventPublisher.publishEvent(foGeneratedEvent); //Publish an event

                            *//*foaMemoryDOA.put(flexOffer2[0].getId(), flexOffer2[0]);
                            FlexOfferGeneratedEvent foGeneratedEvent2 = new FlexOfferGeneratedEvent(this, "FlexOffer Generation Request Received", "", flexOffer2[0]); //Create new event
                            applicationEventPublisher.publishEvent(foGeneratedEvent2); //Publish an event
*//*
                        }
                    }

                }
            }
        }
    }*/


}
