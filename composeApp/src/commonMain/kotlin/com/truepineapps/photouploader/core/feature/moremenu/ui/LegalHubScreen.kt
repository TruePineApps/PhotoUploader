package com.truepineapps.photouploader.core.feature.moremenu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.truepineapps.photouploader.core.feature.moremenu.navigation.LegalDestination
import com.truepineapps.photouploader.core.presentation.design.Dimensions
import com.truepineapps.photouploader.resources.Res
import com.truepineapps.photouploader.resources.licenses
import com.truepineapps.photouploader.resources.privacy_policy
import com.truepineapps.photouploader.resources.terms_of_service
import org.jetbrains.compose.resources.stringResource

@Composable
fun LegalHubScreen(
    onNavigateToLicense: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onUpdateTopAppBar: (String, (() -> Unit)?, @Composable (RowScope.() -> Unit)) -> Unit,
    modifier: Modifier = Modifier,
) {
    onUpdateTopAppBar(stringResource(LegalDestination.titleRes), null) {}

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimensions.padding_medium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.padding_medium)
    ) {
        Button(onClick = onNavigateToLicense, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.licenses))
        }
        Button(onClick = onNavigateToTerms, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.terms_of_service))
        }
        Button(onClick = onNavigateToPrivacy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.privacy_policy))
        }
    }
}
