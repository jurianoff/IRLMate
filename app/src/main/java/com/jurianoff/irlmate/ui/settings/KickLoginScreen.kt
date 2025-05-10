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

    // Kolory niezależne od systemowego motywu – bazują wyłącznie na aktywnym schemacie
    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor        = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor      = MaterialTheme.colorScheme.onSurface,
        disabledTextColor       = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTextColor          = MaterialTheme.colorScheme.error,
        focusedContainerColor   = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        errorContainerColor     = MaterialTheme.colorScheme.surface,
        cursorColor             = MaterialTheme.colorScheme.primary,
        errorCursorColor        = MaterialTheme.colorScheme.error,
        focusedIndicatorColor   = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        errorIndicatorColor     = MaterialTheme.colorScheme.error,
        focusedLabelColor       = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor     = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kick_login_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
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
        ) {
            Text(
                text = stringResource(R.string.enter_kick_login),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(16.dp))

            TextField(
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
                shape = MaterialTheme.shapes.medium,
                colors = textFieldColors
            )

            if (showError) {
                Text(
                    text = stringResource(R.string.kick_login_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val trimmed = username.trim()
                    if (trimmed.isBlank() || trimmed.contains(' ')) {
                        showError = true
                    } else {
                        context.getSharedPreferences("kick_auth", Context.MODE_PRIVATE)
                            .edit().putString("username", trimmed).apply()

                        val loginUrl =
                            Uri.parse("https://ah2d6m1qy4.execute-api.eu-central-1.amazonaws.com/auth/kick/start?username=$trimmed")

                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, loginUrl)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cont))
            }
        }
    }
}
