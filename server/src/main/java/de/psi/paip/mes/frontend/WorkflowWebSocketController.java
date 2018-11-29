package de.psi.paip.mes.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RestController
@RequestMapping("api/socket/workflow")
@EnableWebSocket
public class WorkflowWebSocketController implements WebSocketConfigurer {

    @Autowired
    private WorkflowWebSocketHandler workflowWebSocketHandler;

    @Value("${app.version}")
    private String appVersion;

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowWebSocketController.class);

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(workflowWebSocketHandler, "/api/socket/workflow").setAllowedOrigins("*");
    }

    /**
     * register terminalId before subscribing to websocket
     * @param terminalId of werkercockpit
     * @return success / failure of connecting to websocket
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "registerTerminal/{terminalId}")
    public ResponseEntity<SocketInformation> registerTerminal(@PathVariable String terminalId) {
    	if(terminalId != null && terminalId.length() > 0) {

            workflowWebSocketHandler.setTerminalId(terminalId);
            workflowWebSocketHandler.setUniqueIdentifier(UUID.randomUUID().toString()); // set unique identifier
            SocketInformation info = new SocketInformation("Register Terminal successful",terminalId, UUID.randomUUID().toString());
            //return ResponseEntity.ok("{ \"terminalId\": \""+terminalId+"\", \"uniqueIdentifier: \""+UUID.randomUUID().toString()+"}");
            return ResponseEntity.ok().body(info);
    	} else {
    		return ResponseEntity.badRequest().body(null);
    	}
    }

    /**
     * @param uniqueIdentifier of this websocket session
     * @return true or false
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "checkSessionId/{uniqueIdentifier}")
    public ResponseEntity<String> checkSessionId(@PathVariable String uniqueIdentifier) {

        if(uniqueIdentifier != null && uniqueIdentifier.length() > 0) {
            if(workflowWebSocketHandler.getSession(uniqueIdentifier).isPresent()){
                return ResponseEntity.ok("{\"exists\": \"true\", \"version\": \""+appVersion+"\"}");
            }else{
                return ResponseEntity.ok("{\"exists\": \"false\", \"version\": \""+appVersion+"\"}");
            }
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"Could not register terminal\"}");
        }
    }

    /**
     *  just for testing can be removed
     * @param uniqueIdentifier of this websocket session
     * @return true or false
     */
    @CrossOrigin(origins = "*")
    @GetMapping(path = "closeSocketSession/{uniqueIdentifier}")
    public ResponseEntity<String> closeSocketSession(@PathVariable String uniqueIdentifier) {

        if(uniqueIdentifier != null && uniqueIdentifier.length() > 0) {

            if(workflowWebSocketHandler.getSession(uniqueIdentifier).isPresent()){
                return ResponseEntity.ok(String.valueOf(workflowWebSocketHandler.closeSession(uniqueIdentifier)));
            }else{
                return ResponseEntity.ok("false");
            }
        } else {
            return ResponseEntity.badRequest().body("session id doesn't exist'");
        }
    }

    public static class SocketInformation {
        public String message;
        public String terminalId;
        public String uniqueIdentifier;

        public SocketInformation(String message, String terminalId, String uniqueIdentifier){
            this.message= message;
            this.terminalId = terminalId;
            this.uniqueIdentifier = uniqueIdentifier;
        }

    }

}
