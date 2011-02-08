package org.ow2.proactive_grid_cloud_portal.exceptions;

import java.io.Serializable;

import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "exception")
@XmlAccessorType(XmlAccessType.FIELD) 
@Produces("application/json")
public class ExceptionToJson  implements Serializable {

        private int httpErrorCode;
        private String errorMessage;
        private String stackTrace;
        
        public ExceptionToJson() {}
        
        //getters and setters for errorCode and errorMessage    
        public int getHttpErrorCode() {
            return httpErrorCode;
        }
        public void setHttpErrorCode(int errorCode) {
            this.httpErrorCode = errorCode;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getStackTrace() {
            return stackTrace;
        }
        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }
       
    }

