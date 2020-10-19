package com.gateway.workflow.util;

public class GetEnvPropUtil {
    private GetEnvPropUtil() {
        throw new IllegalStateException("Environment var class");
    }

    public static String getEnvProp(String envProp) {
        String prop = "";
        try {
            prop = System.getenv(envProp);
        } catch (NullPointerException e) {
            return prop;
        }

        return prop;
    }

}
