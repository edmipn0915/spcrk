package com.spcrk.app.ai

data class ModelConfig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val provider: String = "custom",
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com/v1",
    val modelName: String = "gpt-4o-mini",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val timeout: Int = 60,
    val createdAt: Long = System.currentTimeMillis()
)

object PresetModels {
    /**
     * 基于内置模型清单自动生成所有厂商的默认配置。
     * 默认全部关闭，用户需要时再到设置中手动打开对应厂商。
     */
    val presets: Map<String, ModelConfig> by lazy {
        presetModelsByProvider.keys.associateWith { provider ->
            ModelConfig(
                name = providerDisplayName(provider),
                provider = provider,
                baseUrl = providerBaseUrls[provider] ?: "",
                modelName = presetModelsByProvider[provider]?.firstOrNull() ?: "",
                isEnabled = false
            )
        }
    }
}

/**
 * 各厂商预置的常用模型清单（chatbox 式），用于"添加预置模型"。
 * 来源: Cherry Studio 内置厂商列表（62家厂商，300+模型）。
 */
val presetModelsByProvider: Map<String, List<String>> = mapOf(
    // ═══════════════════════════════════════════════════════════════════════════
    // 一、直连厂商 (Direct) - 模型原厂
    // ═══════════════════════════════════════════════════════════════════════════
    "openai" to listOf(
        "gpt-5.6-sol", "gpt-5.6-sol-pro", "gpt-5.6-terra", "gpt-5.6-luna",
        "gpt-5.5", "gpt-5.4", "gpt-5.4-mini", "gpt-4.1", "gpt-4.1-mini",
        "o3", "o4-mini", "gpt-4o", "gpt-4o-mini",
        "text-embedding-3-small", "text-embedding-3-large",
        "gpt-image-2", "dall-e-3", "gpt-realtime-1.5", "tts-1", "whisper-1"
    ),
    "anthropic" to listOf(
        "claude-opus-4-8", "claude-opus-4-7", "claude-opus-4-6",
        "claude-sonnet-5", "claude-sonnet-4-6", "claude-sonnet-4-5",
        "claude-haiku-4-5", "claude-opus-4-5", "claude-opus-4-1"
    ),
    "gemini" to listOf(
        "gemini-3.6-flash", "gemini-3.5-flash", "gemini-3.5-flash-lite",
        "gemini-3.1-pro", "gemini-3.1-flash", "gemini-3.1-flash-lite",
        "gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.5-flash-lite"
    ),
    "deepseek" to listOf(
        "deepseek-chat", "deepseek-reasoner", "deepseek-v3", "deepseek-v3.2"
    ),
    "grok" to listOf("grok-4", "grok-3", "grok-2"),
    "mistral" to listOf(
        "mistral-large", "mistral-small", "codestral",
        "ministral-8b", "ministral-3b"
    ),
    "cerebras" to listOf("llama-3.1-70b", "llama-3.1-8b", "llama-3.3-70b"),
    "mimo" to listOf("mimo-v1", "mimo-v1.5"),
    "zhipu" to listOf("glm-5", "glm-4-plus", "glm-4", "glm-4-air", "glm-4-flash"),
    "moonshot" to listOf("kimi-k2", "kimi-k1.5", "kimi-latest"),
    "baichuan" to listOf("baichuan4", "baichuan3", "baichuan2"),
    "dashscope" to listOf(
        "qwen3", "qwen2.5", "qwen2", "qwen-long", "qwen-turbo", "qwen-plus", "qwen-max"
    ),
    "stepfun" to listOf("step-1", "step-1.5", "step-2"),
    "doubao" to listOf("doubao-seed-1.8", "doubao-seed-1.6", "doubao-seed-1.6-vision"),
    "minimax" to listOf("minimax-text-01", "minimax-vl-01", "abab-7", "abab-6.5"),
    "perplexity" to listOf(
        "sonar-pro", "sonar", "sonar-reasoning", "sonar-reasoning-pro",
        "llama-3.1-sonar-small", "r1-1776"
    ),
    "nvidia" to listOf(
        "llama-3.1-nemotron-70b-instruct", "llama-3.1-nemotron-51b-instruct",
        "llama-3.1-nemotron-8b-instruct", "nemotron-mini-4b-instruct"
    ),

    // ═══════════════════════════════════════════════════════════════════════════
    // 一、直连厂商 (Direct) - 推理平台
    // ═══════════════════════════════════════════════════════════════════════════
    "groq" to listOf(
        "llama-3.3-70b-versatile", "llama-3.1-8b-instant",
        "llama-3-70b-8192", "llama-3-8b-8192",
        "mixtral-8x7b-32768", "gemma-2-9b-it"
    ),
    "together" to listOf(
        "meta-llama/Llama-3.3-70B-Instruct-Turbo",
        "meta-llama/Llama-3.1-8B-Instruct-Turbo",
        "mistralai/Mixtral-8x7B-Instruct-v0.1",
        "Qwen/Qwen2.5-7B-Instruct-Turbo"
    ),
    "fireworks" to listOf(
        "accounts/fireworks/models/llama-v3p3-70b-instruct",
        "accounts/fireworks/models/llama-v3p1-8b-instruct",
        "accounts/fireworks/models/mixtral-8x7b-instruct"
    ),
    "huggingface" to listOf(
        "meta-llama/Llama-3.3-70B-Instruct",
        "meta-llama/Llama-3.1-8B-Instruct",
        "mistralai/Mistral-7B-Instruct-v0.3",
        "Qwen/Qwen2.5-72B-Instruct"
    ),
    "jina" to listOf("jina-embeddings-v3", "jina-embeddings-v2", "jina-reranker-v2"),
    "voyageai" to listOf("voyage-3", "voyage-3-lite", "voyage-2", "voyage-code-2"),

    // ═══════════════════════════════════════════════════════════════════════════
    // 二、直连厂商 (Direct) - 云平台
    // ═══════════════════════════════════════════════════════════════════════════
    "azure-openai" to listOf(
        "gpt-4o", "gpt-4o-mini", "gpt-4", "gpt-3.5-turbo",
        "text-embedding-3-small", "text-embedding-3-large"
    ),
    "vertexai" to listOf(
        "gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash",
        "claude-3-5-sonnet", "claude-3-5-haiku", "claude-3-opus", "llama-3.1-405b"
    ),
    "aws-bedrock" to listOf(
        "anthropic.claude-3-5-sonnet", "anthropic.claude-3-5-haiku",
        "anthropic.claude-3-opus", "meta.llama3-1-70b-instruct",
        "meta.llama3-1-8b-instruct", "mistral.mistral-large",
        "mistral.mixtral-8x7b", "cohere.command-r-plus", "amazon.titan-text-premier"
    ),
    "github" to listOf(
        "gpt-4o", "gpt-4o-mini", "o1", "o3-mini",
        "claude-3-5-sonnet", "claude-3-5-haiku",
        "llama-3.1-405b", "llama-3.1-70b", "mistral-large", "cohere-command-r"
    ),
    "copilot" to listOf("gpt-4o", "gpt-4o-mini", "claude-3-5-sonnet", "llama-3.1-405b"),
    "modelscope" to listOf(
        "Qwen/Qwen2.5-72B-Instruct", "Qwen/Qwen2.5-32B-Instruct",
        "Qwen/Qwen2.5-14B-Instruct", "Qwen/Qwen2.5-7B-Instruct",
        "internlm/internlm2_5-20b-chat"
    ),
    "xirang" to listOf("xirang-default"),

    // ═══════════════════════════════════════════════════════════════════════════
    // 三、本地调用 (Local)
    // ═══════════════════════════════════════════════════════════════════════════
    "lmstudio" to listOf("local-model"),
    "ovms" to listOf("local-model"),
    "gpustack" to listOf("local-model"),

    // ═══════════════════════════════════════════════════════════════════════════
    // 四、中转/网关 (Relay/Proxy) - 模型列表动态变化，列常见示例
    // ═══════════════════════════════════════════════════════════════════════════
    "cherryin" to listOf("gpt-4o", "claude-3-5-sonnet", "deepseek-chat", "qwen-max"),
    "silicon" to listOf("Qwen/Qwen2.5-7B-Instruct", "meta-llama/Llama-3.1-8B-Instruct", "deepseek-ai/DeepSeek-V3"),
    "aihubmix" to listOf("gpt-4o", "claude-3-5-sonnet", "gemini-2.5-flash", "llama-3.1-405b"),
    "openrouter" to listOf("anthropic/claude-3.5-sonnet", "openai/gpt-4o", "google/gemini-2.5-flash", "meta-llama/llama-3.1-405b"),
    "new-api" to listOf("gpt-4o", "claude-3-5-sonnet"),
    "dmxapi" to listOf("gpt-4o", "claude-3-5-sonnet", "deepseek-chat"),
    "ocoolai" to listOf("gpt-4o", "claude-3-5-sonnet", "qwen-max"),
    "302ai" to listOf("gpt-4o", "claude-3-5-sonnet", "gemini-2.5-flash", "qwen-max"),
    "aionly" to listOf("gpt-4o", "claude-3-5-sonnet", "deepseek-chat"),
    "burncloud" to listOf("gpt-4o", "claude-3-5-sonnet", "qwen-max"),
    "lanyun" to listOf("gpt-4o", "claude-3-5-sonnet", "qwen-max"),
    "ph8" to listOf("gpt-4o", "claude-3-5-sonnet", "deepseek-chat"),
    "sophnet" to listOf("gpt-4o", "claude-3-5-sonnet", "qwen-max"),
    "ppio" to listOf("gpt-4o", "claude-3-5-sonnet", "llama-3.1-70b"),
    "qiniu" to listOf("gpt-4o", "claude-3-5-sonnet", "deepseek-chat"),
    "alayanew" to listOf("deepseek-chat", "deepseek-reasoner"),
    "tokenhub" to listOf("gpt-4o", "claude-3-5-sonnet", "qwen-max"),
    "baidu-cloud" to listOf("ernie-4.0", "deepseek-chat", "qwen-max"),
    "radeon-cloud" to listOf("llama-3.1-70b", "llama-3.1-8b"),
    "opencode" to listOf("gpt-4o", "claude-3-5-sonnet", "llama-3.1-405b"),
    "grok-cli" to listOf("grok-3", "grok-2"),
    "longcat" to listOf("gpt-4o", "claude-3-5-sonnet", "deepseek-chat"),
    "gateway" to listOf("gpt-4o", "claude-3-5-sonnet", "llama-3.1-405b"),
    "poe" to listOf("gpt-4o", "claude-3.5-sonnet", "llama-3.1-405b", "gemini-2.5-flash"),

    // ═══════════════════════════════════════════════════════════════════════════
    // 五、推荐厂商 (Recommended)
    // ═══════════════════════════════════════════════════════════════════════════
    "agnes-ai" to listOf("agnes-2.5-flash", "agnes-2.0-flash")
)

