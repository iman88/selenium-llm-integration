package com.gbt;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HealingPageRegion1 {
    static SelfHealingGbtClient healingDriver;
    WebDriver driver;
    String baseUrl = "https://example.cypress.io/commands/querying";

    By parentLocatorBy = By.xpath("//*[@id=\"querying\"]/div[1]");
    String regionHTML;

    @BeforeMethod
    public void beforeMethod() throws InterruptedException {
        // setup
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        healingDriver = new SelfHealingGbtClient(driver);
        System.out.println("Healing using ChatGBT");
        // open page
        driver.get(baseUrl);
        Thread.sleep(3000);

        // extract parent html for AI comparison
        WebElement parentElement = driver.findElement(parentLocatorBy);
        regionHTML = (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return arguments[0].innerHTML;", parentElement);
    }

    @Test(priority = 1)
    public void validLocator() throws Exception {
        By goodLocator = By.xpath("//*[@id='query-btn' and contains(text(), 'Button')]");
        WebElement button = healingDriver.findElementWithHealing(goodLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button.click();
    }

    @Test(priority = 2)
    public void goodTextAndBadIdLocator() throws Exception {
        By goodTextAndBadIdLocator = By.xpath("//*[@id='query-btnn' and contains(text(), 'Button')]");
        WebElement button1 = healingDriver.findElementWithHealing(goodTextAndBadIdLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button1.click();
    }

    @Test(priority = 3)
    public void badTextAndBadIdLocator() throws Exception {
        By badTextAndBadIdLocator = By.xpath("//*[@id='query-btnn' and contains(text(), 'Button new')]");
        WebElement button2 = healingDriver.findElementWithHealing(badTextAndBadIdLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button2.click();
    }

    @AfterMethod
    public void afterMethod() {
        healingDriver.closeAll();
    }

}