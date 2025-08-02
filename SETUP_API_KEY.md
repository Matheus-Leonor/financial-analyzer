# 🔑 Configuração da API Key do Claude

Para usar o Financial Analyzer com Claude AI, você precisa configurar sua API key.

## 1. Obter API Key do Claude

1. Acesse [console.anthropic.com](https://console.anthropic.com/)
2. Faça login ou crie uma conta
3. Vá em **API Keys** no menu lateral
4. Clique em **Create Key**
5. Copie a chave (formato: `sk-ant-api03-...`)

## 2. Configurar no Projeto

### Opção A: Arquivo .env (Recomendado)

1. Vá para a pasta `python-engine/`
2. Copie o arquivo `.env.example` para `.env`:
   ```bash
   cd python-engine
   cp .env.example .env
   ```

3. Abra o arquivo `.env` e substitua sua API key:
   ```
   ANTHROPIC_API_KEY=sk-ant-api03-SUA_CHAVE_AQUI
   ```

### Opção B: Variável de Ambiente do Sistema

**Windows:**
```cmd
setx ANTHROPIC_API_KEY "sk-ant-api03-SUA_CHAVE_AQUI"
```

**Mac/Linux:**
```bash
export ANTHROPIC_API_KEY="sk-ant-api03-SUA_CHAVE_AQUI"
```

## 3. Verificar Configuração

Execute o teste Python para verificar se está funcionando:

```bash
cd python-engine
python test_install.py
```

Deve aparecer: ✅ Environment OK

## 4. Executar o App

Agora você pode executar o Financial Analyzer:

```bash
./gradlew composeApp:run
```

O chat deve mostrar "✅ Conectado ao Claude AI" no header.

## 🚨 Importante

- **NUNCA** commite o arquivo `.env` no Git
- O `.env` já está no `.gitignore`
- Mantenha sua API key segura e privada

## 💰 Custos

O Claude AI é pago por uso. Custos aproximados:
- Mensagem simples: ~$0.001-0.003
- Análise de dados: ~$0.01-0.05
- Upload de arquivo: ~$0.02-0.10

O projeto inclui rate limiting para evitar gastos excessivos.