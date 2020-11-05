package com.gateway.workflow.config;

import com.gateway.workflow.util.ApplicationPropertiesUtil;
import com.gateway.workflow.util.SecretManagerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "gcp")
@PropertySource({ "classpath:google-cloud.properties"})
@Conditional(ProfileCondition.class)
@Profile("!default")
public class DataSourceCloudConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    @Primary
    public DataSource dataSource() throws IOException {
        // Configure database connection settings
        HikariConfig config = new HikariConfig();

        // Environment vars are mapped to the application properties
        String gcpProjectNumber = environment.getProperty(ApplicationPropertiesUtil.GCP_PROJECT_NUMBER);
        String gcpSecretId = environment.getProperty(ApplicationPropertiesUtil.GCP_SECRET_ID);
        String gcpSecretVersion = environment.getProperty(ApplicationPropertiesUtil.GCP_SECRET_VERSION);

        config.setDriverClassName(environment.getProperty(ApplicationPropertiesUtil.GCP_DATA_DRIVER_CLASS));
        config.setJdbcUrl(environment.getProperty(ApplicationPropertiesUtil.GCP_SQL_URL));
        config.setUsername(environment.getProperty(ApplicationPropertiesUtil.GCP_SQL_USER));
        config.setPassword(SecretManagerUtil.getSecret(gcpProjectNumber, gcpSecretId, gcpSecretVersion));
        config.setIdleTimeout(Long.parseLong(environment.getProperty(ApplicationPropertiesUtil.GCP_DATA_IDLE_TIMEOUT)));
        config.setMinimumIdle(Integer.parseInt(environment.getProperty(ApplicationPropertiesUtil.GCP_DATA_MIN_IDLE)));
        config.setMaximumPoolSize(Integer.parseInt(environment.getProperty(ApplicationPropertiesUtil.GCP_DATA_MAX_POOL_SIZE)));
        return new HikariDataSource(config);
    }
}