@file:JvmName("KickLoginScreenKt")

package com.jurianoff.irlmate.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KickEnterUsernameScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kick_login_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.enter_kick_login),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it.trimStart()
                    showError = false
                },
                isError = showError,
                singleLine = true,
                label = { Text(stringResource(R.string.username_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            )

            if (showError) {
                Text(
                    text = stringResource(R.string.kick_login_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmed = username.trim()
                    if (trimmed.isEmpty() || trimmed.contains(" ")) {
                        showError = true
                    } else {
                        context.getSharedPreferences("kick_auth", Context.MODE_PRIVATE)
                            .edit().putString("username", trimmed).apply()

                        val loginUrl = Uri.parse("https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/auth/kick/start?username=$trimmed")
                        val intent = Intent(Intent.ACTION_VIEW, loginUrl).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // zabezpieczenie Compose
                        }

                        context.startActivity(intent)

                        // 👇 NIE cofamy się — wrócimy z KickAuthRedirectActivity
                        // onBack() tutaj NIE jest potrzebne
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cont))
            }
        }
    }
}
