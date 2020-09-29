package com.gateway.workflow.config;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

public class ProfileCondition extends AnyNestedCondition {

    public ProfileCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = "deployed-profile")
    static class OnDeployedProfile {
    }
}
