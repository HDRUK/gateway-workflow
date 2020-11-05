package com.gateway.workflow.services;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.logging.Logger;

import static com.gateway.workflow.util.GenerateJwtUtil.generateGatewayJWT;

@Configuration
public class DARNotificationService implements JavaDelegate {

    private static final Logger LOGGER = Logger.getLogger(DARNotificationService.class.getName());

    @Autowired
    private Environment environment;

    /**
     * <p>This is executed by the camunda bpmn, once a timer completes a delegate execution is made.
     * This triggers an api call back to the gateway to send out notifications about the breach of the SLA (Service Level Agreement).
     * </p>
     * @param delegateExecution Executes listeners
     * @see <a href="https://docs.camunda.org/javadoc/camunda-bpm-platform/7.14/">Delegate Execution</a>
     * */
    public void execute(DelegateExecution delegateExecution) throws Exception {
        sendNotification(delegateExecution.getProcessBusinessKey()).exchange().block().bodyToMono(String.class).block();
    }

    /**
     * <p>Makes the rest call the gateway to trigger the notifications being sent.</p>
     * @param dataRequestId The DarId passed in from the execute method
     * @return The webclient response
     * */
    private WebClient.RequestBodySpec sendNotification(String dataRequestId) throws WebClientException {
        String gateway = environment.getProperty("gateway-api.url");
        String systemUser = environment.getProperty("jwt.system-user");
        String token = generateGatewayJWT(systemUser);

        WebClient webClient = WebClient.create(gateway);
        WebClient.RequestBodySpec client = webClient
                .post()
                .uri("api/v1/data-access-request/" + dataRequestId + "/notify")
                .header("Access-Control-Allow-Origin", "*")
                .cookie("jwt", token);
        return client;
    }
}
