package com.gateway.workflow.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.workflow.dtos.JwtPayloadDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import static com.gateway.workflow.util.GetJwtSecretUtil.getJWTSecret;

public class GenerateJwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(GenerateJwtUtil.class);

    public static String generateGatewayJWT(String systemUser) {
        String token = "";
        try {
            String secret = getJWTSecret();
            String sub = UUID.randomUUID().toString();
            String id = systemUser;
            long iat = new Date().getTime() / 1000L;

            JwtPayloadDto payload = JwtPayloadDto.builder()
                    .sub(sub)
                    .id(id)
                    .iat(Long.toString(iat))
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            Algorithm algorithm = Algorithm.HMAC256(secret);
            token = JWT.create()
                    .withClaim("data", objectMapper.writeValueAsString(payload))
                    .sign(algorithm);

            return token;
        } catch (JWTCreationException | IOException exception) {
            logger.error(String.format("BAD JWT: %s", exception.getMessage()));
        }

        return token;
    }
}