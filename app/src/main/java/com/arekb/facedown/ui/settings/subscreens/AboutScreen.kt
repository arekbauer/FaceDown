package com.arekb.facedown.ui.settings.subscreens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.arekb.facedown.BuildConfig
import com.arekb.facedown.R
import com.arekb.facedown.ui.settings.components.FaceDownListItem
import com.arekb.facedown.ui.settings.components.ItemPosition

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Helper to launch Play Store for rating
    fun launchPlayStore() {
        val appPackageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                "market://details?id=$appPackageName".toUri()))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appPackageName".toUri()))
        }
    }

    fun launchOssLicenses() {
        try {
            val intent = Intent(context, Class.forName("com.google.android.gms.oss.licenses.OssLicensesMenuActivity"))
            intent.putExtra("title", "Open source licenses")

            context.startActivity(intent)
        } catch (e: Exception) {
            // This catches crashes if the plugin/library isn't properly synced
            Toast.makeText(context, "License info not available", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("About FaceDown") },
                subtitle = { Text(stringResource(R.string.toolbar_settings_label)) },
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            FaceDownListItem(
                topText = "Support me",
                title = "Rate FaceDown",
                subtitle = "Leave a review on the Play Store",
                icon = R.drawable.settings_rate,
                position = ItemPosition.Single,
                onClick = { launchPlayStore() },
                trailingContent = null
            )

            Spacer(modifier = Modifier.height(24.dp))

            FaceDownListItem(
                topText = "Legal",
                title = "Privacy policy",
                icon = R.drawable.settings_privacy,
                position = ItemPosition.Top,
                onClick = {
                    // Replace with your actual hosted policy URL
                    uriHandler.openUri("https://your-privacy-policy-url.com")
                },
                trailingContent = null
            )

            FaceDownListItem(
                title = "Open source licenses",
                icon = R.drawable.settings_license,
                position = ItemPosition.Bottom,
                onClick = { launchOssLicenses() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Footer (Version & Credit)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}