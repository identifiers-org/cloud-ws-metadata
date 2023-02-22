package org.identifiers.cloud.ws.metadata.api.controllers;

import org.identifiers.cloud.ws.metadata.api.models.HealthApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.api.controllers
 * Timestamp: 2018-08-23 11:15
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@RestController
@RequestMapping("healthApi")
public class HealthApiController {
    @Autowired
    private HealthApiModel model;

    // liveness probe
    @RequestMapping(value = "/liveness_check")
    public String livenessCheck() {
        return model.livenessCheck();
    }

    // Readiness check
    @RequestMapping(value = "/readiness_check")
    public String readinessCheck() {
        return model.readinessCheck();
    }
}
