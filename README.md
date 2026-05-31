# Self-Healing Selenium Locators with AI Integration

Extended from [SACTAN/AI-powered-locator-generation](https://github.com/SACTAN/AI-powered-locator-generation) — credits to the original concept.

---

## What was extended

The original repo demonstrated the core idea of using Ollama to heal a broken locator against a local HTML file. This project builds on that foundation with the following additions:

- **Real-world test target** — switched from a local HTML file to [example.cypress.io/commands/querying](https://example.cypress.io/commands/querying), a stable public page used in Cypress documentation that rarely changes
- **TestNG test framework** — replaced the `main()` approach with a proper TestNG suite with `@BeforeMethod` / `@AfterMethod` lifecycle and priorities
- **AI-friendly locators** — locators are authored with multiple attributes so the AI has richer context to heal against: `By.xpath("//input[@id='inputName' and @placeholder='Name' and @type='text']")`
- **Extended healing prompt** — the prompt now instructs the model to validate each attribute individually and fix only the ones that are no longer valid
- **OpenAI GPT-4o backend** — added a second healing path via the OpenAI API (`com.gbt`), which is significantly more reliable than a local Ollama model
- **Region-scoped HTML** — instead of sending the full page source, each test extracts only the relevant region's `innerHTML` via JavaScript and sends that to the AI. 27 calls cost ~3 cents

---

## Architecture

```
Test (HealingPageRegion1 / HealingPageRegion2)
         │
         │  findElementWithHealing(By locator, String regionHTML)
         │
    ┌────┴─────────────────────────────┐
    │                                  │
    ▼                                  ▼
SelfHealingOllamaClient         SelfHealingGbtClient
  (org.apicodes)                   (com.gbt)
    │                                  │
    ▼                                  ▼
OllamaClient                   ChatGPTLocatorFixer
localhost:11434                 api.openai.com/v1
qwen2.5-coder:3b                gpt-4o
```

Both paths share the same interface: receive the original `By` locator and the region's HTML, try the original locator, and if it fails ask the AI to produce a corrected XPath.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Browser automation | Selenium WebDriver 4.20 |
| Driver management | WebDriverManager 6 (SeleniumManager fallback) |
| Test framework | TestNG 7.10 |
| Local AI | Ollama + qwen2.5-coder:3b |
| Cloud AI | OpenAI GPT-4o via REST |
| HTTP client (Ollama) | Apache HttpClient 4.5 |
| HTTP client (OpenAI) | OkHttp 4.12 |
| JSON | Gson 2.11 |
| Build | Maven 3 |

---

## Prerequisites

- Java 21+
- Maven 3.6+
- Chrome (WebDriverManager handles the driver automatically)
- **For Ollama backend**: [Ollama](https://ollama.com) running locally with the model pulled:
  ```
  ollama pull qwen2.5-coder:3b
  ollama serve
  ```
- **For OpenAI backend**: an OpenAI API key set as an environment variable:
  ```
  export OPENAI_API_KEY=sk-...
  ```
  On Windows: `$env:OPENAI_API_KEY = "sk-..."`

---

## Setup

```bash
git clone https://github.com/Bernard-Lowe/selenium-llm-integration
cd selenium-llm-integration
mvn clean install -DskipTests
```

---

## Running the tests

```bash
# Full suite (both Ollama and OpenAI backends)
mvn test

# Ollama backend only
mvn test -Dtest="org.apicodes.HealingPageRegion1,org.apicodes.HealingPageRegion2"

# OpenAI backend only
mvn test -Dtest="com.gbt.HealingPageRegion1,com.gbt.HealingPageRegion2"
```

The suite runs against `https://example.cypress.io/commands/querying`.

---

## How the healing works

1. `findElementWithHealing(By, regionHTML)` tries the original locator with a 1-second wait
2. On failure, it extracts a prompt: region HTML + broken locator → AI is asked to return one corrected XPath
3. The corrected locator is parsed from the AI response and retried
4. If the second attempt also fails, `NoSuchElementException` is thrown — no silent swallowing

The `TestListener` registered in `testng.xml` calls the AI on any test failure to produce a bullet-point analysis of what went wrong.

---

## Test coverage

**Region 1** — `cy.get()` button area

| Test | Locator scenario |
|---|---|
| `validLocator` | Correct ID and text — should pass without healing |
| `goodTextAndBadIdLocator` | Wrong ID, correct text — AI must fix the ID |
| `badTextAndBadIdLocator` | Wrong ID and wrong text — AI must fix both |

**Region 2** — `.within()` input area

| Test | Locator scenario |
|---|---|
| `validLocator` | All attributes correct |
| `invalidId` | Wrong `@id` |
| `invalidPlaceholder` | Wrong `@placeholder` |
| `invalidType` | Wrong `@type` |

---

## Roadmap

- [ ] Persist healed locators to a JSON file so the locator store self-updates after a successful heal
- [ ] Bash/CI script to auto-raise a PR when tests pass with healed locators
- [ ] Plug this healing layer into a full-stack Java framework (TestNG + Cucumber, Selenium + Playwright, API, JDBC)

---

## Acknowledgements

Original concept: [SACTAN/AI-powered-locator-generation](https://github.com/SACTAN/AI-powered-locator-generation)

---

## License

This repository is distributed under the [MIT License](https://opensource.org/licenses/MIT) — you are free to use, copy, modify, and distribute this code, provided you retain credit to the original authors.
