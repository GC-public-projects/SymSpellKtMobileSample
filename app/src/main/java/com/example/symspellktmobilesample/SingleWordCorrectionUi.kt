package com.example.symspellktmobilesample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SingleWordCorrectionUi(viewModel: MyViewModel) {
    var suggestions by remember { mutableStateOf("") }
    var searchTime by remember { mutableStateOf("") }
    var myText by remember { mutableStateOf("") }
    val modifyText = { text: String -> myText = text }

    Column {
        Text("Single Word Correction", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = myText,
            onValueChange = modifyText,
            label = { Text("Spell Checker") },
            placeholder = { Text("Type misspelled words") },
            singleLine = true,
        )

        LaunchedEffect(myText) {
            val phrase = myText.trim().lowercase()
            if (phrase.isNotEmpty()) {
                val (items, time) = viewModel.getSuggestionsSingleWord(phrase)
                searchTime = "Lookup Took: $time ms"
                suggestions = if (items.firstOrNull()?.term == phrase) {
                    "<No Misspelling>"
                } else {
                    items.joinToString("\n") { it.term }
                }
            } else {
                suggestions = ""
                searchTime = ""
            }
        }

        Text(searchTime, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Possible Corrections", style = MaterialTheme.typography.labelLarge)
        Text(suggestions, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.verticalScroll(rememberScrollState()))
    }
}

