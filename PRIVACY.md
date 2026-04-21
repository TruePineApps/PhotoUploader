# Privacy Policy for Photo-Uploader

**Effective Date:** April 3, 2026

---

### 1. Introduction and Identity

This Privacy Policy describes how **Marcel van Heerwaarden**, trading as **True Pine Apps**
("the Developer"), handles personal data within the application **Photo-Uploader** ("the
App"). The App is a tool designed to upload photos to Google Photos and organize them into
albums based on local directory structures.

As a developer based in the Netherlands, I am committed to protecting your privacy in
accordance with the General Data Protection Regulation (GDPR). The Developer is the data
controller for the personal data described in this policy.

**Contact:**  
**Marcel van Heerwaarden**, trading as **True Pine Apps**  
KvK: 98723316  
Email: marcel@truepineapps.com  
Project: https://github.com/truepineapps/photouploader  
Full contact details: https://truepineapps.com/en/imprint

---

### 2. What Personal Data Is Collected and Why

The only personal data the Developer directly collects is your **Google email address**.

Your email address is collected for the following purposes:

- To add you to the authorized testing whitelist in Google Cloud Console, which is required
  for you to authenticate and use the App.
- To contact you about the status of your access (for example, if your access is withdrawn
  or for notifications on changes in data usage).

**Legal basis:** Processing your email address is necessary to provide you access to the App
and manage the testing service. The legal basis is [Article 6(1)(b) GDPR](https://gdpr-info.eu/art-6-gdpr/)
— processing necessary for the performance of a service at your request.

Where processing is not strictly necessary for the performance of the service — such as
sending notifications about access changes — it is based on the Developer's legitimate
interest in managing access to the App and ensuring its proper functioning
([Article 6(1)(f) GDPR](https://gdpr-info.eu/art-6-gdpr/)).

The Developer does **not** collect passwords, payment data, location data, or any other
personal information.

---

### 3. Photo Processing

When you use the App, your photos are processed locally on your device. The App reads your
photo files from your local storage, processes them in memory, and uploads them directly to
your own Google Photos account via the Google Photos API. At no point are your photos stored
on, or transmitted to, any server controlled by the Developer.

You remain the sole owner of your photos. You maintain full control over your photos and
data within Google Photos. You can manage, view, and delete your photos directly through
Google Photos according to Google's policies. The Developer never has access to your photo
content.

---

### 4. Google API Scopes and Usage

The App requests access to your Google Account to perform the actions required for its core
functionality. The following access scopes are used:

- **`photoslibrary.appendonly`** — To upload photos and create albums in your Google Photos
  library.
- **`photoslibrary.readonly.appcreateddata`** — To track albums and photos created by the
  App, in order to prevent duplicate uploads.
- **`photoslibrary.edit.appcreateddata`** — To set album covers and organize albums created
  by the App.
- **`userinfo.profile`** — To display your name and avatar in the App interface, so you can
  confirm which account is active.
- **`userinfo.email`** — To identify your account, verify your authorization during the
  testing phase and distinguish between users with identical display names.

The App's use and transfer of information received from Google APIs complies with the
[Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy),
including the Limited Use requirements.

---

### 5. Google Cloud Platform, Google as Data Controller, and Logging

The App is registered on Google Cloud Platform (GCP), which hosts the App's Google API
credentials. GCP may retain standard administrative logs related to the operation of the
project itself, such as configuration changes. These logs do not contain any record of your
photo uploads or your use of the App.

The Developer has deliberately chosen not to enable access logging in GCP. This means that
no record is kept of when you log in, which photos you upload, or how often you use the App.
Combined with Section 3, this means the Developer holds neither your photos nor any record
of your activity within the App.

**Google acts as an independent data controller** for data processed through its own
services, including OAuth authentication, the Google Photos API, and Google Cloud Platform.
This means Google determines the purposes and means of processing your data within its own
infrastructure, independently of the Developer. The Developer has no control over what data
Google collects during authentication or API interactions, and cannot make representations
about Google's processing on Google's behalf.

Additional data — such as authentication metadata — may be processed by Google as part of
the OAuth process. For full details of how Google handles your data, please refer to
[Google's Privacy Policy](https://policies.google.com/privacy).

---

### 6. Data Transfers Outside the European Union

Google LLC is based in the United States. By using the App, your personal data (email
address, authentication metadata, and photo content) is processed by Google on servers that
may be located outside the European Economic Area (EEA).

Google has implemented appropriate safeguards for such transfers in accordance with GDPR
requirements, including Standard Contractual Clauses (SCCs) as approved by the European
Commission. For more information, see [Google's Privacy Policy](https://policies.google.com/privacy)
and [Google's Data Transfer Framework](https://business.safety.google/gdpr/).

---

### 7. Data Retention

| Data                                         | Retention Period                                                |
|----------------------------------------------|-----------------------------------------------------------------|
| Email address (active whitelist)             | Retained for the duration of your active testing access         |
| Email address (after removal from whitelist) | Deleted within **6 months** of removal                          |
| OAuth tokens                                 | Stored locally on your device only; the Developer has no access |

When your email address is deleted, no further record is kept by the Developer.

---

### 8. Special Provisions for the Testing Phase

Because the App currently has "Testing" status in Google Cloud Console, the following
conditions apply:

- **User Limit:** Google restricts the testing phase to a maximum of 100 concurrent users.
- **Access Rotation:** To allow as many people as possible to use the App, the authorized
  list is managed manually. If the 100-user limit is reached, access may be withdrawn to
  accommodate new requests.
- **Notification:** If your access is withdrawn, you will receive a short notification to
  your Google email address.
- **Re-application:** You are welcome to re-apply for access at any time by contacting the
  Developer.

---

### 9. Data Security

- **No Third-Party Sharing:** Your data is never sold, shared with third parties, used for
  advertising, or used for AI training.
- **Authentication Tokens:** OAuth refresh tokens are stored securely on your local device.
  The Developer never has access to your Google password or your login tokens.
- **Communication and Storage:** Your email address is used to manage your testing access,
  send you access status updates, and is stored in Google Cloud Console as part of the
  testing whitelist. It will not be used for marketing. For Google's role in storing this
  data, see Section 5.
- **Automated Decision-Making:** The App does not use automated decision-making or
  profiling as defined under [Article 22 GDPR](https://gdpr-info.eu/art-22-gdpr/).

---

### 10. Data Protection Impact Assessment

The Developer has assessed whether a Data Protection Impact Assessment (DPIA) is required
under [Article 35 GDPR](https://gdpr-info.eu/art-35-gdpr/).

Given that:

- the App processes photos exclusively on your local device,
- the Developer does not have access to your photo content, and
- the only personal data directly collected is your email address, used solely to manage
  your testing access,

the processing is not considered likely to result in a high risk to your rights and
freedoms. A formal DPIA has therefore not been conducted. This assessment will be revisited
if the functionality of the App changes materially.

---

### 11. Your Rights Under the GDPR

Under the GDPR, you have the following rights regarding your personal data. To exercise any
of these rights, contact the Developer at **marcel@truepineapps.com**.

- **Right to Access ([Article 15](https://gdpr-info.eu/art-15-gdpr/)):** You may request a
  copy of the personal data held about you. The Developer will provide your email address
  and the date it was registered.

- **Right to Erasure ([Article 17](https://gdpr-info.eu/art-17-gdpr/)):** You may request
  that your email address be removed from the testing whitelist. Note that removal also
  means you will no longer be able to use the App, as your email address is required for
  authentication.

- **Right to Restriction of Processing ([Article 18](https://gdpr-info.eu/art-18-gdpr/)):**
  You may request that the Developer restrict processing of your data. Because your email
  address is the only data held and is necessary to provide access to the App, restriction
  of processing means your access to the App will be suspended for the duration of the
  restriction.

- **Right to Object ([Article 21](https://gdpr-info.eu/art-21-gdpr/)):** You may object to
  the processing of your personal data. As your email address is necessary for the provision
  of the service you requested, an objection will result in the termination of your access
  to the App.

- **Right to Revoke Google Access:** You can revoke the App's access to your Google account
  at any time via your [Google Security Settings](https://myaccount.google.com/permissions).

- **Right to Lodge a Complaint:** You have the right to lodge a complaint with the Dutch
  Data Protection Authority (*Autoriteit Persoonsgegevens*) at
  [autoriteitpersoonsgegevens.nl](https://www.autoriteitpersoonsgegevens.nl).

The Developer will respond to any rights request within **one month** of receipt, as
required by [Article 12(3) GDPR](https://gdpr-info.eu/art-12-gdpr/).

---

### 12. Changes to This Policy

If this Privacy Policy is updated in a material way, the Developer will notify active
testers in the app and by email at least **14 days before** the changes take effect.
To enable in-app notifications, the App contacts truepineapps.com on launch solely to check
whether the Privacy Policy has been updated. If the check cannot be completed due to a
network issue, the App will proceed normally and retry on the next launch. No personal data
is transmitted in this request.

The updated policy will also be published at
[truepineapps.com/photouploader](https://truepineapps.com/photouploader) with a revised
effective date. Continued use of the App after the effective date constitutes acceptance of
the updated policy.

---

### 13. Contact Information

For questions about this policy, or to exercise your rights, contact:

**Marcel van Heerwaarden**, trading as **True Pine Apps**  
KvK: 98723316  
Email: marcel@truepineapps.com  
Project: https://github.com/truepineapps/photouploader  
Full contact details: https://truepineapps.com/en/imprint