package com.example.gestordearchivos

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("themes", Context.MODE_PRIVATE)

    companion object {
        const val GUINDA_SYSTEM = "guinda_system"
        const val GUINDA_LIGHT = "guinda_light"
        const val GUINDA_DARK = "guinda_dark"
        const val AZUL_SYSTEM = "azul_system"
        const val AZUL_LIGHT = "azul_light"
        const val AZUL_DARK = "azul_dark"
    }

    fun getTheme(): String {
        return prefs.getString("theme_preference", GUINDA_SYSTEM) ?: GUINDA_SYSTEM
    }

    fun setTheme(theme: String) {
        prefs.edit().putString("theme_preference", theme).apply()
    }

    fun applyTheme() {
        val theme = getTheme()
        
        // 1. Set the color theme (Guinda or Azul)
        if (theme.startsWith("guinda")) {
            context.setTheme(R.style.Theme_App_Guinda)
        } else {
            context.setTheme(R.style.Theme_App_Azul)
        }

        // 2. Set the light/dark mode override
        when {
            theme.endsWith("light") -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            theme.endsWith("dark") -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
