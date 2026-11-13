package aimock;

import org.apicodes.TestListener;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * TestListener's AI failure analysis, against the AI mock (skips with -Dai=live).
 * The canned answer is in src/test/resources/wiremock/mappings/ollama-failure-analysis.json. No browser.
 */
public class FailureAnalysisTest {

    private static final String SELENIUM_ERROR =
            "no such element: Unable to locate element: {\"method\":\"xpath\",\"selector\":\"//*[@id='query-btnn']\"}";

    @BeforeClass
    public void mockOnly() {
        if (!AiMock.isOn()) {
            throw new SkipException("Failure analysis checks need the AI mock (run without -Dai=live)");
        }
    }

    @Test
    public void failureIsExplainedByTheAi() {
        assertEquals(new TestListener().analyze(SELENIUM_ERROR),
                "- Check that the locator in the error still matches the page.\n"
                        + "- Compare it with the region HTML the healing used.\n"
                        + "- If the page changed, update the locator or let self-healing fix it.");
    }

    @Test
    public void aiDownGivesANoteInsteadOfBreakingTheRun() throws IOException {
        String mockUrl = System.getProperty("ollama.base.url");
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        System.setProperty("ollama.base.url", "http://localhost:" + closedPort);
        try {
            String note = new TestListener().analyze(SELENIUM_ERROR);
            assertTrue(note.startsWith("AI analysis unavailable: "), "got: " + note);
        } finally {
            System.setProperty("ollama.base.url", mockUrl);
        }
    }
}
