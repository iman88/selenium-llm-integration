package org.apicodes;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private final OllamaClient ollama = new OllamaClient();

    @Override
    public void onTestFailure(ITestResult result) {
        String errorLog = result.getThrowable().getMessage();
        String prompt = "Analyze this Selenium test failure: " + errorLog +
                ". Suggest possible fixes in bullet points.";
        try {
            System.out.println("AI Failure Analysis:\n" + ollama.generateText("qwen2.5-coder:3b", prompt));
        } catch (Exception e) {
            System.out.println("AI analysis unavailable: " + e.getMessage());
        }
    }
}
