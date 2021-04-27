package com.tostech.artisan

import android.content.SearchRecentSuggestionsProvider

class SearchSuggestion: SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.tostech.artisan.SearchSuggestion"
        const val MODE: Int = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}