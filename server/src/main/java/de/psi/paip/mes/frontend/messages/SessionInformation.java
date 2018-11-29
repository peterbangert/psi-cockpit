package de.psi.paip.mes.frontend.messages;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Data
public class SessionInformation {

    private static final Logger LOG = LoggerFactory.getLogger(SessionInformation.class);

    private String sessionId;

    private String reconnectTimer;

    private String appVersion;

    private String uniqueIdentifier;

    public SessionInformation(String sessionId, String reconnectTimer, String appVersion, String uniqueIdentifier){
        this.sessionId = sessionId;
        this.reconnectTimer = reconnectTimer;
        this.appVersion = appVersion;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String toString(){
        return "{ \"sessionId\": \""+getSessionId()+"\", \"sessionId\": \""+getUniqueIdentifier()+"\", \"reconnectTimer\": "+getReconnectTimer()+"\", \"reconnectTimer\": "+getAppVersion()+"}";
    }
}
