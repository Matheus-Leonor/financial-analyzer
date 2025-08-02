package com.financialanalyzer.app.shared.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "dashboard"),
    DOCUMENTS("Documents", Icons.Default.Folder, "documents"),
    CHARTS("Charts", Icons.Default.BarChart, "charts"),
    CHAT("Chat AI", Icons.Default.Chat, "chat"),
    SETTINGS("Settings", Icons.Default.Settings, "settings")
}