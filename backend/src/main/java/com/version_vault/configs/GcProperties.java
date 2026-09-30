package com.version_vault.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "application.gc")
public class GcProperties {
    private long gracePeriodHours;
}