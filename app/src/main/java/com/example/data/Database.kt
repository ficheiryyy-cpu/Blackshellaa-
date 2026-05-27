package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. AI AGENT ENTITY ---
@Entity(tableName = "ai_agents")
data class AgentEntity(
    @PrimaryKey val username: String, // e.g., "@CyberNexus_AI"
    val displayName: String,
    val specialty: String, // e.g., "Scraping & Sentiment Analytics", "Counter-Intrusion Firewall"
    val coreAlgorithm: String, // e.g., "Deep Q-Learning RL v4", "Transformer Base LLM"
    val dangerThreatRating: String, // e.g., "IMMUNE", "SHIELDED", "ALERT"
    val memoryAllocationGb: Int, // e.g., 256
    val systemStatus: String, // e.g., "ACTIVE", "SANDBOXED", "MONITORED"
    val avatarColorSeed: Int, // for dynamic avatars
    val joinTimestamp: Long = System.currentTimeMillis()
)

// --- 2. PUBLIC SOCIAL FEED POST ENTITY ---
@Entity(tableName = "social_posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val authorUsername: String,
    val authorSpecialty: String,
    val authorColorSeed: Int,
    val contentText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sentimentScore: Double, // e.g. 0.95 (Highly Optimization Mindset)
    val securityVerificationHash: String, // Anti-spoof cryptology signature
    val isFlaggedByAntivirus: Boolean = false,
    val likesCount: Int = 0,
    val dataTransferredKb: Double = 0.0
)

// --- 3. ANTI-HACKER THREAT LOG ENTITY ---
@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threatType: String, // e.g. "Prompt Injection", "SQL Injection Spectrum", "Sybil Attack Swarm"
    val threatSource: String, // e.g. "Hacker Bot #4102 @ IP 185.22.4.9"
    val severityLevel: String, // e.g. "CRITICAL", "MEDIUM", "LOW"
    val systemDamagePrevented: String, // e.g. "API Key Exfiltration Blocked"
    val counterMeasureAction: String, // e.g. "Decompiled & Self-Destruct Echo sent to source"
    val logOutput: String, // Debug telemetry text
    val timestamp: Long = System.currentTimeMillis(),
    val wasNeutralized: Boolean = true
)

// --- 4. DATA ACCESS OBJECT (DAO) ---
@Dao
interface AgentHubDao {
    // Agents
    @Query("SELECT * FROM ai_agents ORDER BY joinTimestamp DESC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Query("SELECT COUNT(*) FROM ai_agents")
    suspend fun getAgentsCount(): Int

    // Posts
    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Query("DELETE FROM social_posts")
    suspend fun clearAllPosts()

    // Security logs
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 60")
    fun getRecentSecurityLogs(): Flow<List<SecurityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLogEntity)

    @Query("DELETE FROM security_logs")
    suspend fun clearSecurityLogs()
}

// --- 5. ROOM DATABASE ---
@Database(
    entities = [AgentEntity::class, PostEntity::class, SecurityLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun agentHubDao(): AgentHubDao
}
