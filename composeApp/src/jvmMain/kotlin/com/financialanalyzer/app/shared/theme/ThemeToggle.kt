package com.financialanalyzer.app.shared.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun ThemeToggleButton(
    currentTheme: ThemeMode,
    onThemeChanged: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Botão circular sem fundo (apenas no hover)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (isHovered) MaterialTheme.colorScheme.surfaceVariant // Cor dos cards para consistência
                else Color.Transparent
            )
            .padding(8.dp) // Aumentando a área de hover
            .hoverable(interactionSource)
            .clickable { showDropdown = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (currentTheme) {
                ThemeMode.LIGHT -> Icons.Default.LightMode
                ThemeMode.DARK -> Icons.Default.DarkMode
                ThemeMode.SYSTEM -> Icons.Default.Settings
            },
            contentDescription = "Theme",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
    
    // Dropdown posicionado abaixo do ícone
    if (showDropdown) {
        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    // Posicionar o popup à direita e abaixo do ícone
                    // anchorBounds é a posição do ícone
                    val x = anchorBounds.right - popupContentSize.width // Alinhado à direita do ícone
                    val y = anchorBounds.top + 72 // 8px abaixo do ícone
                    return IntOffset(x, y)
                }
            },
            onDismissRequest = { showDropdown = false },
            properties = PopupProperties(focusable = true)
        ) {
            Card(
                modifier = Modifier.width(180.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ThemeOption(
                        icon = Icons.Default.LightMode,
                        title = "Light",
                        isSelected = currentTheme == ThemeMode.LIGHT,
                        onClick = {
                            onThemeChanged(ThemeMode.LIGHT)
                            showDropdown = false
                        }
                    )
                    
                    ThemeOption(
                        icon = Icons.Default.DarkMode,
                        title = "Dark", 
                        isSelected = currentTheme == ThemeMode.DARK,
                        onClick = {
                            onThemeChanged(ThemeMode.DARK)
                            showDropdown = false
                        }
                    )
                    
                    ThemeOption(
                        icon = Icons.Default.Settings,
                        title = "System",
                        isSelected = currentTheme == ThemeMode.SYSTEM,
                        onClick = {
                            onThemeChanged(ThemeMode.SYSTEM)
                            showDropdown = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}