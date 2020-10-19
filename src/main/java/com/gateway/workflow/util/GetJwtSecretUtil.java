package com.gateway.workflow.util;

import java.io.IOException;

import static com.gateway.workflow.util.GetEnvPropUtil.getEnvProp;

public class GetJwtSecretUtil {

    public static String getJWTSecret() throws IOException {
        String gcpProjectNumber = getEnvProp("PROJECT_NUMBER");
        String gcpJwtId = getEnvProp("JWT_ID");
        String gcpSecretVersion = getEnvProp("SECRET_VERSION");

        String secret = "";
        if(!gcpProjectNumber.equals("") && !gcpJwtId.equals("") && !gcpSecretVersion.equals("")) {
            try {
                secret = SecretManagerUtil.getSecret(gcpProjectNumber, gcpJwtId, gcpSecretVersion);
            } catch (IOException e) {
                throw new IOException(e.getMessage());
            }
        }

        if(secret.equals("")) {
            try {
                return System.getenv("SECRET_JWT");
            } catch (NullPointerException e) {
                throw new IOException(e.getMessage());
            }
        }

        return secret;
    }
}
