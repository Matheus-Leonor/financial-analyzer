package com.financialanalyzer.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.financialanalyzer.app.features.dashboard.DashboardScreen
import com.financialanalyzer.app.features.chat.ChatScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.financialanalyzer.app.shared.enums.NavigationItem
import com.financialanalyzer.app.shared.navigation.NavigationDrawer
import com.financialanalyzer.app.shared.theme.FinancialAnalyzerTheme
import com.financialanalyzer.app.shared.theme.ThemeMode
import com.financialanalyzer.app.shared.theme.ThemeToggleButton

@Composable
@Preview
fun App() {
    var currentTheme by remember { mutableStateOf(ThemeMode.DARK) }
    
    FinancialAnalyzerTheme(themeMode = currentTheme) {
        var selectedItem by remember { mutableStateOf(NavigationItem.DASHBOARD) }

        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation Drawer
            NavigationDrawer(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )

            // Main Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(end = 72.dp) // Espaçamento igual largura do drawer colapsado
            ) {
                // Theme Toggle (top-right, maior)
                ThemeToggleButton(
                    currentTheme = currentTheme,
                    onThemeChanged = { currentTheme = it },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp) // Aumentado de 40dp para 48dp
                )
                
                // Content
                Box(modifier = Modifier.padding(16.dp)) {
                    when (selectedItem) {
                        NavigationItem.DASHBOARD -> DashboardScreen()
                        NavigationItem.DOCUMENTS -> Text("Documents Content")
                        NavigationItem.CHARTS -> Text("Charts Content")
                        NavigationItem.CHAT -> ChatScreen()
                        NavigationItem.SETTINGS -> Text("Settings Content")
                    }
                }
            }
        }
    }
}
