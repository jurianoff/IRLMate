package com.jurianoff.irlmate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import com.jurianoff.irlmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf(ThemeSettings.languageCode) }
    var keepScreenOn by remember { mutableStateOf(ThemeSettings.keepScreenOn) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeModeOption(
                title = stringResource(R.string.light_theme),
                description = "Używaj jasnego motywu",
                selected = ThemeSettings.darkMode == ThemeMode.LIGHT,
                onClick = {
                    ThemeSettings.darkMode = ThemeMode.LIGHT
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            ThemeModeOption(
                title = stringResource(R.string.dark_theme),
                description = "Używaj ciemnego motywu",
                selected = ThemeSettings.darkMode == ThemeMode.DARK,
                onClick = {
                    ThemeSettings.darkMode = ThemeMode.DARK
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            ThemeModeOption(
                title = stringResource(R.string.system_theme),
                description = "Dopasuj do ustawień systemowych",
                selected = ThemeSettings.darkMode == ThemeMode.SYSTEM,
                onClick = {
                    ThemeSettings.darkMode = ThemeMode.SYSTEM
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.keep_screen_on),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.keep_screen_on)) },
                supportingContent = { Text(stringResource(R.string.keep_screen_on_desc)) },
                trailingContent = {
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = {
                            keepScreenOn = it
                            ThemeSettings.keepScreenOn = it
                            coroutineScope.launch { ThemeSettings.saveTheme(context) }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LanguageOption(
                title = stringResource(R.string.language_polish),
                selected = selectedLanguage == "pl",
                onClick = {
                    selectedLanguage = "pl"
                    ThemeSettings.languageCode = "pl"
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            LanguageOption(
                title = stringResource(R.string.language_english),
                selected = selectedLanguage == "en",
                onClick = {
                    selectedLanguage = "en"
                    ThemeSettings.languageCode = "en"
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )
        }
    }
}

@Composable
private fun ThemeModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LanguageOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}
