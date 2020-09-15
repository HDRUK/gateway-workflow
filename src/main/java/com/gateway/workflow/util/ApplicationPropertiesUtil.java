package com.gateway.workflow.util;

public class ApplicationPropertiesUtil {

    private ApplicationPropertiesUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static final String GCP_SQL_URL = "gcp.url";
    public static final String GCP_SQL_USER = "gcp.username";
    public static final String GCP_DATA_DRIVER_CLASS = "gcp.dataSourceDriverClassName";
    public static final String GCP_DATA_IDLE_TIMEOUT = "gcp.dataSourceIdleTimeout";
    public static final String GCP_DATA_MIN_IDLE = "gcp.dataSourceMinimumIdle";
    public static final String GCP_DATA_MAX_POOL_SIZE = "gcp.dataSourceMaximumPoolSize";
    public static final String GCP_SECRET_ID= "gcp.secretId";
    public static final String GCP_SECRET_VERSION = "gcp.secretVersion";
    public static final String GCP_PROJECT_NUMBER = "gcp.projectNumber";
}
