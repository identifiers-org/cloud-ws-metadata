package org.identifiers.cloud.ws.metadata.configuration;

import org.identifiers.cloud.ws.metadata.periodictasks.MetadataCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@ConditionalOnProperty("org.identifiers.cloud.ws.metadata.backend.data.metadatacollector.enabled")
public class TasksConfiguration implements SchedulingConfigurer {
    private final Random random = new Random(System.currentTimeMillis());

    @Value("${org.identifiers.cloud.ws.metadata.backend.data.metadatacollector.wait_time_limit}")
    private Duration waitTimeLimit;

    final MetadataCollector metadataCollector;
    public TasksConfiguration(MetadataCollector metadataCollector) {
        this.metadataCollector = metadataCollector;
    }

    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(metadataCollector,
                triggerContext -> Optional
                        .ofNullable(triggerContext.lastActualExecution())
                        .orElse(Instant.now())
                        .plusSeconds(random.nextLong(waitTimeLimit.getSeconds())));
    }
}
