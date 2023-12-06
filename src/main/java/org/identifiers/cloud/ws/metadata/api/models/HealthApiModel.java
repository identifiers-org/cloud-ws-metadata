package org.identifiers.cloud.ws.metadata.api.models;

import org.identifiers.cloud.ws.metadata.api.controllers.HealthApiController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.api.models
 * Timestamp: 2018-08-23 11:12
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
@Deprecated
@ConditionalOnBean(HealthApiController.class)
public class HealthApiModel {
    private static final String runningSessionId = UUID.randomUUID().toString();

    public String livenessCheck() {
        return runningSessionId;
    }

    // TODO - Refactor this to actually reflect whether the service ir ready or not
    public String readinessCheck() {
        return runningSessionId;
    }
}
