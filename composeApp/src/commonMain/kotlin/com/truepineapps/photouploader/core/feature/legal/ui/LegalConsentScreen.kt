package com.truepineapps.photouploader.core.feature.legal.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.truepineapps.photouploader.app.theme.AppTheme
import com.truepineapps.photouploader.core.feature.legal.domain.model.LegalContent
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalIntent
import com.truepineapps.photouploader.core.feature.legal.viewmodel.LegalUiState
import com.truepineapps.photouploader.core.presentation.component.MarkDownText
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.legal_backup_acknowledgement
import com.truepineapps.photouploader.resources.legal_button_accept
import com.truepineapps.photouploader.resources.legal_checkbox_privacy
import com.truepineapps.photouploader.resources.legal_checkbox_terms
import com.truepineapps.photouploader.resources.legal_title_first_launch
import com.truepineapps.photouploader.resources.legal_title_update
import com.truepineapps.photouploader.resources.privacy_policy
import com.truepineapps.photouploader.resources.terms_of_service
import org.jetbrains.compose.resources.stringResource

// TODO: replace dimensions with constants
@Composable
fun LegalConsentScreen(
    state: LegalUiState.ShowLegal,
    onIntent: (LegalIntent) -> Unit,
) {
    val outerScroll = rememberScrollState()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimensions.padding_medium)
                .verticalScroll(outerScroll),
            verticalArrangement = Arrangement.spacedBy(Dimensions.padding_large),
        ) {
            Spacer(Modifier.height(Dimensions.padding_medium))

            Text(
                text = if (state.isUpdate)
                    stringResource(Res.string.legal_title_update)
                else
                    stringResource(Res.string.legal_title_first_launch),
                style = MaterialTheme.typography.headlineSmall,
            )

            // ── Terms of Service ──────────────────────────────────────────
            LegalSection(
                title = stringResource(Res.string.terms_of_service),
                body = state.content.termsOfService,
                isChecked = state.termsChecked,
                checkLabel = stringResource(Res.string.legal_checkbox_terms),
                onScrolledToBottom = { onIntent(LegalIntent.TermsScrolledToBottom) },
                onChecked = { onIntent(LegalIntent.TermsChecked(it)) },
            )

            // ── Privacy Policy ────────────────────────────────────────────
            LegalSection(
                title = stringResource(Res.string.privacy_policy),
                body = state.content.privacyPolicy,
                isChecked = state.privacyChecked,
                checkLabel = stringResource(Res.string.legal_checkbox_privacy),
                onScrolledToBottom = { onIntent(LegalIntent.PrivacyScrolledToBottom) },
                onChecked = { onIntent(LegalIntent.PrivacyChecked(it)) },
            )

            // ── Backup Acknowledgement ────────────────────────────────────
            BackupAcknowledgementRow(
                isChecked = state.backupChecked,
                onChecked = { onIntent(LegalIntent.BackupChecked(it)) },
            )

            // ── Accept button ─────────────────────────────────────────────
            Button(
                onClick = { onIntent(LegalIntent.Accept) },
                enabled = state.canAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimensions.padding_large),
            ) {
                Text(stringResource(Res.string.legal_button_accept))
            }
        }
    }
}

@Composable
private fun LegalSection(
    title: String,
    body: String,
    isChecked: Boolean,
    checkLabel: String,
    onScrolledToBottom: () -> Unit,
    onChecked: (Boolean) -> Unit,
) {
    val scrollState = rememberScrollState()
    val hasScrolledToBottom by remember {
        derivedStateOf { scrollState.value >= scrollState.maxValue - 8 }
    }

    LaunchedEffect(hasScrolledToBottom) {
        if (hasScrolledToBottom) onScrolledToBottom()
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimensions.padding_medium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.padding_medium_extra)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.text_area_height)
                    .border(
                        width = Dimensions.border_width,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(Dimensions.padding_small)
            ) {
                MarkDownText(
                    markdown = body,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isChecked, onCheckedChange = onChecked)
                Spacer(Modifier.width(Dimensions.padding_small))
                Text(text = checkLabel, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun BackupAcknowledgementRow(
    isChecked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.padding_medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = isChecked, onCheckedChange = onChecked)
            Spacer(Modifier.width(Dimensions.padding_small))
            Text(
                text = stringResource(Res.string.legal_backup_acknowledgement),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewLegalConsentScreen() {
    AppTheme {
        LegalConsentScreen(
            state = LegalUiState.ShowLegal(
                content = LegalContent(
                    latestVersion = "01-01-2000",
                    termsOfService = "TODO()",
                    privacyPolicy = "TODO()"
                ),
                isUpdate = false,
                termsScrolled = false,
                termsChecked = false,
                privacyScrolled = false,
                privacyChecked = false,
                backupChecked = false
            ),
            onIntent = {}
        )
    }
}

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun PreviewLegalConsentScreenMD() {
    AppTheme {
        LegalConsentScreen(
            state = LegalUiState.ShowLegal(
                content = LegalContent(
                    latestVersion = "01-01-2000",
                    termsOfService =
                        """
# Terms of Service for Photo-Uploader

**Effective Date:** April 17, 2026

---

### 1. Introduction

This document describes the Terms of Service ("Terms") for **Photo-Uploader** ("the App"),
developed and maintained by **Marcel van Heerwaarden**, trading as **True Pine Apps**
("the Developer").

The App is currently in a **testing phase** and should be treated as beta software.

                        """,
                    privacyPolicy =
                        """
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
                    """
                ),
                isUpdate = false,
                termsScrolled = false,
                termsChecked = false,
                privacyScrolled = false,
                privacyChecked = false,
                backupChecked = false
            ),
            onIntent = {}
        )
    }
}