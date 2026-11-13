package org.apicodes;

import aimock.AiMock;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

/**
 * When a test fails, asks the AI (Ollama, qwen2.5-coder:3b) to explain the failure and suggest fixes.
 * The answer goes to the console and into the TestNG report (Reporter output of the failed test).
 *
 * <p>With the AI mock (the default) the answer is a canned one from
 * {@code src/test/resources/wiremock/mappings/ollama-failure-analysis.json}, labelled as such.
 * With {@code -Dai=live} it is the real model. If the AI can't answer, a short note says so;
 * the analysis never changes a test's result.
 *
 * <p>Registered through {@code META-INF/services/org.testng.ITestNGListener}, so it also runs with {@code -Dtest=...}.
 */
public class TestListener implements ITestListener {

    static final String MODEL = "qwen2.5-coder:3b";

    private final OllamaClient ollama = new OllamaClient();

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        String analysis = analyze(error == null ? "unknown failure" : String.valueOf(error.getMessage()));
        String source = AiMock.isOn() ? "AI mock, canned answer" : "model " + MODEL;
        System.out.println("AI failure analysis for " + result.getName() + " (" + source + "):\n" + analysis);
        Reporter.setCurrentTestResult(result);
        Reporter.log("<b>AI failure analysis</b> (" + source + ")<pre>" + escape(analysis) + "</pre>");
        Reporter.setCurrentTestResult(null);
    }

    /** Asks the AI about one failure. Never throws: if the AI can't answer, says why. */
    public String analyze(String errorLog) {
        String prompt = "Analyze this Selenium test failure: " + errorLog + ". Suggest possible fixes in bullet points.";
        try {
            String answer = ollama.generateText(MODEL, prompt);
            if (answer == null || answer.isBlank()) {
                return "AI analysis unavailable: empty answer";
            }
            return answer.trim();
        } catch (Exception e) {
            return "AI analysis unavailable: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
