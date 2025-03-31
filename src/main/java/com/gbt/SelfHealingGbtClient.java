package com.gbt;

import org.apicodes.OllamaClient;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SelfHealingGbtClient {
    private WebDriver driver;

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public void setOllama(OllamaClient ollama) {
        ollama = ollama;
    }

    private final WebDriverWait wait;

    public SelfHealingGbtClient(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(1));
    }

    // Self-healing findElement method
    public WebElement findElementWithHealing(By originalLocator, String regionHTML) throws Exception {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(originalLocator));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // enable below line for fix suggestion
            // ResultAnalyzer.analyzeFailure(e.getMessage());
            System.out.println("Starting fixing: " + originalLocator);
            return attemptSelfHealing(originalLocator, e, regionHTML);
        }
    }

    private WebElement attemptSelfHealing(By originalLocator, Exception e, String regionHTML) {
        try {
            String xpath = originalLocator.toString().substring("By.xpath: ".length()).trim();
            String newLocator= ChatGPTLocatorFixer.getFixedLocator(regionHTML, xpath);
            System.out.println("Healed locator: " + newLocator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(newLocator)));
        } catch (Exception ex) {
            throw new NoSuchElementException("Self-healing failed: " + ex.getMessage());
        }
    }

    public void closeAll() {
        this.driver.quit();
    }

}
