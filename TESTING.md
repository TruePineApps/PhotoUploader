# Testing Strategy

## Unit Tests
Automated unit tests cover business logic, data mapping, and utility functions.

## Integration Tests
No automated integration tests are implemented for the Google Photos API interaction.
The upload flow is validated manually before each release using a dedicated
test Google account. This is a known limitation accepted given the complexity
of automating OAuth flows and the scope of the application.

## Manual Pre-release Checklist
- [ ] Sign in with a Google account succeeds
- [ ] Directory selection works on Windows, macOS, and Linux
- [ ] Pre-upload summary shows correct album and photo counts
- [ ] Upload completes successfully for a test folder
- [ ] Post-upload report matches Google Photos content
- [ ] Cancel upload works mid-upload
- [ ] Legal documents load and are displayed correctly
- [ ] Settings are preserved across restarts