package com.example.gestordearchivos

import android.content.Context

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun addFavorite(path: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.add(path)
        prefs.edit().putStringSet("files", favorites).apply()
    }

    fun removeFavorite(path: String) {
        val favorites = getFavorites().toMutableSet()
        favorites.remove(path)
        prefs.edit().putStringSet("files", favorites).apply()
    }

    fun isFavorite(path: String): Boolean {
        return getFavorites().contains(path)
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet("files", emptySet()) ?: emptySet()
    }
}
