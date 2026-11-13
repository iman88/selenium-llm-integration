package org.apicodes;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelfHealingOllamaClient {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final OllamaClient ollama;

    public SelfHealingOllamaClient(WebDriver driver) {
        this.driver = driver;
        this.ollama = new OllamaClient();
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
            String prompt = String.format(
                    "A Java Selenium automation locator has changed. Analyze the following HTML snippet: '%s' " +
                    "and correct this locator: '%s'. " +
                    "Respond ONLY with one valid relative XPath expression. " +
                    "If the original locator uses properties like id, placeholder, type, class, or text, " +
                    "validate each one and fix any that are no longer valid.",
                    regionHTML, originalLocator
            );
            String newLocator = extractXPathLocator(ollama.generateText("qwen2.5-coder:3b", prompt));
            System.out.println("Healed locator: " + newLocator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(newLocator)));
        } catch (Exception ex) {
            throw new NoSuchElementException("Self-healing failed: " + ex.getMessage());
        }
    }

    public static String extractXPathLocator(String aiResponse) {
        Pattern codeBlock = Pattern.compile("```xpath\\s*([\\s\\S]*?)\\s*```");
        Matcher m = codeBlock.matcher(aiResponse);
        if (m.find()) {
            String xpath = m.group(1).trim();
            if (xpath.startsWith("//")) return xpath;
        }

        Pattern byXpath = Pattern.compile("By\\.xpath:\\s*(//[^\\s\\n]+(?:\\s*\\[[^\\]]+\\])?)");
        m = byXpath.matcher(aiResponse);
        if (m.find()) return m.group(1).trim();

        Pattern fallback = Pattern.compile("(//[^\\s\"']+\\[[^\\]]+\\])");
        m = fallback.matcher(aiResponse);
        if (m.find()) return m.group(1).trim();

        throw new IllegalArgumentException("No valid XPath locator found in AI response: " + aiResponse);
    }

    public void closeAll() {
        driver.quit();
    }
}
