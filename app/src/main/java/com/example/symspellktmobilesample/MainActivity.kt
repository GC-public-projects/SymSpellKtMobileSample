package com.example.symspellktmobilesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.symspellktmobilesample.ui.theme.SymSpellKtMobileSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SymSpellKtMobileSampleTheme {
                val viewModel: MyViewModel = viewModel()
                val context = LocalContext.current

                // Initialize the SpellChecker when the activity starts
                LaunchedEffect(Unit) {
                     viewModel.initializeSpellChecker(context)
                }

                val spellChecker by viewModel.spellChecker.collectAsStateWithLifecycle()

                // progressing bar purpose values
                val loading by viewModel.loadingState.collectAsStateWithLifecycle()
                val progressionUnigram by viewModel.progressionUnigram.collectAsStateWithLifecycle()
                val progressionBigram by viewModel.progressionBigram.collectAsStateWithLifecycle()

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if(loading) {
                        Column {
                            Text("Loading dictionaries...")
                            Text("unigram : $progressionUnigram %")
                            Text("bigram : $progressionBigram %")
                        }

                    } else {
                        spellChecker?.let {
                            SingleWordCorrectionUi(viewModel)
                            Spacer(Modifier.height(25.dp))
                            MultiWordCorrectionUi(viewModel)
                        }
                    }
                }
            }
        }
    }
}

