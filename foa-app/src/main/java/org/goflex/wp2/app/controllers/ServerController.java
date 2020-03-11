package org.goflex.wp2.app.controllers;

import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.models.ServerDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1.0/server")
public class ServerController {


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


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getServerStatus() {

        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("ok");
        statusMsg.setData(new ServerDetails("FOA", 1));
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }

}
