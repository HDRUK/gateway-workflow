package com.gateway.workflow.config;

import io.digitalstate.camunda.authentication.jwt.ProcessEngineAuthenticationFilterJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class CamundaSecurityFilter {

    @Value("${jwt.validator-class}")
    String jwtValidatorClass;

    @Bean
    public FilterRegistrationBean processEngineAuthenticationFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("camunda-jwt-auth");
        registration.addInitParameter("authentication-provider", "io.digitalstate.camunda.authentication.jwt.AuthenticationFilterJwt");
        registration.addInitParameter("jwt-validator", ValidatorJwt.class.getName());
        registration.addUrlPatterns("/engine-rest/*");
        registration.addUrlPatterns("/api/gateway/*");
        registration.setFilter(getProcessEngineAuthenticationFilter());
        return registration;
    }

    @Bean
    public Filter getProcessEngineAuthenticationFilter() {
        return new ProcessEngineAuthenticationFilterJwt();
    }
}
