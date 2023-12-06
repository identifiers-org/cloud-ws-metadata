package org.identifiers.cloud.ws.metadata.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Project: metadata
 * Package: org.identifiers.cloud.ws.metadata.models
 * Timestamp: 2018-09-18 17:28
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Component
public class MetadataFetcherChromeEngineBased implements MetadataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(MetadataFetcherChromeEngineBased.class);

    @Value("${org.identifiers.cloud.ws.metadata.backend.selenium.driver.chrome.path.bin}")
    private String pathChromedriver;

    private ChromeDriverService chromeDriverService;
    private final ChromeOptions chromeOptions =
            new ChromeOptions().addArguments(
                    "--disable-gpu",
                    "--no-sandbox",
                    "--headless"
                );

    @PostConstruct
    private void init() {
        logger.info("Starting Chrome driver service");
        chromeDriverService = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(pathChromedriver))
                .usingAnyFreePort()
                .withSilent(true)
                .build();
        try {
            chromeDriverService.start();
            // TODO create a watchdog that makes sure the chrome driver service is running
        } catch (IOException e) {
            String errorMessage = String.format("Could not start Google Chrome Driver Service due to '%s'!", e.getMessage());
            logger.error(errorMessage);
            throw new MetadataFetcherException(errorMessage);
        }
    }

    @PreDestroy
    private void shutdown() {
        logger.info("Shutting down Google Chromedriver Service");
        chromeDriverService.stop();
    }

    @Override
    public Object fetchMetadataFor(String url) throws MetadataFetcherException {
        logger.info("Connecting to google chrome driver");
        RemoteWebDriver driver = new RemoteWebDriver(chromeDriverService.getUrl(), chromeOptions);
        List<Object> metadataObjects = Collections.emptyList();
        try {
            logger.info("Using Google Chrome driver to get URL '{}' content", url);
            driver.get(url); // FIXME: Not sure if this waits for all elements to load

            String jsonLdXpathQuery = "//script[@type='application/ld+json']";
            List<WebElement> jsonLdWebElements = driver.findElements(By.xpath(jsonLdXpathQuery));

            ObjectMapper mapper = new ObjectMapper();
            metadataObjects = jsonLdWebElements.stream().map(webElement -> {
                try {
                    return mapper.readTree(webElement.getAttribute("innerText"));
                } catch (IOException e) {
                    logger.error("MALFORMED METADATA for URL '{}', metadata '{}'", url, webElement.getAttribute("innerText"));
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            logger.info("For URL '{}', #{} metadata entries recovered, out of #{} metadata entries",
                    url, metadataObjects.size(), jsonLdWebElements.size());
        } finally {
            driver.quit();
        }
        return metadataObjects;
    }
}
