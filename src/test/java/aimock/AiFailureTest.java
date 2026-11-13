package aimock;

import com.gbt.ChatGPTLocatorFixer;
import org.apicodes.OllamaClient;
import org.apicodes.SelfHealingOllamaClient;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * What the healing does when the AI misbehaves. Only possible with the AI mock, so these skip with -Dai=live.
 * Each broken locator below picks one stub in src/test/resources/wiremock/mappings/ai-failures.json.
 * No browser: the AI clients are called directly.
 */
public class AiFailureTest {

    private static final String REGION_HTML = "<button type=\"button\" id=\"query-btn\">Button</button>";

    @BeforeClass
    public void mockOnly() {
        if (!AiMock.isOn()) {
            throw new SkipException("AI failure cases need the AI mock (run without -Dai=live)");
        }
    }

    @Test
    public void openAiAnswerThatIsNotAnXPathIsRejected() {
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
                () -> ChatGPTLocatorFixer.getFixedLocator(REGION_HTML, "//*[@id='ai-answers-with-prose']"));
        assertEquals(e.getMessage(),
                "Unexpected format. Not a valid XPath: Sorry, I could not find that element in the HTML.");
    }

    @Test
    public void openAiAnswerInFencedXPathBlockIsUnwrapped() throws IOException {
        assertEquals(ChatGPTLocatorFixer.getFixedLocator(REGION_HTML, "//*[@id='ai-answers-fenced']"),
                "//*[@id='query-btn']");
    }

    @Test
    public void openAiRateLimitIsReportedNotSwallowed() {
        IOException e = expectThrows(IOException.class,
                () -> ChatGPTLocatorFixer.getFixedLocator(REGION_HTML, "//*[@id='ai-rate-limited']"));
        assertTrue(e.getMessage().contains("code=429"), "expected HTTP 429 in: " + e.getMessage());
        assertTrue(e.getMessage().contains("Rate limit reached for gpt-4o"), "expected the API error in: " + e.getMessage());
    }

    @Test
    public void ollamaAnswerThatIsNotAnXPathIsRejected() throws Exception {
        String answer = new OllamaClient().generateText("qwen2.5-coder:3b",
                "correct this locator: '//*[@id='ai-answers-with-prose']'");
        assertEquals(answer, "Sorry, I could not find that element in the HTML.");
        IllegalArgumentException e = expectThrows(IllegalArgumentException.class,
                () -> SelfHealingOllamaClient.extractXPathLocator(answer));
        assertEquals(e.getMessage(),
                "No valid XPath locator found in AI response: Sorry, I could not find that element in the HTML.");
    }

    @Test
    public void ollamaNotRunningFailsTheCall() throws IOException {
        String mockUrl = System.getProperty("ollama.base.url");
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        System.setProperty("ollama.base.url", "http://localhost:" + closedPort);
        try {
            assertThrows(java.net.ConnectException.class,
                    () -> new OllamaClient().generateText("qwen2.5-coder:3b", "any prompt"));
        } finally {
            System.setProperty("ollama.base.url", mockUrl);
        }
    }
}
