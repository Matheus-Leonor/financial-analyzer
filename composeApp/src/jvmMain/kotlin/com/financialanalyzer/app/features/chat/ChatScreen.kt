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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            // Header minimalista
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Claude",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = connectionStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connectionStatus.startsWith("✅")) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            // Chat área - estilo Claude/ChatGPT
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        ClaudeStyleEmptyState()
                    }
                } else {
                    items(messages) { message ->
                        ClaudeStyleMessage(message = message)
                    }
                }
                
                if (isLoading) {
                    item {
                        ClaudeStyleTypingIndicator()
                    }
                }
            }
        }

        // Input area estilo Claude (fixo na parte inferior)
        ClaudeStyleInputArea(
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
                                    content = "Erro: ${response.message}\n\n${response.error ?: ""}",
                                    isFromUser = false
                                )
                            }
                            
                            messages = messages + aiResponse
                        } catch (e: Exception) {
                            val errorMessage = ChatMessage(
                                id = "ai_${System.currentTimeMillis()}",
                                content = "Erro de conexão: ${e.message}\n\nVerifique se o Python está configurado e a API key do Claude está definida no arquivo .env",
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
private fun ClaudeStyleEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How can I help you today?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "I can analyze financial data, create visualizations, and answer questions about economics and finance.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun ClaudeStyleMessage(message: ChatMessage) {
    SelectionContainer {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Label do remetente (You/Claude)
            Text(
                text = if (message.isFromUser) "You" else "Claude",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Conteúdo da mensagem - texto selecionável
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 24.sp,
                modifier = Modifier.padding(bottom = if (message.hasTable) 16.dp else 0.dp)
            )
            
            // Tabela em markdown (se houver)
            if (message.hasTable && !message.tableData.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = message.tableData,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClaudeStyleTypingIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label Claude
        Text(
            text = "Claude",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Indicador minimalista
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val alpha by animateFloatAsState(
                    targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1f else 0.3f,
                    animationSpec = tween(500),
                    label = "typing_dot_$index"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                            RoundedCornerShape(4.dp)
                        )
                )
                if (index < 2) Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun ClaudeStyleInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Message Claude...",
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
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6,
                    minLines = 1
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                IconButton(
                    onClick = onSendMessage,
                    enabled = inputText.isNotBlank() && !isLoading,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (inputText.isNotBlank() && !isLoading) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = if (inputText.isNotBlank() && !isLoading) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}