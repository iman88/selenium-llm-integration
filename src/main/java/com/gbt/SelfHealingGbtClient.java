package com.gbt;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SelfHealingGbtClient {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SelfHealingGbtClient(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(1));
    }

    public WebElement findElementWithHealing(By originalLocator, String regionHTML) throws Exception {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(originalLocator));
        } catch (Exception e) {
            System.out.println("Locator failed, attempting self-healing: " + originalLocator);
            return attemptSelfHealing(originalLocator, regionHTML);
        }
    }

    private WebElement attemptSelfHealing(By originalLocator, String regionHTML) {
        try {
            String xpath = originalLocator.toString().substring("By.xpath: ".length()).trim();
            String newLocator = ChatGPTLocatorFixer.getFixedLocator(regionHTML, xpath);
            System.out.println("Healed locator: " + newLocator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(newLocator)));
        } catch (Exception ex) {
            throw new NoSuchElementException("Self-healing failed: " + ex.getMessage());
        }
    }

    public void closeAll() {
        driver.quit();
    }
}
