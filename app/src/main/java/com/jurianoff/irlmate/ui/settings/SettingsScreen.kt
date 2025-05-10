package com.jurianoff.irlmate.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.jurianoff.irlmate.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToKickLogin: () -> Unit,
    onNavigateToTwitchLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedLanguage by remember { mutableStateOf(ThemeSettings.languageCode) }
    var keepScreenOn by remember { mutableStateOf(ThemeSettings.keepScreenOn) }

    val isTwitchLoggedIn by rememberUpdatedState(TwitchSession.isLoggedIn())
    val twitchUsername by rememberUpdatedState(TwitchSession.username ?: "")
    var showTwitch by remember { mutableStateOf(TwitchSession.showChatAndStatus) }

    val isKickLoggedIn by rememberUpdatedState(KickSession.isLoggedIn())
    val kickUsername by rememberUpdatedState(KickSession.username ?: "")
    var showKick by remember { mutableStateOf(KickSession.showChatAndStatus) }

    val darkModeKey = ThemeSettings.darkMode // ensures recomposition when theme changes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        /* ▶ wymuszamy pełną recompozycję po zmianie motywu */
        key(ThemeSettings.darkMode) {

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                /* ---------- Sekcja PLATFORMY ---------- */
                SettingsSectionHeader(title = stringResource(R.string.platforms))

                PlatformCard(
                    iconRes = R.drawable.ic_kick_logo,
                    label = "Kick.com",
                    isLoggedIn = isKickLoggedIn,
                    username = kickUsername,
                    showToggle = showKick,
                    onLogin = onNavigateToKickLogin,
                    onLogout = {
                        coroutineScope.launch {
                            KickSession.clearSession(context)
                            showKick = false
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.kick_logout_success),
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    },
                    onToggle = {
                        showKick = it
                        coroutineScope.launch { KickSession.setShowChatAndStatus(context, it) }
                    }
                )

                PlatformCard(
                    iconRes = R.drawable.ic_twitch_logo,
                    label = "Twitch",
                    isLoggedIn = isTwitchLoggedIn,
                    username = twitchUsername,
                    showToggle = showTwitch,
                    onLogin = onNavigateToTwitchLogin,
                    onLogout = {
                        coroutineScope.launch {
                            TwitchSession.clearSession(context)
                            showTwitch = false
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.twitch_logout_success),
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    },
                    onToggle = {
                        showTwitch = it
                        coroutineScope.launch { TwitchSession.setShowChatAndStatus(context, it) }
                    }
                )

                /* ---------- Sekcja WYGLĄD ---------- */
                SettingsSectionHeader(title = stringResource(R.string.appearance))

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        ThemeModeOption(
                            stringResource(R.string.light_theme),
                            stringResource(R.string.theme_light_desc),
                            ThemeSettings.darkMode == ThemeMode.LIGHT
                        ) {
                            ThemeSettings.darkMode = ThemeMode.LIGHT
                            coroutineScope.launch { ThemeSettings.saveTheme(context) }
                        }
                        ThemeModeOption(
                            stringResource(R.string.dark_theme),
                            stringResource(R.string.theme_dark_desc),
                            ThemeSettings.darkMode == ThemeMode.DARK
                        ) {
                            ThemeSettings.darkMode = ThemeMode.DARK
                            coroutineScope.launch { ThemeSettings.saveTheme(context) }
                        }
                        ThemeModeOption(
                            stringResource(R.string.system_theme),
                            stringResource(R.string.theme_system_desc),
                            ThemeSettings.darkMode == ThemeMode.SYSTEM
                        ) {
                            ThemeSettings.darkMode = ThemeMode.SYSTEM
                            coroutineScope.launch { ThemeSettings.saveTheme(context) }
                        }
                    }
                }

                /* -- „Nie wygaszaj ekranu” w karcie -- */
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
                }

                /* ---------- Sekcja JĘZYK ---------- */
                SettingsSectionHeader(title = stringResource(R.string.language))

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        LanguageOption("Polski", selectedLanguage == "pl") {
                            selectedLanguage = "pl"
                            ThemeSettings.languageCode = "pl"
                            coroutineScope.launch { ThemeSettings.saveLanguage(context) }
                        }
                        LanguageOption("English", selectedLanguage == "en") {
                            selectedLanguage = "en"
                            ThemeSettings.languageCode = "en"
                            coroutineScope.launch { ThemeSettings.saveLanguage(context) }
                        }
                    }
                }
            } /* ← Column */

        } /* ← key */
    }
}

    @Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun PlatformCard(
    iconRes: Int,
    label: String,
    isLoggedIn: Boolean,
    username: String,
    showToggle: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, style = MaterialTheme.typography.titleMedium)
                }
                if (isLoggedIn) {
                    Switch(checked = showToggle, onCheckedChange = onToggle)
                } else {
                    TextButton(onClick = onLogin) {
                        Text(stringResource(R.string.login))
                    }
                }
            }

            if (isLoggedIn) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.logged_in_as, username),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onLogout) {
                        Text(stringResource(R.string.logout))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = { RadioButton(selected = selected, onClick = onClick) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LanguageOption(title: String, selected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { RadioButton(selected = selected, onClick = onClick) },
        modifier = Modifier.fillMaxWidth()
    )
}