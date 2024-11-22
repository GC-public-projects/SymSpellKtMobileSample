package com.example.symspellktmobilesample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

                val loading by viewModel.loadingState.collectAsStateWithLifecycle()
                val totalLinesUnigram by viewModel.totalLinesUnigram.collectAsStateWithLifecycle()
                val currentLineUnigram by viewModel.currentLineUnigram.collectAsStateWithLifecycle()
                val totalLinesBigram by viewModel.totalLinesBigram.collectAsStateWithLifecycle()
                val currentLineBigram by viewModel.currentLineBigram.collectAsStateWithLifecycle()



                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if(loading) {
                        Column {
                            Text("Loading dictionaries...")
                            Text("unigram : $currentLineUnigram / $totalLinesUnigram")
                            Text("bigram : $currentLineBigram / $totalLinesBigram")
                        }

                    } else {
                        spellChecker?.let {
                            SingleWordCorrectionUi(viewModel)
                            MultiWordCorrectionUi(viewModel)
                        }
                    }
                }
            }
        }
    }
}

