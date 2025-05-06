package com.jurianoff.irlmate.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToKickLogin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context as? Activity

    var selectedLanguage by remember { mutableStateOf(ThemeSettings.languageCode) }
    var keepScreenOn by remember { mutableStateOf(ThemeSettings.keepScreenOn) }
    val scrollState = rememberScrollState()

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
                .verticalScroll(scrollState)
        ) {
            // Kick login/logout
            val isLoggedIn = remember { mutableStateOf(KickSession.isLoggedIn()) }
            val kickUsername = remember { mutableStateOf(KickSession.username ?: "") }
            var showKick by remember { mutableStateOf(KickSession.showChatAndStatus) }

            if (!isLoggedIn.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_kick_logo),
                            contentDescription = "Kick",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "Kick.com",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    TextButton(onClick = { onNavigateToKickLogin() }) {
                        Text(stringResource(R.string.login))
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kick.com",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = showKick,
                        onCheckedChange = {
                            showKick = it
                            coroutineScope.launch {
                                KickSession.setShowChatAndStatus(context, it)
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.logged_in_as, kickUsername.value),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                KickSession.clearSession(context)
                                isLoggedIn.value = false
                                kickUsername.value = ""

                                // ✅ tylko jeden Toast
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.kick_logout_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Motyw
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeModeOption(
                title = stringResource(R.string.light_theme),
                description = stringResource(R.string.theme_light_desc),
                selected = ThemeSettings.darkMode == ThemeMode.LIGHT,
                onClick = {
                    ThemeSettings.darkMode = ThemeMode.LIGHT
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            ThemeModeOption(
                title = stringResource(R.string.dark_theme),
                description = stringResource(R.string.theme_dark_desc),
                selected = ThemeSettings.darkMode == ThemeMode.DARK,
                onClick = {
                    ThemeSettings.darkMode = ThemeMode.DARK
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            ThemeModeOption(
                title = stringResource(R.string.system_theme),
                description = stringResource(R.string.theme_system_desc),
                selected = ThemeSettings.darkMode == ThemeMode.SYSTEM,
                onClick = {
                    ThemeSettings.darkMode = ThemeMode.SYSTEM
                    coroutineScope.launch { ThemeSettings.saveTheme(context) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Wygaszanie
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

            // Język
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LanguageOption(
                title = "Polski",
                selected = selectedLanguage == "pl",
                onClick = {
                    selectedLanguage = "pl"
                    ThemeSettings.languageCode = "pl"
                    coroutineScope.launch { ThemeSettings.saveLanguage(context) }
                }
            )

            LanguageOption(
                title = "English",
                selected = selectedLanguage == "en",
                onClick = {
                    selectedLanguage = "en"
                    ThemeSettings.languageCode = "en"
                    coroutineScope.launch { ThemeSettings.saveLanguage(context) }
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
        trailingContent = { RadioButton(selected = selected, onClick = onClick) },
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
        trailingContent = { RadioButton(selected = selected, onClick = onClick) },
        modifier = Modifier.fillMaxWidth()
    )
}
