package com.truepine.photouploader.auth

/*
The Mandatory "Privacy Policy" URL
Even for a personal or internal app, if you use OAuth, Google
demands a Privacy Policy URL on the Consent Screen configuration.
•The Requirement: You cannot just leave this blank if you want to verify the app or even look
 professional on the consent screen.
•The "Fat Client" Trap: Since your app is a desktop app, you might not have a website.
•The Solution: You need a simple static page (GitHub Pages is free) that states:
  ◦What data you access (Photos).
  ◦That you don't store it on servers (since you have no backend).
  ◦That tokens are stored locally.
  ◦How a user can request deletion (revoke access).

Google's "Sensitive Scopes" Branding RequirementsBecause photos library is a restricted scope,
Google is extremely strict about how you use their logo and branding within your app's UI.
•The "Sign In with Google" Button: You cannot just make a blue button that says "Login". It must
 follow the Google Identity Branding Guidelines pixel-perfectly.
  ◦Incorrect: A generic text button.
  ◦Correct: The official SVG logo on a white or blue rectangular pill.
•The "Google Photos" Logo: You generally cannot use the official Google Photos "pinwheel" logo as
 your own app icon or prominent UI element to imply partnership.
  ◦Risk: If your app icon looks too much like Google Photos, Google Play/Apple App Store will
   reject it for "Impersonation," and the Google API team can revoke your credentials during a
   manual review.
Recommendation: Ensure your GoogleSignInButton composable (which I see commented out in App.kt)
uses the official assets provided by Google, and ensure your App Icon is distinctively "True Pine"
and not "Google-ish."
 */
interface GoogleAuthService {
    /**
     * Triggers the sign-in flow.
     * If the user is already signed in and valid, then the valid token will be returned.
     * @Return the OAuth 2.0 Access Token (Bearer Token) as a String, or null if sign-in failed or
     * was cancelled.
     */
    suspend fun signIn(): String?
    
    suspend fun signOut()
    
    /**
     * Checks if the user is already signed in and valid.
     * @Return the Access Token if valid, null otherwise.
     */
    suspend fun restoreSignIn(): String?
}
