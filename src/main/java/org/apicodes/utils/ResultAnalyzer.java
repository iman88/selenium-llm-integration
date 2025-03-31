package org.apicodes.utils;

import static org.apicodes.SelfHealingOllamaClient.getOllama;


public class ResultAnalyzer{

    public static void analyzeFailure(String errorMessage) throws Exception {
        String prompt = "What could cause this Selenium error? " + errorMessage;
        String analysis = getOllama().generateText("qwen2.5-coder:3b", prompt);
        System.out.println("AI Analysis: " + analysis);
    }
}
