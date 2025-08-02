package com.financialanalyzer.app.shared.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Ultra Minimalist Dark Theme
    val DarkBackground = Color(0xFF171717)        // Main content (mais claro que drawer)
    val DarkSurface = Color(0xFF0D0D0D)          // Navigation drawer (bem próximo do preto)
    val DarkSurfaceVariant = Color(0xFF1A1A1A)   // Variantes do drawer e cards
    val DarkOnSurface = Color(0xFFE8E8E8)        // Texto no drawer
    val DarkOnSurfaceVariant = Color(0xFFAAAAAA)  // Texto secundário no drawer
    val DarkOnBackground = Color(0xFFE8E8E8)     // Texto no main content
    val DarkBorder = Color(0xFF2A2A2A)           // Bordas
    
    // Ultra Minimalist Light Theme
    val LightBackground = Color(0xFFFAFAFA)      // Main content (off-white, menos agressivo)
    val LightSurface = Color(0xFFF8F8F8)         // Navigation drawer (mais escuro que main)
    val LightSurfaceVariant = Color(0xFFF0F0F0)  // Variantes do drawer e cards
    val LightOnSurface = Color(0xFF121212)       // Texto no drawer
    val LightOnSurfaceVariant = Color(0xFF6F6F6F) // Texto secundário no drawer
    val LightOnBackground = Color(0xFF121212)    // Texto no main content
    val LightBorder = Color(0xFFE5E5E5)          // Bordas
    
    // Neutral Primary - Sem cores vibrantes, apenas contraste
    val PrimaryDark = Color(0xFFE8E8E8)
    val PrimaryLight = Color(0xFF121212)
    
    // Semantic - Discreto e mínimo
    val Error = Color(0xFFD32F2F)
    val Success = Color(0xFF2E7D32)
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}