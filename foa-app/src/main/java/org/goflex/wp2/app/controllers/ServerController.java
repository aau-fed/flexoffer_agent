package org.goflex.wp2.app.controllers;

import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.models.ServerDetails;
import org.goflex.wp2.core.models.PoolDeviceModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1.0/server")
public class ServerController {

    @Resource(name = "poolDeviceDetail")
    private ConcurrentHashMap<String, Map<String, PoolDeviceModel>> poolDeviceDetail;

    private ResponseEntity<ResponseMessage> errorResponse(String msg, HttpStatus status) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, status);
    }

    private ResponseEntity<ResponseMessage> successResponse(String msg) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    /**
     * get device model state
     */
    @RequestMapping(value = "/getModelStateMap/{orgName}", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getDeviceModelStateMap(@PathVariable("orgName") String orgName){

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("OK");
        List<PoolDeviceModel> _data = new ArrayList<>();
        poolDeviceDetail.get(orgName).forEach((k, v) ->{
            _data.add(v);
        });
        responseMessage.setData(_data);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getServerStatus() {

        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("ok");
        statusMsg.setData(new ServerDetails("FOA", 1));
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }

}
