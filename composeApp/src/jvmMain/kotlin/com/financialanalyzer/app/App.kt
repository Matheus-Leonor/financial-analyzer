package com.financialanalyzer.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.financialanalyzer.app.features.dashboard.DashboardScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.financialanalyzer.app.shared.enums.NavigationItem
import com.financialanalyzer.app.shared.navigation.NavigationDrawer

@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedItem by remember { mutableStateOf(NavigationItem.DASHBOARD) }

        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation Drawer
            NavigationDrawer(
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )

            // Main Content
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                when (selectedItem) {
                    NavigationItem.DASHBOARD -> DashboardScreen()
                    NavigationItem.DOCUMENTS -> Text("Documents Content")
                    NavigationItem.CHARTS -> Text("Charts Content")
                    NavigationItem.CHAT -> Text("Chat Content")
                    NavigationItem.SETTINGS -> Text("Settings Content")
                }
            }
        }
    }
}
