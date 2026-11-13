---
name: maintain
description: Run the periodic maintenance of this repo - check every row of the README "Maintenance & planned updates" table against current releases (Maven dependencies, Java, the AI models), apply the updates that are due one at a time with mvn test green, and refresh the table. Use when asked for maintenance, upgrades, or "is anything outdated".
---

# Maintain the repo

The README section "🔄 Maintenance & planned updates" is the checklist. Work through it row by row.

1. **Check what is current** (do not rely on memory):
   - Maven: the newest **stable** version from
     `https://repo1.maven.org/maven2/<group path>/<artifact>/maven-metadata.xml` (skip alpha, beta, RC, M).
   - Java support dates: https://endoflife.date/api/eclipse-temurin.json (stay on an LTS).
   - AI models: that OpenAI `gpt-4o` and Ollama `qwen2.5-coder:3b` still exist, and whether a newer one fits.
2. **Apply one update at a time**, each as its own change:
   - change the version property in `pom.xml` (exact versions only, never ranges or pre-releases);
   - `mvn clean test` must be green (the AI is mocked by default, so no key or Ollama is needed);
   - a new **model**: try it with `mvn test -Dai=live` first, then update the model name in the code and in
     the stubs under `src/test/resources/wiremock/mappings` (they check the model name);
   - update README.md wherever it names the version. Done means the code, the stubs and the README.
3. **Keep maven-surefire-plugin on 3.5.x**: 3.6.x drops `suiteXmlFiles`, and the suite runs from `testng.xml`.
   Majors (OkHttp 5, HttpClient 5) need their migration notes read first.
4. **Update the table**: new current versions, end-of-life dates, urgency (✅ when nothing is due) and the
   "Checked on" date. Tell the user what changed and what is still due.

Never upgrade as a side effect of other work, and never commit or push without the user's approval.
