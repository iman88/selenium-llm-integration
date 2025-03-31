package com.gbt;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apicodes.SelfHealingOllamaClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HealingPageRegion2 {
    static SelfHealingGbtClient healingDriver;
    WebDriver driver;
    String baseUrl = "https://example.cypress.io/commands/querying";

    By parentLocatorBy = By.xpath("//*[@id='querying']/div[6]/div[5]");
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
        By goodLocator = By.xpath("//input[@id='inputName' and @placeholder='Name' and @type='text']");
        WebElement button = healingDriver.findElementWithHealing(goodLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button.click();
    }

    @Test(priority = 2)
    public void invalidId() throws Exception {
        By goodLocator = By.xpath("//input[@id='adsad' and @placeholder='Name' and @type='text']");
        WebElement button = healingDriver.findElementWithHealing(goodLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button.click();
    }

    @Test(priority = 3)
    public void invalidPlaceholder() throws Exception {
        By goodLocator = By.xpath("//input[@id='inputName' and @placeholder='User Name' and @type='text']");
        WebElement button = healingDriver.findElementWithHealing(goodLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button.click();
    }

    @Test(priority = 3)
    public void invalidType() throws Exception {
        By goodLocator = By.xpath("//input[@id='inputName' and @placeholder='Name' and @type='password']");
        WebElement button = healingDriver.findElementWithHealing(goodLocator, regionHTML);
        // click is done just to confirm visible & enabled
        button.click();
    }


    @AfterMethod
    public void afterMethod() {
        healingDriver.closeAll();
    }

}