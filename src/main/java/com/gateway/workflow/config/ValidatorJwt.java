package com.gateway.workflow.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.digitalstate.camunda.authentication.jwt.AbstractValidatorJwt;
import io.digitalstate.camunda.authentication.jwt.ValidatorResultJwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.util.Date;
import java.util.List;


public class ValidatorJwt extends AbstractValidatorJwt {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorJwt.class);
    private static String jwtSecret;

    @Override
    public ValidatorResultJwt validateJwt(String encodedCredentials, String jwtSecretPath) {
        if(jwtSecret != null || !jwtSecret.equals("")) {
            try {
                jwtSecret = new String(Files.readAllBytes(new ClassPathResource(jwtSecretPath).getFile().toPath()));
            } catch (Exception e) {
                logger.error(String.format("Error: %s", e.getMessage()));
                return ValidatorResultJwt.setValidatorResult(false,null,null, null);
            }
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .acceptNotBefore(new Date().getTime())
                    .build();
            DecodedJWT jwt = verifier.verify(encodedCredentials);

            String username = jwt.getClaim("username").asString();
            List<String> groupIds = jwt.getClaim("groupIds").asList(String.class);
            List<String> tenantIds = jwt.getClaim("tenantIds").asList(String.class);

            if (!username.equals("")){
                logger.error("BAD JWT: Missing username");
                return ValidatorResultJwt.setValidatorResult(false, null, null, null);
            }

            return ValidatorResultJwt.setValidatorResult(true, username, groupIds, tenantIds);

        } catch(JWTVerificationException exception){
            logger.error("BAD JWT: ${exception.getLocalizedMessage()}");
            return ValidatorResultJwt.setValidatorResult(false, null, null, null);
        }
    }
}
