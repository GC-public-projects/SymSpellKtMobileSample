package com.example.symspellktmobilesample

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.symspellkt.api.SpellChecker
import com.darkrockstudios.symspellkt.common.SuggestionItem
import com.darkrockstudios.symspellkt.common.Verbosity
import com.darkrockstudios.symspellkt.impl.SymSpell
import com.darkrockstudios.symspellkt.impl.loadBiGramLine
import com.darkrockstudios.symspellkt.impl.loadUniGramLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class MyViewModel : ViewModel() {
    private val _spellChecker = MutableStateFlow<SpellChecker?>(null)
    val spellChecker = _spellChecker.asStateFlow()

    private val _loadingState = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    private var _totalLinesUnigram = 80000
    private var _progressionUnigram = MutableStateFlow<Int>(0)
    val progressionUnigram = _progressionUnigram.asStateFlow()

    private var _totalLinesBigram = 242342
    private var _progressionBigram = MutableStateFlow<Int>(0)
    val progressionBigram = _progressionBigram.asStateFlow()


    fun initializeSpellChecker(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            _loadingState.value = true
            val checker = SymSpell()

            val elapsedTime = measureTimeMillis {
                // Launch parallel dictionary loading
                val unigramJob = async { loadUnigramDictionary(context, checker) }
                val bigramJob = async { loadBigramDictionary(context, checker) }

                // Wait for both to complete
                unigramJob.await()
                bigramJob.await()
            }

            println("Dictionaries loaded in $elapsedTime ms")
            _spellChecker.value = checker
            _loadingState.value = false
        }
    }

private suspend fun loadUnigramDictionary(context: Context, checker: SpellChecker) {
    val assetManager = context.assets
    val progressInterval = _totalLinesUnigram / 100 // Update every 1%
    var index = 0
    withContext(Dispatchers.IO) {
        assetManager.open("files/en-80k.txt").bufferedReader().use { reader ->
            val batchSize = 1000
            val batch = mutableListOf<String>()

            reader.forEachLine{ line ->
                batch.add(line)
                if (batch.size >= batchSize) {
                    batch.forEach {
                        checker.dictionary.loadUniGramLine(it)
                        index += 1
                        if(index % progressInterval == 0) {
                            _progressionUnigram.value += 1
                        }
                    }
                    batch.clear()
                }
            }
            // Process remaining lines
            batch.forEach {
                checker.dictionary.loadUniGramLine(it)
                index += 1
                if(index % progressInterval == 0) {
                    _progressionUnigram.value += 1
                }
            }
        }
    }
}

    private suspend fun loadBigramDictionary(context: Context, checker: SpellChecker) {
        val assetManager = context.assets
        val progressInterval = _totalLinesBigram/ 100 // Update every 1%
        var index = 0
        withContext(Dispatchers.IO) {
            assetManager.open("files/frequency_bigramdictionary_en_243_342.txt").bufferedReader()
                .use { reader ->
                    val batchSize = 1000
                    val batch = mutableListOf<String>()

                    reader.forEachLine { line ->
                        batch.add(line)
                        if (batch.size >= batchSize) {
                            batch.forEach {
                                checker.dictionary.loadBiGramLine(it)
                                index +=1
                                if(index % progressInterval == 0) {
                                    _progressionBigram.value += 1
                                }
                            }
                            batch.clear()
                        }
                    }
                    // Process remaining lines
                    batch.forEach {
                        checker.dictionary.loadBiGramLine(it)
                        index +=1
                        if(index % progressInterval == 0) {
                            _progressionBigram.value += 1
                        }
                    }
                }
        }
    }


    fun getSuggestionsSingleWord(phrase: String): Pair<List<SuggestionItem>, Double> {
        val checker = _spellChecker.value ?: return emptyList<SuggestionItem>() to 0.0
        var items: List<SuggestionItem> = emptyList()
        val time = measureTimeMillis {
            items = checker.lookup(phrase.trim().lowercase(), Verbosity.All, 2.0)
        }
        return items to time.toDouble()
    }

    fun getSuggestionsMultiWord(phrase: String): Pair<List<SuggestionItem>, Double> {
        val checker = _spellChecker.value
            ?: return emptyList<SuggestionItem>() to 0.0
        var items: List<SuggestionItem> = emptyList()
        val time = measureTimeMillis {
            items = checker.lookupCompound(phrase.trim())
        }
        return items to time.toDouble()
    }

}