

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
 *  Last Modified 2/2/18 4:13 PM
 */

package org.goflex.wp2.app.controllers;

import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.models.FlexOfferT;
import org.goflex.wp2.core.models.PoolDeviceModel;
import org.goflex.wp2.core.wrappers.GetFOControllerWrapper;
import org.goflex.wp2.foa.wrapper.PoolFOWrapper;
import org.goflex.wp2.foa.interfaces.FOAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/* This is a native built-in RESTful Flex-Offer generator */
@RestController
@RequestMapping("/api/v1.0")
public class FOARestController implements ApplicationContextAware {

    public static final Logger logger = LoggerFactory.getLogger(FOARestController.class);
    public static final long HOUR = 3600 * 1000; // in milli-seconds.
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Autowired
    private Environment env;

    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private FOAService foaService;

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    @RequestMapping(value = "/uriForTest", method = RequestMethod.GET)
    public ResponseEntity<String> getTestString() {

        return new ResponseEntity<>("Ok i am returning", HttpStatus.OK);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
    }

    @RequestMapping(value = "/flexOffers", method = RequestMethod.POST)
    public ResponseEntity<?> getFlexOffers(@RequestBody GetFOControllerWrapper foControllerWrapper) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = new Date();
        String methodToInvoke = "getFlexOfferBy";
        Class<?>[] paramTypes = {List.class};
        List<Object> params = new ArrayList<Object>();
        if (!foControllerWrapper.getClientID().equals("") && foControllerWrapper.getClientID() != null) {
            methodToInvoke = methodToInvoke + "ClientID";
            params.add(foControllerWrapper.getClientID());
        }
        if (!foControllerWrapper.getPlugID().equals("") && foControllerWrapper.getPlugID() != null) {
            methodToInvoke = methodToInvoke + "PlugID";
            params.add(foControllerWrapper.getPlugID());
        }
        if (foControllerWrapper.getCreationTime() != null) {
            methodToInvoke = methodToInvoke + "CreationTime";

            dt = foControllerWrapper.getCreationTime();

            params.add(dt);
        }
        if (foControllerWrapper.getStatus() != null) {
            methodToInvoke = methodToInvoke + "Status";
            params.add(FlexOfferState.fromInteger(Integer.parseInt(foControllerWrapper.getStatus())));
        }
        if (methodToInvoke.equals("getFlexOfferBy")) {
            return new ResponseEntity<>("No Parameter Provided", HttpStatus.OK);
        }

        Method method = foaService.getClass().getMethod(methodToInvoke, paramTypes);
        List<FlexOfferT> fo = (ArrayList) method.invoke(foaService, params);
        return new ResponseEntity<>(fo, HttpStatus.OK);
    }

    /*URI to get allflexoffers*/
    @RequestMapping(value = "/getAllFlexOffer", method = RequestMethod.GET)
    public ResponseEntity<List<FlexOfferT>> retrieveActiveFlexOffers(@RequestHeader(value = "Authorization") String authorization) {
        return new ResponseEntity<>(foaService.getAllFO(), HttpStatus.OK);
    }

    @RequestMapping(value = "/organization/flexOffers/{orgName}", method = RequestMethod.POST)
    public ResponseEntity<?> getOrgFlexOffers(@RequestBody PoolFOWrapper poolFOWrapper,
                                              @PathVariable("orgName") String orgName) throws Exception {

        //swissDeviceDetail
        UUID foId = poolFOWrapper.getFoId();
        poolFOWrapper.getDeviceIds().forEach(deviceId ->{
            poolDeviceDetail.get(orgName).get(deviceId).setLastFOGenerated(new Date());
            poolDeviceDetail.get(orgName).get(deviceId).setLastFOId(foId);
            poolDeviceDetail.get(orgName).get(deviceId).setHasActiveFO(true);
        });
        return new ResponseEntity<>("Ok", HttpStatus.OK);
    }

}