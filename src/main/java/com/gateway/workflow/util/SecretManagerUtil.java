package com.gateway.workflow.util;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;

import java.io.IOException;

public class SecretManagerUtil {

    private SecretManagerUtil() {
        throw new IllegalStateException("Secret Manager class");
    }

    // Get secrets from GCP's secret manager
    public static String getSecret(String projectId, String secretId, String secretVersion) throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, secretVersion);

            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

            if(!response.hasPayload()) {
                return String.format("Unable to find secret related to this {$s1}, project", projectId);
            }

            return response.getPayload().getData().toStringUtf8();
        }
    }


}
