package com.blackboard.platform.extensions.lmsintegrations.layer.exceptions;


import com.blackboard.platform.extensions.restapi.exceptions.EntityException;
import com.blackboard.platform.extensions.restapi.exceptions.ExceptionSource;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

public class BadGatewayException extends EntityException {
        private static final long serialVersionUID = 1846799709030820062L;
        private static final String MESSAGE = "Could not negotiate a bearer token";

        public BadGatewayException(String errorCode, ExceptionSource exceptionSource, String resourceId,
                        String overrideMessage) {
                super(!StringUtils.isBlank(overrideMessage) ? overrideMessage : String.format(MESSAGE, resourceId),
                                errorCode, exceptionSource);
                setHttpStatus(HttpStatus.BAD_GATEWAY_502);
        }

}

