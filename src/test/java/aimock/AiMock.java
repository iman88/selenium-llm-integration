package aimock;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Mocks the AI communication (OpenAI and Ollama) for the whole test run. The web page stays live.
 *
 * <p>{@code mvn test} (or {@code -Dai=mock}) starts a WireMock server and points both AI clients at it.
 * The canned answers are in {@code src/test/resources/wiremock/mappings}: one stub per broken locator,
 * each returning an explicit healed XPath. A request that matches no stub gets a 404, so the healing fails
 * loudly instead of calling a real model.
 *
 * <p>{@code mvn test -Dai=live} leaves the clients alone: real Ollama on localhost:11434 and real OpenAI
 * (needs OPENAI_API_KEY). Use it to check the model itself; the mock only checks our healing logic.
 *
 * <p>Registered through {@code META-INF/services/org.testng.ITestNGListener}, so it also runs with
 * {@code -Dtest=...}.
 */
public class AiMock implements ISuiteListener {

    private static WireMockServer server;

    public static boolean isOn() {
        return !"live".equalsIgnoreCase(System.getProperty("ai", "mock"));
    }

    @Override
    public synchronized void onStart(ISuite suite) {
        if (!isOn()) {
            System.out.println("AI: live (Ollama on localhost:11434, OpenAI api.openai.com)");
            return;
        }
        if (server != null) {
            return;
        }
        server = new WireMockServer(options().dynamicPort().usingFilesUnderClasspath("wiremock"));
        server.start();
        String baseUrl = "http://localhost:" + server.port();
        System.setProperty("openai.base.url", baseUrl);
        System.setProperty("ollama.base.url", baseUrl);
        System.out.println("AI: mock on " + baseUrl + " (" + server.getStubMappings().size() + " stubs)");
    }

    @Override
    public synchronized void onFinish(ISuite suite) {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
