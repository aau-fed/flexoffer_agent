package org.goflex.wp2.fogenerator.ExceptionHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class UncaughtExceptionHandler {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void handleAll(Exception e) {
        logger.error("Unhandled exception occurred", e);
    }

}