/**
 * 厂商类型 → 用户可读显示名。
 * 来源: Cherry Studio 内置厂商列表（62家厂商）。
 */
fun providerDisplayName(provider: String): String = when (provider) {
    // 直连 - 模型原厂
    "openai" -> "OpenAI"
    "anthropic" -> "Anthropic"
    "gemini" -> "Google Gemini"
    "deepseek" -> "DeepSeek"
    "grok" -> "xAI Grok"
    "mistral" -> "Mistral"
    "cerebras" -> "Cerebras"
    "mimo" -> "Xiaomi MiMo"
    "zhipu" -> "智谱 GLM"
    "moonshot" -> "Moonshot"
    "baichuan" -> "百川 AI"
    "dashscope" -> "通义千问"
    "stepfun" -> "阶跃星辰"
    "doubao" -> "豆包"
    "minimax" -> "MiniMax"
    "perplexity" -> "Perplexity"
    "nvidia" -> "NVIDIA"
    // 直连 - 推理平台
    "groq" -> "Groq"
    "together" -> "Together"
    "fireworks" -> "Fireworks"
    "huggingface" -> "Hugging Face"
    "jina" -> "Jina"
    "voyageai" -> "VoyageAI"
    // 直连 - 云平台
    "azure-openai" -> "Azure OpenAI"
    "vertexai" -> "VertexAI"
    "aws-bedrock" -> "AWS Bedrock"
    "github" -> "GitHub Models"
    "copilot" -> "GitHub Copilot"
    "modelscope" -> "ModelScope"
    "xirang" -> "息壤"
    // 本地调用
    "ollama" -> "Ollama"
    "lmstudio" -> "LM Studio"
    "ovms" -> "OpenVINO"
    "gpustack" -> "GPUStack"
    // 中转/网关
    "cherryin" -> "CherryIN"
    "silicon" -> "Silicon"
    "aihubmix" -> "AiHubMix"
    "openrouter" -> "OpenRouter"
    "new-api" -> "New API"
    "dmxapi" -> "DMXAPI"
    "ocoolai" -> "ocoolAI"
    "302ai" -> "302.AI"
    "aionly" -> "AIOnly"
    "burncloud" -> "BurnCloud"
    "lanyun" -> "蓝云"
    "ph8" -> "PH8"
    "sophnet" -> "SophNet"
    "ppio" -> "PPIO"
    "qiniu" -> "七牛"
    "alayanew" -> "AlayaNew"
    "tokenhub" -> "TokenHub"
    "baidu-cloud" -> "百度千帆"
    "radeon-cloud" -> "AMD GPU Cloud"
    "opencode" -> "OpenCode"
    "grok-cli" -> "Grok CLI"
    "longcat" -> "LongCat"
    "gateway" -> "Vercel AI Gateway"
    "poe" -> "Poe"
    "agnes-ai" -> "Agnes AI"
    "custom" -> "自定义"
    else -> provider.replaceFirstChar { it.uppercase() }
}

