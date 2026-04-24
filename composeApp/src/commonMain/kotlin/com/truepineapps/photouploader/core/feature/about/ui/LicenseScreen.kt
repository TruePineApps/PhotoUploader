package com.truepineapps.photouploader.core.feature.about.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.collapse_album
import com.truepineapps.photouploader.resources.error_loading_license
import com.truepineapps.photouploader.resources.expand_album
import com.truepineapps.photouploader.resources.font_licenses
import com.truepineapps.photouploader.resources.license_text
import com.truepineapps.photouploader.resources.licenses
import com.truepineapps.photouploader.resources.loading
import com.truepineapps.photouploader.resources.noto_sans
import com.truepineapps.photouploader.resources.third_party_notices
import com.truepineapps.photouploader.resources.unknown_error
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.core.presentation.component.ThemedIconButton
import com.truepineapps.photouploader.app.navigation.NavigationDestination
import com.truepineapps.photouploader.core.feature.about.viewmodel.LicenseViewModel
import com.truepineapps.photouploader.core.feature.about.viewmodel.LoadLicenseResult
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

object LicenseDestination : NavigationDestination {
    override val route = "license"
    override val titleRes = Res.string.licenses
}

@Composable
fun LicenseScreen(
    modifier: Modifier = Modifier,
    viewModel: LicenseViewModel = koinViewModel(),
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit = { _, _, _ -> },
) {
    // Collect states from ViewModel
    val fontResult by viewModel.fontResult.collectAsState()
    val noticesResult by viewModel.noticesResult.collectAsState()

    onUpdateTopAppBar(stringResource(LicenseDestination.titleRes), null) {}

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_medium),
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.padding_medium)
            .verticalScroll(rememberScrollState())
    ) {
        // Copyright
        PhotoUploaderCopyright()

        // Font license Section
        ExpandableLicenseSection(
            sectionHeaderText = stringResource(Res.string.font_licenses),
            loadingResult = fontResult,
            loadingMessage = stringResource(
                Res.string.loading,
                stringResource(Res.string.license_text)
            ),
            licenseHeaderText = stringResource(Res.string.noto_sans)
        )

        // Third Party Section
        ExpandableLicenseSection(
            sectionHeaderText = stringResource(Res.string.third_party_notices),
            loadingResult = noticesResult,
            loadingMessage = stringResource(
                Res.string.loading,
                stringResource(Res.string.licenses)
            )
        )
    }
}

@Composable
fun PhotoUploaderCopyright(modifier: Modifier = Modifier) {
    val apacheUrl = "https://www.apache.org/licenses/LICENSE-2.0"
    val annotatedString = buildAnnotatedString {
        append("PhotoUploader – © Copyright 2026 True Pine Apps\n")
        append("Licensed under the Apache License, Version 2.0\n")

        // Make only the URL clickable and styled
        withLink(
            LinkAnnotation.Url(
                url = apacheUrl,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(apacheUrl)
        }
    }

    // Allows users to copy the text if they want
    SelectionContainer(modifier = modifier) {
        Text(text = annotatedString)
    }
}


@Composable
private fun ExpandableLicenseSection(
    sectionHeaderText: String,
    loadingResult: LoadLicenseResult,
    loadingMessage: String,
    modifier: Modifier = Modifier,
    licenseHeaderText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { expanded = !expanded }
                .padding(Dimensions.padding_small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionHeaderText,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            ThemedIconButton(
                onClick = { expanded = !expanded },
                imageVector = if (expanded) Icons.Filled.UnfoldLess else Icons.Filled.UnfoldMore,
                contentDescriptionResource = if (expanded) Res.string.collapse_album else Res.string.expand_album,
                enabled = true
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(horizontal = Dimensions.padding_small)) {
                licenseHeaderText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = Dimensions.padding_small)
                    )
                }
                LicenseText(loadingResult, loadingMessage)
            }
        }
    }
}

// Format license text in monospace font
@Composable
private fun LicenseText(
    loadingResult: LoadLicenseResult,
    loadingMessage: String,
    modifier: Modifier = Modifier
) {
    // Use derivedStateOf to compute the text only when the Result changes
    val unknownErrorMsg = stringResource(Res.string.unknown_error)
    val errorFormatMsg = stringResource(Res.string.error_loading_license, "")

    val displayText by remember(loadingResult) {
        derivedStateOf {
            when (loadingResult) {
                is LoadLicenseResult.Loading -> loadingMessage
                is LoadLicenseResult.Success -> loadingResult.licenseText
                is LoadLicenseResult.Error -> {
                    val message = loadingResult.exception.message ?: unknownErrorMsg
                    "$errorFormatMsg $message"
                }
            }
        }
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            lineHeight = TextUnit(14f, TextUnitType.Sp)
        ),
        modifier = modifier.padding(vertical = Dimensions.padding_medium)
    )
}
