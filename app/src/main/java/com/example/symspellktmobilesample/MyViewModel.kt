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

    private var _totalLinesUnigram = MutableStateFlow<Int>(80000)
    val totalLinesUnigram = _totalLinesUnigram.asStateFlow()
    private var _currentLineUnigram = MutableStateFlow<Int>(0)
    val currentLineUnigram = _currentLineUnigram.asStateFlow()

    private var _totalLinesBigram = MutableStateFlow<Int>(242342)
    val totalLinesBigram = _totalLinesBigram.asStateFlow()
    private var _currentLineBigram = MutableStateFlow<Int>(0)
    val currentLineBigram = _currentLineBigram.asStateFlow()


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
        withContext(Dispatchers.IO) {
            assetManager.open("files/en-80k.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    checker.dictionary.loadUniGramLine(line)
                    _currentLineUnigram.value++
                }
            }
        }
    }

    private suspend fun loadBigramDictionary(context: Context, checker: SpellChecker) {
        val assetManager = context.assets
        withContext(Dispatchers.IO) {
            assetManager.open("files/frequency_bigramdictionary_en_243_342.txt").bufferedReader()
                .useLines { lines ->
                    lines.forEach { line ->
                        checker.dictionary.loadBiGramLine(line)
                        _currentLineBigram.value++
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