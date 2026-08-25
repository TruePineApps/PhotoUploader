# Testing & Release Verification

This document outlines the testing strategy for PhotoUploader, with a heavy emphasis on **Manual Pre-release Verification** since the project does not currently use automated integration tests for Google API flows.

## Unit Tests
Automated unit tests cover business logic, data mapping, and utility functions. Use `./gradlew test` to run all project tests.

## Integration Tests
No automated integration tests are implemented for the Google Photos API interaction.
The upload flow is validated manually before each release using a dedicated
test Google account. This is a known limitation accepted given the complexity
of automating OAuth flows and the scope of the application.

## Manual Pre-release Checklist
This checklist must be performed on all target desktop platforms (Windows, macOS, Linux) before a new release is published.
- [ ] Sign in with a Google account succeeds
- [ ] Directory selection works on Windows, macOS, and Linux
- [ ] Pre-upload summary shows correct album and photo counts
- [ ] Upload completes successfully for a test folder
- [ ] Post-upload report matches Google Photos content
- [ ] Cancel upload works mid-upload
- [ ] Legal documents load and are displayed correctly
- [ ] Settings are preserved across restarts
- [ ] Legal consent gate: scroll-to-bottom enables checkboxes, Accept button
    enables only when all three are checked, policy version check fires on launch