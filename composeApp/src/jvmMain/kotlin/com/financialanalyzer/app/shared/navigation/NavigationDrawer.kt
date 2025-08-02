package com.financialanalyzer.app.shared.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financialanalyzer.app.shared.enums.NavigationItem
import com.financialanalyzer.app.shared.theme.AppColors

@Composable
fun NavigationDrawer(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    val drawerWidth by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 72.dp,
        animationSpec = tween(400), // Aumentado para transição mais suave
        label = "drawer_width"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(AppColors.DarkSurface) // Sempre preto, independente do tema
            .padding(16.dp)
    ) {
        // Header com toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = if (isExpanded) Alignment.CenterStart else Alignment.Center
        ) {
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle menu",
                    tint = AppColors.DarkOnSurfaceVariant, // Sempre cor escura
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Items de navegação
        NavigationItem.values().forEach { item ->
            NavigationDrawerItem(
                item = item,
                isSelected = selectedItem == item,
                isExpanded = isExpanded,
                onClick = { onItemSelected(item) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerItem(
    item: NavigationItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        AppColors.DarkSurfaceVariant // Sempre cor escura para seleção
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        AppColors.DarkOnSurface // Sempre cor escura para texto
    } else {
        AppColors.DarkOnSurfaceVariant // Sempre cor escura para texto secundário
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(
                horizontal = if (isExpanded) 12.dp else 0.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
    ) {
        // Ícone sempre presente
        val iconContent = @Composable {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier.size(24.dp) // Aumentado de 20dp para 24dp
            )
        }
        
        // Ícone sempre presente
        if (isExpanded) {
            iconContent()
        } else {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                state = rememberTooltipState()
            ) {
                iconContent()
            }
        }
        
        // Texto com animação suave
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Row {
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = item.title,
                    color = contentColor,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}