package com.gateway.workflow.config;

import io.digitalstate.camunda.authentication.jwt.ProcessEngineAuthenticationFilterJwt;
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class CamundaSecurityFilter {

    @Value("${jwt.secret-path}")
    String jwtSecretPath;

    @Value("${jwt.validator-class}")
    String jwtValidatorClass;

    @Bean
    public FilterRegistrationBean processEngineAuthenticationFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setName("camunda-jwt-auth");
        registration.addInitParameter("authentication-provider", "io.digitalstate.camunda.authentication.jwt.AuthenticationFilterJwt");
        registration.addInitParameter("jwt-secret-path", jwtSecretPath);
        registration.addInitParameter("jwt-validator", jwtValidatorClass);
        registration.addUrlPatterns("/engine-rest/*");
        registration.setFilter(getProcessEngineAuthenticationFilter());
        return registration;
    }

    @Bean
    public Filter getProcessEngineAuthenticationFilter() {
        return new ProcessEngineAuthenticationFilterJwt();
    }

//    @Bean
//    public FilterRegistrationBean<Filter> processEngineAuthenticationFilter() {
//        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
//        registration.setName("camunda-auth");
//        registration.setFilter(getProcessEngineAuthenticationFilter());
//        registration.addInitParameter("authentication-provider", "org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider");
//        registration.addUrlPatterns("/*");
//        registration.setOrder(1);
//        return registration;
//    }
//
//    @Bean
//    public Filter getProcessEngineAuthenticationFilter() {
//        return new ProcessEngineAuthenticationFilter();
//    }
}
