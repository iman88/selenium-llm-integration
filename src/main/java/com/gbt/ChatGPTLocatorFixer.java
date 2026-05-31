package com.gbt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;


public class ChatGPTLocatorFixer {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY") != null
            ? System.getenv("OPENAI_API_KEY")
            : System.getProperty("openai.api.key", "");
    private static final OkHttpClient client = new OkHttpClient();

    public static String constructGbtPayload(String regionHTML, String originalLocator){
        String prompt = String.format(
                "A Java Selenium automation locator has changed. Analyze the following HTML snippet: '%s' " +
                        "and correct this locator: '%s'. " +
                        "Respond ONLY with one valid relative XPath expression. " +
                        "If the original locator uses properties like id, placeholder, type, class, or text, " +
                        "validate each one and fix any that are no longer valid.",
                regionHTML, originalLocator
        );

        Gson gson = new Gson();
        String escapedPrompt = gson.toJson(prompt); // This returns a properly escaped string WITH quotes

        // Remove surrounding quotes (Gson wraps it in double quotes), or use `.formatted()` with %s
        escapedPrompt = escapedPrompt.substring(1, escapedPrompt.length() - 1);

        String jsonPayload = """
                {
                  "model": "gpt-4o",
                  "messages": [
                    {
                      "role": "system",
                      "content": "You are a helpful assistant skilled in Selenium automation."
                    },
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ]
                }
                """.formatted(escapedPrompt);

        return jsonPayload;
    }

    public static String getFixedLocator(String regionHTML, String originalLocator) throws IOException {

        String gbtPayload = constructGbtPayload(regionHTML, originalLocator);
        RequestBody body = RequestBody.create(gbtPayload, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response + "\n" + response.body().string());
            }

            return extractXPathLocator(response.body().string());
        }
    }

    public static String extractXPathLocator(String aiResponseJson) {
        // Parse content field from JSON
        JsonObject root = JsonParser.parseString(aiResponseJson).getAsJsonObject();
        String content = root.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        // Remove triple backticks and trim
        String cleaned = content.replace("```", "").trim();

        // Optionally validate it's an XPath
        if (!cleaned.startsWith("//")) {
            throw new IllegalArgumentException("Unexpected format. Not a valid XPath: " + cleaned);
        }

        return cleaned;
    }
}
