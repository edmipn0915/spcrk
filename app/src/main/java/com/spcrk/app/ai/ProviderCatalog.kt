package com.spcrk.app.ai

/**
 * AI 供應商權威目錄（60 家）。
 * 統一名稱與 baseUrl，避免多份清單數量不一致的問題。
 */
data class ProviderInfo(
    val id: String,
    val displayName: String,
    val baseUrl: String
)

object ProviderCatalog {

    private val providers: List<ProviderInfo> = listOf(
        // —— 標準 OpenAI 兼容大廠 ——
        ProviderInfo("openai", "OpenAI", "https://api.openai.com/v1"),
        ProviderInfo("anthropic", "Anthropic", "https://api.anthropic.com/v1"),
        ProviderInfo("gemini", "Google Gemini", "https://generativelanguage.googleapis.com/v1beta"),
        ProviderInfo("deepseek", "DeepSeek", "https://api.deepseek.com/v1"),
        ProviderInfo("grok", "xAI Grok", "https://api.x.ai/v1"),
        ProviderInfo("mistral", "Mistral", "https://api.mistral.ai/v1"),
        ProviderInfo("cerebras", "Cerebras", "https://api.cerebras.ai/v1"),
        ProviderInfo("mimo", "Xiaomi MiMo", "https://api.xiaomi.com/v1"),
        ProviderInfo("zhipu", "智谱 GLM", "https://open.bigmodel.cn/api/paas/v4"),
        ProviderInfo("moonshot", "Moonshot", "https://api.moonshot.cn/v1"),
        ProviderInfo("baichuan", "百川 AI", "https://api.baichuan-ai.com/v1"),
        ProviderInfo("dashscope", "通义千问", "https://dashscope.aliyuncs.com/compatible-mode/v1"),
        ProviderInfo("stepfun", "阶跃星辰", "https://api.stepfun.com/v1"),
        ProviderInfo("doubao", "豆包", "https://ark.cn-beijing.volces.com/api/v3"),
        ProviderInfo("minimax", "MiniMax", "https://api.minimax.chat/v1"),
        ProviderInfo("perplexity", "Perplexity", "https://api.perplexity.ai"),
        ProviderInfo("nvidia", "NVIDIA", "https://integrate.api.nvidia.com/v1"),
        ProviderInfo("groq", "Groq", "https://api.groq.com/openai/v1"),
        ProviderInfo("together", "Together", "https://api.together.xyz/v1"),
        ProviderInfo("fireworks", "Fireworks", "https://api.fireworks.ai/inference/v1"),
        ProviderInfo("huggingface", "Hugging Face", "https://api-inference.huggingface.co/v1"),
        ProviderInfo("jina", "Jina", "https://api.jina.ai/v1"),
        ProviderInfo("voyageai", "VoyageAI", "https://api.voyageai.com/v1"),
        ProviderInfo("azure-openai", "Azure OpenAI", "https://openai.azure.com/openai/deployments"),
        ProviderInfo("vertexai", "VertexAI", "https://us-central1-aiplatform.googleapis.com/v1"),
        ProviderInfo("aws-bedrock", "AWS Bedrock", "https://bedrock-runtime.us-east-1.amazonaws.com"),
        ProviderInfo("github", "GitHub Models", "https://models.github.ai/inference"),
        ProviderInfo("copilot", "GitHub Copilot", "https://api.githubcopilot.com"),
        // —— 本地推理 ——
        ProviderInfo("ollama", "Ollama", "http://localhost:11434/v1"),
        ProviderInfo("lmstudio", "LM Studio", "http://localhost:1234/v1"),
        // —— 國內/聚合/第三方 ——
        ProviderInfo("modelscope", "ModelScope", ""),
        ProviderInfo("xirang", "息壤", ""),
        ProviderInfo("ovms", "OpenVINO", ""),
        ProviderInfo("gpustack", "GPUStack", ""),
        ProviderInfo("cherryin", "CherryIN", ""),
        ProviderInfo("silicon", "Silicon", ""),
        ProviderInfo("aihubmix", "AiHubMix", ""),
        ProviderInfo("openrouter", "OpenRouter", ""),
        ProviderInfo("new-api", "New API", ""),
        ProviderInfo("dmxapi", "DMXAPI", ""),
        ProviderInfo("ocoolai", "ocoolAI", ""),
        ProviderInfo("302ai", "302.AI", ""),
        ProviderInfo("aionly", "AIOnly", ""),
        ProviderInfo("burncloud", "BurnCloud", ""),
        ProviderInfo("lanyun", "蓝云", ""),
        ProviderInfo("ph8", "PH8", ""),
        ProviderInfo("sophnet", "SophNet", ""),
        ProviderInfo("ppio", "PPIO", ""),
        ProviderInfo("qiniu", "七牛", ""),
        ProviderInfo("alayanew", "AlayaNew", ""),
        ProviderInfo("tokenhub", "TokenHub", ""),
        ProviderInfo("baidu-cloud", "百度千帆", ""),
        ProviderInfo("radeon-cloud", "AMD GPU Cloud", ""),
        ProviderInfo("opencode", "OpenCode", ""),
        ProviderInfo("grok-cli", "Grok CLI", ""),
        ProviderInfo("longcat", "LongCat", ""),
        ProviderInfo("gateway", "Vercel AI Gateway", ""),
        ProviderInfo("poe", "Poe", ""),
        ProviderInfo("agnes-ai", "Agnes AI", ""),
        ProviderInfo("custom", "自定义", "")
    )

    val all: List<ProviderInfo> get() = providers

    fun displayName(id: String): String =
        providers.find { it.id == id }?.displayName ?: id

    fun baseUrl(id: String): String =
        providers.find { it.id == id }?.baseUrl ?: ""
}
