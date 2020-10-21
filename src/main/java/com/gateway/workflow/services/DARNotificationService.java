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

    public void execute(DelegateExecution delegateExecution) throws Exception {
        sendNotification(delegateExecution.getProcessBusinessKey()).exchange().block().bodyToMono(String.class).block();
    }

    private WebClient.RequestBodySpec sendNotification(String dataRequestId) throws WebClientException {
        String gateway = environment.getProperty("gateway-api.url");
        String systemUser = environment.getProperty("jwt.system-user");
        String token = generateGatewayJWT(systemUser);

        WebClient webClient = WebClient.create(gateway);
        WebClient.RequestBodySpec client = webClient
                .post()
                .uri("/api/v1/slaNotification/" + dataRequestId)
                .header("Access-Control-Allow-Origin", "*")
                .cookie("jwt", token);
        return client;
    }
}
