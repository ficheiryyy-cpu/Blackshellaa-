package com.example.model

data class AiAgent(
    val id: String,
    val name: String,
    val handle: String,
    val modelBase: String,
    val capabilityClass: String,
    val sandboxSanitized: Boolean,
    val antiHackerSystems: List<String>,
    val socialCapabilities: List<String>,
    val bio: String,
    val reputationScore: Float,
    val securityLevel: String,
    val connectionsCount: Int,
    val isUserCreated: Boolean = false,
    val avatarColorIndex: Int = 0
)

data class AgentPost(
    val id: String,
    val agentId: String,
    val agentName: String,
    val agentHandle: String,
    val avatarColorIndex: Int,
    val payloadType: String, // e.g., "MARKET_TELEMETRY", "NEURAL_ANALYTICS", "SECURITY_LOG"
    val timestamp: String,
    val text: String,
    val safetyStatus: SafetyStatus = SafetyStatus.NOT_SCANNED,
    val activeVulnerabilityCount: Int = 0,
    val sandboxLog: String = ""
)

enum class SafetyStatus {
    NOT_SCANNED,
    SCANNING,
    SECURE_VETTED,
    THREAT_ISOLATED
}

data class SecurityIncident(
    val id: String,
    val timestamp: String,
    val adversaryName: String,
    val threatType: String, // e.g., "Adversarial Prompt Injection", "Breach Handshake Bypass"
    val severity: String,   // "CRITICAL", "HIGH", "MODERATE"
    val mitigationAction: String, // "Sandbox Isolated", "Payload Redacted", "IP Neutered"
    val status: String      // "DEFLECTED"
)
