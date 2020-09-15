package com.gateway.workflow.util;

public class ConstantPropertiesUtil {

    private ConstantPropertiesUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static final String DATA_REQUEST_FLOW = "dataRequest";
    public static final String UPDATE_REQUEST = "updateRequest";

    public static final String REQUESTED_BY = "requestedBy";
    public static final String STATUS_TYPE = "statusType";
    public static final String UPDATED_BY = "updatedBy";
}
