package de.psi.paip.mes.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;

@SpringBootApplication(scanBasePackages = "de.psi.paip.mes")
@Controller
public class FrontendApplication {
    //private final String fqh;

    public static void main(String[] args) {
        SpringApplication.run(FrontendApplication.class, args);
    }

    /*
    @Bean
    public ApplicationRunner sched(WorkflowWebSocketHandler handler) {
        return args -> {
            Timer timer = new Timer("dummy live");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Workflow state change");
                    String playload = "{ \"workflowVariables\": { \"activityType\": \"userTask\" }, \"variables\":{ \"stationID\":\"" + stationID + "\",\"skillcheckSuccessful\":true,\"verificationRequired\":true,\"VIN\":\"123456789\",\"employeeId\":123,\"withEquipment\":false},\"fields\":[{\"id\":\"stepCompleted\",\"type\":\"boolean\",\"label\":\"Prozessschritt abgeschlossen?\",\"defaultValue\":\"true\"}],\"properties\":{}} ";
                    handler.pushStateChange(playload);
                }
            }, 10000, 10000);

        };
    }
    */

    /*
    public FrontendApplication(@Value("${server.port}") int serverPort) throws UnknownHostException {
        fqh = InetAddress.getLocalHost().getHostName() + ":" + serverPort;
    }
    */
}
