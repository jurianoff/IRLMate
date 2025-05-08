
package com.jurianoff.irlmate.ui.settings

import android.app.Activity
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

    var selectedLanguage by remember { mutableStateOf(ThemeSettings.languageCode) }
    var keepScreenOn by remember { mutableStateOf(ThemeSettings.keepScreenOn) }
    val scrollState = rememberScrollState()

    val isTwitchLoggedIn = remember { mutableStateOf(TwitchSession.isLoggedIn()) }
    val twitchUsername = remember { mutableStateOf(TwitchSession.username ?: "") }
    var showTwitch by remember { mutableStateOf(TwitchSession.showChatAndStatus) }

    val isKickLoggedIn = remember { mutableStateOf(KickSession.isLoggedIn()) }
    val kickUsername = remember { mutableStateOf(KickSession.username ?: "") }
    var showKick by remember { mutableStateOf(KickSession.showChatAndStatus) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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

            if (!isKickLoggedIn.value) {
                PlatformLoginRow(
                    iconRes = R.drawable.ic_kick_logo,
                    label = "Kick.com",
                    onClick = onNavigateToKickLogin
                )
            } else {
                PlatformSwitchRow(
                    label = "Kick.com",
                    iconRes = R.drawable.ic_kick_logo,
                    show = showKick,
                    onToggle = {
                        showKick = it
                        coroutineScope.launch {
                            KickSession.setShowChatAndStatus(context, it)
                        }
                    }
                )
                LoggedInRow(
                    username = kickUsername.value,
                    onLogout = {
                        coroutineScope.launch {
                            KickSession.clearSession(context)
                            isKickLoggedIn.value = false
                            kickUsername.value = ""
                            Toast.makeText(context, context.getString(R.string.kick_logout_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            if (!isTwitchLoggedIn.value) {
                PlatformLoginRow(
                    iconRes = R.drawable.ic_twitch_logo,
                    label = "Twitch",
                    onClick = onNavigateToTwitchLogin
                )
            } else {
                PlatformSwitchRow(
                    label = "Twitch",
                    iconRes = R.drawable.ic_twitch_logo,
                    show = showTwitch,
                    onToggle = {
                        showTwitch = it
                        coroutineScope.launch {
                            TwitchSession.setShowChatAndStatus(context, it)
                        }
                    }
                )
                LoggedInRow(
                    username = twitchUsername.value,
                    onLogout = {
                        coroutineScope.launch {
                            TwitchSession.clearSession(context)
                            isTwitchLoggedIn.value = false
                            twitchUsername.value = ""
                            Toast.makeText(context, context.getString(R.string.twitch_logout_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            ThemeModeOption(stringResource(R.string.light_theme), stringResource(R.string.theme_light_desc), ThemeSettings.darkMode == ThemeMode.LIGHT) {
                ThemeSettings.darkMode = ThemeMode.LIGHT
                coroutineScope.launch { ThemeSettings.saveTheme(context) }
            }
            ThemeModeOption(stringResource(R.string.dark_theme), stringResource(R.string.theme_dark_desc), ThemeSettings.darkMode == ThemeMode.DARK) {
                ThemeSettings.darkMode = ThemeMode.DARK
                coroutineScope.launch { ThemeSettings.saveTheme(context) }
            }
            ThemeModeOption(stringResource(R.string.system_theme), stringResource(R.string.theme_system_desc), ThemeSettings.darkMode == ThemeMode.SYSTEM) {
                ThemeSettings.darkMode = ThemeMode.SYSTEM
                coroutineScope.launch { ThemeSettings.saveTheme(context) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.keep_screen_on), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

            ListItem(
                headlineContent = { Text(stringResource(R.string.keep_screen_on)) },
                supportingContent = { Text(stringResource(R.string.keep_screen_on_desc)) },
                trailingContent = {
                    Switch(checked = keepScreenOn, onCheckedChange = {
                        keepScreenOn = it
                        ThemeSettings.keepScreenOn = it
                        coroutineScope.launch { ThemeSettings.saveTheme(context) }
                    })
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

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

@Composable
private fun PlatformLoginRow(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = onClick) {
            Text(stringResource(R.string.login))
        }
    }
}

@Composable
private fun PlatformSwitchRow(label: String, iconRes: Int, show: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
        Switch(checked = show, onCheckedChange = onToggle)
    }
}

@Composable
private fun LoggedInRow(username: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
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