/**
 * 厂商类型 → 默认 Base URL。
 * 用于首次初始化厂商卡片和添加自定义模型时自动填充。
 */
val providerBaseUrls: Map<String, String> = mapOf(
    // 直连 - 模型原厂
    "openai" to "https://api.openai.com/v1",
    "anthropic" to "https://api.anthropic.com/v1",
    "gemini" to "https://generativelanguage.googleapis.com/v1beta",
    "deepseek" to "https://api.deepseek.com/v1",
    "grok" to "https://api.x.ai/v1",
    "mistral" to "https://api.mistral.ai/v1",
    "cerebras" to "https://api.cerebras.ai/v1",
    "mimo" to "https://api.xiaomimimo.com/v1",
    "zhipu" to "https://open.bigmodel.cn/api/paas/v4",
    "moonshot" to "https://api.moonshot.cn/v1",
    "baichuan" to "https://api.baichuan-ai.com/v1",
    "dashscope" to "https://dashscope.aliyuncs.com/compatible-mode/v1",
    "stepfun" to "https://api.stepfun.com/v1",
    "doubao" to "https://ark.cn-beijing.volces.com/api/v3",
    "minimax" to "https://api.minimaxi.com/v1/",
    "perplexity" to "https://api.perplexity.ai/",
    "nvidia" to "https://integrate.api.nvidia.com/v1",
    // 直连 - 推理平台
    "groq" to "https://api.groq.com/openai/v1",
    "together" to "https://api.together.ai/v1",
    "fireworks" to "https://api.fireworks.ai/inference/v1",
    "huggingface" to "https://router.huggingface.co/v1/",
    "jina" to "https://api.jina.ai/v1",
    "voyageai" to "https://api.voyageai.com/v1",
    // 直连 - 云平台
    "azure-openai" to "https://resource.openai.azure.com/v1",
    "vertexai" to "https://region-aiplatform.googleapis.com/v1",
    "aws-bedrock" to "https://bedrock-runtime.region.amazonaws.com/v1",
    "github" to "https://models.github.ai/inference/v1",
    "copilot" to "https://api.githubcopilot.com/v1",
    "modelscope" to "https://api-inference.modelscope.cn/v1/",
    "xirang" to "https://wishub-x1.ctyun.cn/v1",
    // 本地调用
    "ollama" to "http://localhost:11434/v1",
    "lmstudio" to "http://localhost:1234/v1",
    "ovms" to "http://localhost:8000/v3/",
    "gpustack" to "http://localhost:8000/v1",
    // 中转/网关
    "cherryin" to "https://open.cherryin.net/v1",
    "silicon" to "https://api.siliconflow.cn/v1",
    "aihubmix" to "https://aihubmix.com/v1",
    "openrouter" to "https://openrouter.ai/api/v1/",
    "new-api" to "https://api.newapi.pro/v1",
    "dmxapi" to "https://dmxapi.cn/v1",
    "ocoolai" to "https://api.ocoolai.com/v1",
    "302ai" to "https://api.302.ai/v1",
    "aionly" to "https://api.aionly.com/v1",
    "burncloud" to "https://ai.burncloud.com/v1",
    "lanyun" to "https://maas-api.lanyun.net/v1",
    "ph8" to "https://ph8.co/v1",
    "sophnet" to "https://www.sophnet.com/api/open-apis/v1",
    "ppio" to "https://api.ppinfra.com/v3/openai/",
    "qiniu" to "https://api.qnaigc.com/v1",
    "alayanew" to "https://deepseek.alayanew.com/v1",
    "tokenhub" to "https://tokenhub.tencentmaas.com/v1",
    "baidu-cloud" to "https://qianfan.baidubce.com/v2/",
    "radeon-cloud" to "https://developer.amd.com.cn/radeon/v1",
    "opencode" to "https://opencode.ai/zen/go/v1",
    "grok-cli" to "https://cli-chat-proxy.grok.com/v1",
    "longcat" to "https://api.longcat.chat/openai/v1",
    "gateway" to "https://ai-gateway.vercel.sh/v1/ai",
    "poe" to "https://api.poe.com/v1/",
    "agnes-ai" to "https://apihub.agnes-ai.com/v1"
)

val providerTags: Map<String, List<String>> = mapOf(
    "agnes-ai" to listOf("recommended", "free")
)

val providerNotes: Map<String, String> = mapOf(
    "agnes-ai" to "需要注册 Agnes AI 账号才能使用，目前免费"
)
