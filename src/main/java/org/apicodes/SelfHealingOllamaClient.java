package org.apicodes;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelfHealingOllamaClient {
    private WebDriver driver;
    private static OllamaClient ollama;

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public static OllamaClient getOllama() {
        return ollama;
    }

    public void setOllama(OllamaClient ollama) {
        ollama = ollama;
    }

    private final WebDriverWait wait;

    public SelfHealingOllamaClient(WebDriver driver) {
        this.driver = driver;
        ollama = new OllamaClient();
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
            String prompt = String.format(
                    "A Java Selenium automation locator has changed. Analyze the following HTML snippet: '%s' " +
                            "and correct this locator: '%s'. " +
                            "Respond ONLY with one valid relative XPath expression. " +
                            "If the original locator uses properties like id, placeholder, type, class, or text, " +
                            "validate each one and fix any that are no longer valid.",
                    regionHTML, originalLocator
            );
            // System.out.println("prompt:" + prompt);
            String newSelector = ollama.generateText("qwen2.5-coder:3b", prompt);
            String newLocator= extractXPathLocator(newSelector);
            System.out.println("healed locator: " + newLocator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(newLocator)));
        } catch (Exception ex) {
            throw new NoSuchElementException("Self-healing failed: " + ex.getMessage());
        }
    }

    public static String extractXPathLocator(String aiResponse) {
        // First try to extract from code block like ```xpath\n<xpath>\n```
        Pattern codeBlockPattern = Pattern.compile("```xpath\\s*([\\s\\S]*?)\\s*```");
        Matcher codeBlockMatcher = codeBlockPattern.matcher(aiResponse);
        if (codeBlockMatcher.find()) {
            String xpath = codeBlockMatcher.group(1).trim();
            if (xpath.startsWith("//")) {
                return xpath;
            }
        }

        // Next, try format like: By.xpath: //input[@id='inputName']
        Pattern byXpathPattern = Pattern.compile("By\\.xpath:\\s*(//[^\\s\\n]+(?:\\s*\\[[^\\]]+\\])?)");
        Matcher byXpathMatcher = byXpathPattern.matcher(aiResponse);
        if (byXpathMatcher.find()) {
            return byXpathMatcher.group(1).trim();
        }

        // Finally, fallback: look for any line that looks like an XPath
        Pattern fallbackPattern = Pattern.compile("(//[^\\s\"']+\\[[^\\]]+\\])");
        Matcher fallbackMatcher = fallbackPattern.matcher(aiResponse);
        if (fallbackMatcher.find()) {
            return fallbackMatcher.group(1).trim();
        }

        throw new IllegalArgumentException("No valid XPath locator found in the response.");
    }




    public void closeAll() {
        this.driver.quit();
    }

}
