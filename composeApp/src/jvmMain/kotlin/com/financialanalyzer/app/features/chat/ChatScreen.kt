package com.financialanalyzer.app.features.chat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import com.financialanalyzer.app.shared.theme.AppColors
import com.financialanalyzer.app.shared.python.PythonBridge
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val tableData: String? = null, // Para exibir tabelas em markdown
    val hasTable: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Conectando...") }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val pythonBridge = remember { PythonBridge() }
    
    // Verificar setup do Python na inicialização
    LaunchedEffect(Unit) {
        val setupStatus = pythonBridge.checkPythonSetup()
        connectionStatus = if (setupStatus.available) {
            "✅ Conectado ao Claude AI"
        } else {
            "❌ ${setupStatus.message}"
        }
    }

    // Auto-scroll para a última mensagem
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header do Chat
        Text(
            text = "Financial AI Assistant",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = connectionStatus,
            style = MaterialTheme.typography.bodySmall,
            color = if (connectionStatus.startsWith("✅")) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Ask me to analyze your data, create charts, or explain financial concepts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Lista de mensagens
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    EmptyStateMessage()
                }
            } else {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
            }
            
            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Input area
        ChatInputArea(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSendMessage = {
                if (inputText.isNotBlank() && !isLoading) {
                    // Adicionar mensagem do usuário
                    val userMessage = ChatMessage(
                        id = "user_${System.currentTimeMillis()}",
                        content = inputText.trim(),
                        isFromUser = true
                    )
                    messages = messages + userMessage
                    
                    // Processar com agente Claude real
                    isLoading = true
                    val messageToSend = inputText.trim()
                    inputText = ""
                    
                    coroutineScope.launch {
                        try {
                            // Chamar Python Bridge
                            val response = pythonBridge.processChatMessage(messageToSend)
                            
                            val aiResponse = if (response.status == "success") {
                                ChatMessage(
                                    id = "ai_${System.currentTimeMillis()}",
                                    content = response.message,
                                    isFromUser = false,
                                    tableData = response.tableData,
                                    hasTable = !response.tableData.isNullOrBlank()
                                )
                            } else {
                                ChatMessage(
                                    id = "ai_${System.currentTimeMillis()}",
                                    content = "❌ Erro: ${response.message}\n\n${response.error ?: ""}",
                                    isFromUser = false
                                )
                            }
                            
                            messages = messages + aiResponse
                        } catch (e: Exception) {
                            val errorMessage = ChatMessage(
                                id = "ai_${System.currentTimeMillis()}",
                                content = "❌ Erro de conexão: ${e.message}\n\nVerifique se o Python está configurado e a API key do Claude está definida no arquivo .env",
                                isFromUser = false
                            )
                            messages = messages + errorMessage
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            isLoading = isLoading
        )
    }
}

@Composable
private fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👋",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Start a conversation",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Upload a CSV or PDF file and ask me to analyze it, or just ask any financial question!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // Avatar da IA
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖",
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // Bolha da mensagem
        Card(
            modifier = Modifier.widthIn(max = if (message.hasTable) 600.dp else 320.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Texto da mensagem
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // Tabela em markdown (se houver)
                if (message.hasTable && !message.tableData.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = message.tableData,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
        
        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar do usuário
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar da IA
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🤖",
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Indicador de typing
        Card(
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp, 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 400) % 3 == index.toLong()) 1f else 0.3f,
                        animationSpec = tween(400),
                        label = "typing_dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                                RoundedCornerShape(3.dp)
                            )
                    )
                    if (index < 2) Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                placeholder = {
                    Text(
                        text = "Ask about your data or financial questions...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSendMessage() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                maxLines = 4
            )
            
            IconButton(
                onClick = onSendMessage,
                enabled = inputText.isNotBlank() && !isLoading,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send message",
                    tint = if (inputText.isNotBlank() && !isLoading) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}