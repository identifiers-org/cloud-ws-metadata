package org.identifiers.cloud.ws.metadata.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private ChromeOptions chromeOptions =
            new ChromeOptions()
                    .setHeadless(true)
                    .addArguments("--disable-gpu",
                            "--no-sandbox");

    @PostConstruct
    private void init() {
        logger.info("Starting Chrome driver service");
        chromeDriverService = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(pathChromedriver))
                .usingAnyFreePort()
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
        WebDriver driver = new RemoteWebDriver(chromeDriverService.getUrl(), chromeOptions);
        List<Object> metadataObjects = new ArrayList<>();
        try {
            logger.info("Using Google Chrome driver to get URL '{}' content", url);
            driver.get(url);
            //logger.info("Google Chrome driver for URL '{}', content\n{}", url, driver.getPageSource());
            String jsonLdXpathQuery = "//script[@type='application/ld+json']";
            List<WebElement> jsonLdWebElements = ((RemoteWebDriver) driver).findElementsByXPath(jsonLdXpathQuery);
            //logger.info("For URL '{}', #{} JSON-LD formatted elements found!", url, jsonLdWebElements.size());
            ObjectMapper mapper = new ObjectMapper();
            metadataObjects = jsonLdWebElements.stream().map(webElement -> {
                try {
                    return mapper.readTree(webElement.getAttribute("innerText"));
                } catch (IOException e) {
                    logger.error("MALFORMED METADATA for URL '{}', metadata '{}'", url, webElement.getAttribute("innerText"));
                }
                return null;
            }).filter(item -> item != null).collect(Collectors.toList());
            logger.info("For URL '{}', #{} metadata entries recovered, out of #{} metadata entries",
                    url, metadataObjects.size(), jsonLdWebElements.size());
        } finally {
            driver.quit();
        }
        return metadataObjects;
    }
}
