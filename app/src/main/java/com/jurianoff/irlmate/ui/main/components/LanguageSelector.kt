package com.jurianoff.irlmate.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jurianoff.irlmate.R
import com.jurianoff.irlmate.ui.settings.ThemeSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(ThemeSettings.languageCode) }

    val rawLanguages = listOf(
        "br" to R.string.language_portuguese,
        "cn" to R.string.language_chinese,
        "de" to R.string.language_german,
        "en" to R.string.language_english,
        "es" to R.string.language_spanish,
        "fr" to R.string.language_french,
        "in" to R.string.language_hindi,
        "it" to R.string.language_italian,
        "pl" to R.string.language_polish,
        "ru" to R.string.language_russian,
        "tr" to R.string.language_turkish,
        "ua" to R.string.language_ukrainian
    )

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                val selectedLabel = rawLanguages.firstOrNull { it.first == selectedLanguage }?.second
                val selectedName = selectedLabel?.let { stringResource(it) } ?: ""

                TextField(
                    readOnly = true,
                    value = selectedName,
                    onValueChange = {},
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )

                // 🔧 Sortujemy podczas renderowania – bezpośrednio w forEach
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    rawLanguages
                        .map { it.first to (it.second to stringResource(it.second)) }
                        .sortedBy { it.second.second.lowercase() }
                        .forEach { (code, pair) ->
                            val labelRes = pair.first
                            val labelText = pair.second

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(labelText)
                                        if (code == selectedLanguage) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedLanguage = code
                                    ThemeSettings.languageCode = code
                                    coroutineScope.launch { ThemeSettings.saveLanguage(context) }
                                    expanded = false
                                }
                            )
                        }
                }
            }
        }
    }
}
