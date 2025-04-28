package com.jurianoff.irlmate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Settings, contentDescription = "Wróć")
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
                text = "Motyw aplikacji",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeModeOption(
                title = "Tryb jasny",
                description = "Używaj jasnego motywu",
                selected = ThemeSettings.darkMode == ThemeMode.LIGHT,
                onClick = { ThemeSettings.darkMode = ThemeMode.LIGHT }
            )

            ThemeModeOption(
                title = "Tryb ciemny",
                description = "Używaj ciemnego motywu",
                selected = ThemeSettings.darkMode == ThemeMode.DARK,
                onClick = { ThemeSettings.darkMode = ThemeMode.DARK }
            )

            ThemeModeOption(
                title = "Automatyczny",
                description = "Dopasuj do ustawień systemowych",
                selected = ThemeSettings.darkMode == ThemeMode.SYSTEM,
                onClick = { ThemeSettings.darkMode = ThemeMode.SYSTEM }
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
