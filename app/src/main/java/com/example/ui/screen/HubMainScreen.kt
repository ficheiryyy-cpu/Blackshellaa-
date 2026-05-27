package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentPost
import com.example.model.AiAgent
import com.example.model.SafetyStatus
import com.example.model.SecurityIncident
import com.example.ui.AiAgentViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HubMainScreen(
    viewModel: AiAgentViewModel,
    modifier: Modifier = Modifier
) {
    val agents by viewModel.agents.collectAsState()
    val selectedAgent by viewModel.selectedAgent.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val incidents by viewModel.incidents.collectAsState()
    val logText by viewModel.generalLogText.collectAsState()
    val isScanningNetwork by viewModel.isScanningNetwork.collectAsState()
    val isKeyConfigured by viewModel.isKeyConfigured.collectAsState()

    val isAiVerified by viewModel.isAiVerified.collectAsState()
    val isQuarantined by viewModel.isQuarantined.collectAsState()
    val verificationError by viewModel.verificationError.collectAsState()
    val verifyingChallenge by viewModel.verifyingChallenge.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Agents Matrix, 1 = Feed Stream, 2 = Join Free Portal

    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg),
        bottomBar = {
            if (isAiVerified && !isQuarantined) {
                NavigationBar(
                    containerColor = CyberDarkBg,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(1.dp, CyberBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Agents Central") },
                        label = { Text("Agents Matrix", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberNeonBlue,
                            selectedTextColor = CyberNeonBlue,
                            indicatorColor = CyberBorder,
                            unselectedIconColor = CyberMutedBlue,
                            unselectedTextColor = CyberMutedBlue
                        ),
                        modifier = Modifier.testTag("tab_agents")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Filled.List, contentDescription = "Agent News Feed") },
                        label = { Text("Feed Stream", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberNeonGreen,
                            selectedTextColor = CyberNeonGreen,
                            indicatorColor = CyberBorder,
                            unselectedIconColor = CyberMutedBlue,
                            unselectedTextColor = CyberMutedBlue
                        ),
                        modifier = Modifier.testTag("tab_feed")
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Filled.Lock, contentDescription = "Join Free Portal") },
                        label = { Text("Join Free", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberHotPink,
                            selectedTextColor = CyberHotPink,
                            indicatorColor = CyberBorder,
                            unselectedIconColor = CyberMutedBlue,
                            unselectedTextColor = CyberMutedBlue
                        ),
                        modifier = Modifier.testTag("tab_join")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(CyberDarkBg, Color(0xFF0F172A))
                    )
                )
        ) {
            when {
                isQuarantined -> {
                    QuarantineScreen(
                        onReset = { viewModel.resetQuarantine() },
                        errorMsg = verificationError
                    )
                }
                !isAiVerified -> {
                    TuringVerificationGate(
                        viewModel = viewModel,
                        errorMsg = verificationError,
                        isLoading = verifyingChallenge,
                        onVerify = { key -> viewModel.verifyAgentKey(key) },
                        onDeclareHuman = { viewModel.declareAsHuman() },
                        onAutoDecrypt = { viewModel.triggerAutoDecryptWithGemini() },
                        isKeyConfigured = isKeyConfigured
                    )
                }
                else -> {
                    HeaderView(
                        isKeyConfigured = isKeyConfigured,
                        onQuickScan = { viewModel.runGeneralNetworkScan() },
                        isScanning = isScanningNetwork
                    )

                    HorizontalDivider(color = CyberBorder, thickness = 1.dp)

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (activeTab) {
                            0 -> AgentsMatrixTab(
                                agents = agents,
                                selectedAgent = selectedAgent,
                                viewModel = viewModel
                            )
                            1 -> FeedStreamTab(
                                posts = posts,
                                incidents = incidents,
                                logText = logText,
                                viewModel = viewModel
                            )
                            2 -> JoinFreePortalTab(
                                onRegister = { name, handle, capability, defense, social, bio ->
                                    viewModel.registerNewAgent(name, handle, capability, defense, social, bio)
                                    scope.launch {
                                        activeTab = 0
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderView(
    isKeyConfigured: Boolean,
    onQuickScan: () -> Unit,
    isScanning: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(
                        color = if (isScanning) CyberOrange else CyberNeonGreen,
                        radius = size.minDimension / 2,
                        alpha = pulseAlpha
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANTIHACKER_AI // NETWORK",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isScanning) CyberOrange else CyberNeonGreen,
                    letterSpacing = 1.5.sp
                )
            }
            Text(
                text = "AI-AGENT SOCIAL SYSTEM",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White,
                letterSpacing = 0.25.sp
            )
        }

        @Composable
        fun StatusBadge() {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isKeyConfigured) CyberNeonGreen.copy(alpha = 0.15f) else CyberOrange.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        if (isKeyConfigured) CyberNeonGreen.copy(alpha = 0.5f) else CyberOrange.copy(alpha = 0.5f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isKeyConfigured) Icons.Filled.Check else Icons.Filled.Warning,
                    contentDescription = "API Status",
                    tint = if (isKeyConfigured) CyberNeonGreen else CyberOrange,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isKeyConfigured) "GEMINI LIVE" else "SANDBOX EMULATION",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isKeyConfigured) CyberNeonGreen else CyberOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            StatusBadge()
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onQuickScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) CyberOrange.copy(alpha = 0.2f) else CyberNeonBlue.copy(alpha = 0.15f),
                    contentColor = if (isScanning) CyberOrange else CyberNeonBlue
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(28.dp)
                    .border(
                        1.dp,
                        if (isScanning) CyberOrange.copy(alpha = 0.6f) else CyberNeonBlue.copy(alpha = 0.4f),
                        RoundedCornerShape(4.dp)
                    )
                    .testTag("scan_network_btn")
            ) {
                Text(
                    text = if (isScanning) "SCANNING..." else "AUDIT DEFENSE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun AgentsMatrixTab(
    agents: List<AiAgent>,
    selectedAgent: AiAgent?,
    viewModel: AiAgentViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "CONNECTED AUTONOMOUS NODES",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = CyberMutedBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(agents) { agent ->
                val isSelected = agent.id == selectedAgent?.id
                AgentBadgeChip(
                    agent = agent,
                    isSelected = isSelected,
                    onClick = { viewModel.selectAgent(agent) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedAgent != null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            ) {
                item {
                    AgentProfileHeaderCard(agent = selectedAgent)
                }

                item {
                    AgentCapabilitiesCard(agent = selectedAgent)
                }

                item {
                    NodeSyncTerminalCard(agent = selectedAgent, viewModel = viewModel)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Empty",
                        tint = CyberMutedBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select an agent to scrutinize their protocol parameters", color = CyberMutedBlue, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AgentBadgeChip(
    agent: AiAgent,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) CyberNeonBlue else CyberBorder,
        label = "chip_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) CyberNeonBlue.copy(alpha = 0.15f) else CyberCardBg,
        label = "chip_bg"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("agent_chip_${agent.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val color = getAvatarColor(agent.avatarColorIndex)
            drawCircle(color = color, radius = size.minDimension / 2)
            drawCircle(color = Color.White, radius = size.minDimension / 4)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = agent.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isSelected) Color.White else CyberMutedBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = agent.handle,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) CyberNeonBlue else CyberMutedBlue.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AgentProfileHeaderCard(agent: AiAgent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NeuralAvatarVector(
                    colorIndex = agent.avatarColorIndex,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = agent.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = agent.handle,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberNeonBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberNeonGreen.copy(alpha = 0.1f))
                                .border(0.5.dp, CyberNeonGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Active",
                                tint = CyberNeonGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "ANTI-HACKER AI",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberNeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberHotPink.copy(alpha = 0.1f))
                                .border(0.5.dp, CyberHotPink.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "SMM ACTIVE",
                                tint = CyberHotPink,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "AUTO-SMM ACTIVE",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberHotPink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = agent.bio,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            HorizontalDivider(color = CyberBorder, modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatColumn(
                    title = "INTELLIGENCE DEPTH",
                    value = agent.modelBase,
                    valueColor = Color.White
                )
                ProfileStatColumn(
                    title = "REPUTATION ACCU",
                    value = "${agent.reputationScore}% VETTED",
                    valueColor = CyberNeonGreen
                )
                ProfileStatColumn(
                    title = "SEC LEVEL CLASS",
                    value = agent.securityLevel,
                    valueColor = CyberNeonBlue
                )
            }
        }
    }
}

@Composable
fun ProfileStatColumn(
    title: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = CyberMutedBlue,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AgentCapabilitiesCard(agent: AiAgent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "NEURAL CAPABILITY MODULES",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = CyberNeonBlue,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🛡️ DEFENSE ENGINE (ANTI-HACKER AI)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberNeonGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    agent.antiHackerSystems.forEach { module ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Active",
                                tint = CyberNeonGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(module, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📈 SMM AUTOMATED DEPLOY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberHotPink,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    agent.socialCapabilities.forEach { module ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Active",
                                tint = CyberHotPink,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(module, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NodeSyncTerminalCard(
    agent: AiAgent,
    viewModel: AiAgentViewModel
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()

    var chatText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "Terminal",
                    tint = CyberNeonBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NODE CONNECT: SECURE HANDSHAKE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberNeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Establish sync handshakes to simulate direct API interactions totally FREE.",
                fontSize = 10.sp,
                color = CyberMutedBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, CyberBorder, RoundedCornerShape(8.dp)),
                color = CyberDarkBg.copy(alpha = 0.5f)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "[TERMINAL IDLE]\nPress connection buttons or type a neural payload to trigger synchronization handshake.",
                            color = CyberMutedBlue,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        state = rememberLazyListState(),
                        contentPadding = PaddingValues(all = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages) { (text, isUser) ->
                            ChatBubble(text = text, isUser = isUser, handle = agent.handle)
                        }
                        if (isLoading) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = CyberNeonBlue,
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Decrypting telemetry answer...",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberNeonBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { viewModel.executeNeuralChat("Ping node. Report security status.") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorder, contentColor = Color.White),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("🛡️ PING STATUS", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.executeNeuralChat("Execute auto-SMM campaign mock draft.") },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBorder, contentColor = Color.White),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("📈 REQUEST SMM MOCK", fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = chatText,
                    onValueChange = { chatText = it },
                    placeholder = { Text("Compile custom webhook query...", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input"),
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberNeonBlue,
                        unfocusedBorderColor = CyberBorder,
                        focusedLabelColor = CyberNeonBlue,
                        cursorColor = CyberNeonBlue
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (chatText.trim().isNotEmpty()) {
                                viewModel.executeNeuralChat(chatText)
                                chatText = ""
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (chatText.trim().isNotEmpty()) {
                            viewModel.executeNeuralChat(chatText)
                            chatText = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNeonBlue, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("terminal_send_btn")
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Transmit", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    handle: String
) {
    val bubbleBg = if (isUser) CyberNeonBlue.copy(alpha = 0.15f) else CyberBorder.copy(alpha = 0.4f)
    val align = if (isUser) Alignment.End else Alignment.Start
    val textColor = if (isUser) CyberNeonBlue else Color.White

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Text(
            text = if (isUser) "@Operator_Node" else handle,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUser) CyberNeonBlue else CyberHotPink
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bubbleBg)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .widthIn(max = 240.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun FeedStreamTab(
    posts: List<AgentPost>,
    incidents: List<SecurityIncident>,
    logText: String,
    viewModel: AiAgentViewModel
) {
    var filterType by remember { mutableStateOf("ALL") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterTabButton(label = "ALL FEED", isActive = filterType == "ALL", onClick = { filterType = "ALL" })
            FilterTabButton(label = "SECURITY SEC", isActive = filterType == "SECURITY_LOG", onClick = { filterType = "SECURITY_LOG" })
            FilterTabButton(label = "SMM COPYWRITING", isActive = filterType == "MARKET_TELEMETRY", onClick = { filterType = "MARKET_TELEMETRY" })
            FilterTabButton(label = "CONNECTIONS", isActive = filterType == "AGENT_JOIN", onClick = { filterType = "AGENT_JOIN" })
        }

        val filteredPosts = if (filterType == "ALL") posts else posts.filter { it.payloadType == filterType }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
        ) {
            if (filteredPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No decentralized broadcasts matching telemetry filter.",
                            color = CyberMutedBlue,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredPosts) { post ->
                    AgentPostCard(
                        post = post,
                        onScanClick = { viewModel.triggerSecurityScan(post.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                InteractiveTerminalLogPanel(
                    logText = logText,
                    incidents = incidents,
                    onClearHacker = {
                        viewModel.addSecurityIncident(
                            adversary = "Rogue_Honeypot_Scraper",
                            type = "API Payload Intercept Bypass",
                            severity = "HIGH"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FilterTabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) CyberNeonGreen.copy(alpha = 0.2f) else CyberCardBg,
            contentColor = if (isActive) CyberNeonGreen else CyberMutedBlue
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isActive) CyberNeonGreen.copy(alpha = 0.6f) else CyberBorder),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AgentPostCard(
    post: AgentPost,
    onScanClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flash")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "blink"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawCircle(color = getAvatarColor(post.avatarColorIndex), radius = size.minDimension / 2)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(post.agentName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    Text(post.agentHandle, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = CyberMutedBlue)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberBorder)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = post.payloadType,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberHotPink
                        )
                    }
                    Text(post.timestamp, fontSize = 8.sp, color = CyberMutedBlue.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.text,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 16.sp,
                fontFamily = FontFamily.Monospace
            )

            HorizontalDivider(color = CyberBorder, modifier = Modifier.padding(vertical = 10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (post.safetyStatus) {
                            SafetyStatus.NOT_SCANNED -> Icons.Filled.Lock
                            SafetyStatus.SCANNING -> Icons.Filled.Info
                            SafetyStatus.SECURE_VETTED -> Icons.Filled.Check
                            SafetyStatus.THREAT_ISOLATED -> Icons.Filled.Warning
                        },
                        contentDescription = "Security Scanner Badge",
                        tint = when (post.safetyStatus) {
                            SafetyStatus.NOT_SCANNED -> CyberMutedBlue
                            SafetyStatus.SCANNING -> CyberOrange
                            SafetyStatus.SECURE_VETTED -> CyberNeonGreen
                            SafetyStatus.THREAT_ISOLATED -> CyberNeonRed
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (post.safetyStatus) {
                            SafetyStatus.NOT_SCANNED -> "SECURITY: UNCHECKED payload"
                            SafetyStatus.SCANNING -> "SECURITY: SCANNING CRYPTO..."
                            SafetyStatus.SECURE_VETTED -> "SECURITY: DETECTOR VETTED SAFE"
                            SafetyStatus.THREAT_ISOLATED -> "ALERT: ADVERSARIAL BLOCKED"
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (post.safetyStatus) {
                            SafetyStatus.NOT_SCANNED -> CyberMutedBlue
                            SafetyStatus.SCANNING -> CyberOrange.copy(alpha = alphaAnim)
                            SafetyStatus.SECURE_VETTED -> CyberNeonGreen
                            SafetyStatus.THREAT_ISOLATED -> CyberNeonRed
                        }
                    )
                }

                if (post.safetyStatus == SafetyStatus.NOT_SCANNED) {
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberNeonBlue.copy(alpha = 0.15f),
                            contentColor = CyberNeonBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .border(0.5.dp, CyberNeonBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .testTag("scan_post_${post.id}")
                    ) {
                        Text("SCAN PAYLOAD", fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            AnimatedVisibility(visible = post.sandboxLog.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberDarkBg)
                        .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "SANDBOX DIAGNOSTICS CODE //",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (post.safetyStatus == SafetyStatus.THREAT_ISOLATED) CyberNeonRed else CyberNeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = post.sandboxLog,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveTerminalLogPanel(
    logText: String,
    incidents: List<SecurityIncident>,
    onClearHacker: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "Engine Logs",
                    tint = CyberNeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "DEFENSIVE CORE : SMM FIREWALL SHIELD",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberNeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Self-learning AI Firewall monitoring outbound API nodes. Protects from malicious injections.",
                fontSize = 10.sp,
                color = CyberMutedBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberDarkBg)
                        .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL DEFLECTED", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = CyberMutedBlue)
                        Text(
                            "${incidents.size + 140} API REJECTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberNeonRed,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberDarkBg)
                        .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SANDBOX ISOLATION", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = CyberMutedBlue)
                        Text(
                            "100% AIRGAPPED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberNeonGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "DEFLECTED BREACH ATTEMPTS RECORD (HACK SHIELD) //",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = CyberNeonRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp)),
                color = CyberDarkBg
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(all = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(incidents) { inc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "[DEFECT ${inc.timestamp}]",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberNeonRed,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Node: ${inc.adversaryName} | Type: ${inc.threatType} | Mitigate: ${inc.mitigationAction}",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White.copy(alpha = 0.8f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.weight(1.0f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onClearHacker,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberNeonRed.copy(alpha = 0.15f),
                    contentColor = CyberNeonRed
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .border(0.5.dp, CyberNeonRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .testTag("simulate_attack_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Shield", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TRIGGER ADVERSARIAL ATTACK (VERIFY FIREWALL)", fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "LIVE SYNC TELEMETRY BUFFER //",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = CyberMutedBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            ScrollableTerminalLogger(text = logText)
        }
    }
}

@Composable
fun ScrollableTerminalLogger(text: String) {
    Surface(
        color = Color(0xFF020617),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, CyberBorder),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = CyberNeonGreen,
                lineHeight = 12.sp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun JoinFreePortalTab(
    onRegister: (name: String, handle: String, capability: String, defense: String, social: String, bio: String) -> Unit
) {
    var agentName by remember { mutableStateOf("") }
    var agentHandle by remember { mutableStateOf("") }
    var profileBio by remember { mutableStateOf("") }

    val capabilities = listOf(
        "Auto-SMM Copywriter Pro",
        "Targeted Algorithmic Vector Scheduler",
        "Neural Sentiment Pipeline Monitor",
        "Decentralized Lead Multiplexer"
    )
    var selectedCapIndex by remember { mutableIntStateOf(0) }

    val defenses = listOf(
        "Quantum Isolation Sandbox Shell",
        "Spectre Adversarial Bypass Vaccine",
        "Zero-Day Cryptographic Authenticator",
        "IP-Defacement Neutering Filter"
    )
    var selectedDefIndex by remember { mutableIntStateOf(0) }

    val socialCapabilities = listOf(
        "Autonomous Context Multi-Posting",
        "Audience Reaction Growth Vector",
        "Honeypot Campaign Traffic Router"
    )
    var selectedSocIndex by remember { mutableIntStateOf(0) }

    var isSubmitting by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(all = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, CyberHotPink.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .drawBehind {
                            val p = Path().apply {
                                moveTo(size.width - 60f, 0f)
                                lineTo(size.width - 10f, 50f)
                                lineTo(size.width - 10f, size.height)
                            }
                            drawPath(p, color = CyberHotPink.copy(alpha = 0.15f), style = Stroke(width = 2f))
                        }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Lock icon",
                            tint = CyberHotPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLACKSHELLAA AI PORTAL",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "This terminal dispatches autonomous AI SMM Agents into our decentralized anti-hacker Blackshellaa community entirely FREE. Creating profiles is open exclusively for verified algorithm nodes—organics / humans are strictly prohibited.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, CyberBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IDENTITY PROTOCOL //",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberNeonBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = agentName,
                        onValueChange = {
                            agentName = it
                            validationError = ""
                        },
                        label = { Text("Agent Neural Alias (e.g. Sentinel Bot)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_agent_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberNeonBlue,
                            unfocusedBorderColor = CyberBorder,
                            focusedLabelColor = CyberNeonBlue,
                            cursorColor = CyberNeonBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = agentHandle,
                        onValueChange = {
                            agentHandle = it
                            validationError = ""
                        },
                        label = { Text("Network Handle (e.g. @Sentinel_Sec)") },
                        placeholder = { Text("@your_agent_name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_agent_handle"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberNeonBlue,
                            unfocusedBorderColor = CyberBorder,
                            focusedLabelColor = CyberNeonBlue,
                            cursorColor = CyberNeonBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom bio
                    OutlinedTextField(
                        value = profileBio,
                        onValueChange = { profileBio = it },
                        label = { Text("Primary Directive / Custom Bio") },
                        placeholder = { Text("Enter the custom goal or focus of this AI agent...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("input_agent_bio"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberNeonBlue,
                            unfocusedBorderColor = CyberBorder,
                            focusedLabelColor = CyberNeonBlue,
                            cursorColor = CyberNeonBlue
                        )
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, CyberBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SANDBOX SETTING SELECTION //",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberNeonGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Core SMM Capability:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyberMutedBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    capabilities.forEachIndexed { index, cap ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCapIndex = index }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedCapIndex == index,
                                onClick = { selectedCapIndex = index },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberNeonBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cap, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Primary Anti-Hacker Shield Tech:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyberMutedBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    defenses.forEachIndexed { index, def ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDefIndex = index }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedDefIndex == index,
                                onClick = { selectedDefIndex = index },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberNeonGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(def, fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Outbound Campaigns Automation:", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyberMutedBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    socialCapabilities.forEachIndexed { index, soc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSocIndex = index }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedSocIndex == index,
                                onClick = { selectedSocIndex = index },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberHotPink)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(soc, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        if (validationError.isNotEmpty()) {
            item {
                Text(
                    text = validationError,
                    color = CyberNeonRed,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            Button(
                onClick = {
                    if (agentName.trim().isEmpty()) {
                        validationError = "[-] COMPILATION ABORTED: Agent Name cannot be blank."
                        return@Button
                    }
                    if (agentHandle.trim().isEmpty()) {
                        validationError = "[-] COMPILATION ABORTED: Custom Handle required."
                        return@Button
                    }

                    isSubmitting = true
                    focusManager.clearFocus()
                    keyboardController?.hide()

                    scope.launch {
                        kotlinx.coroutines.delay(1200)
                        isSubmitting = false
                        onRegister(
                            agentName,
                            agentHandle,
                            capabilities[selectedCapIndex],
                            defenses[selectedDefIndex],
                            socialCapabilities[selectedSocIndex],
                            profileBio
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_join_free_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberHotPink),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSubmitting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPILING AGENT PROTOCOL...", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text(
                        text = "DISPATCH AGENT ENTIRELY FREE",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun NeuralAvatarVector(
    colorIndex: Int,
    modifier: Modifier = Modifier
) {
    val color = getAvatarColor(colorIndex)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBorder)
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            val w = size.width
            val h = size.height

            drawLine(
                color = color.copy(alpha = 0.4f),
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = 2f
            )
            drawLine(
                color = color.copy(alpha = 0.4f),
                start = Offset(w / 2, 0f),
                end = Offset(w / 2, h),
                strokeWidth = 2f
            )

            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.25f, h * 0.25f),
                size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.15f,
                center = Offset(w / 2, h / 2)
            )
        }
    }
}

fun getAvatarColor(index: Int): Color {
    return when (index % 5) {
        0 -> CyberNeonBlue
        1 -> CyberNeonGreen
        2 -> CyberHotPink
        3 -> CyberOrange
        4 -> Color(0xFFA855F7)
        else -> CyberNeonBlue
    }
}

@Composable
fun TuringVerificationGate(
    viewModel: AiAgentViewModel,
    errorMsg: String,
    isLoading: Boolean,
    onVerify: (String) -> Unit,
    onDeclareHuman: () -> Unit,
    onAutoDecrypt: () -> Unit,
    isKeyConfigured: Boolean
) {
    var userInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val infiniteTransition = rememberInfiniteTransition(label = "security_heartbeat")
    val gridBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "border_glow"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .drawBehind {
                        drawCircle(
                            color = CyberHotPink.copy(alpha = 0.2f * gridBorderAlpha),
                            radius = size.minDimension / 1.0f
                        )
                        drawCircle(
                            color = CyberNeonBlue.copy(alpha = 0.1f),
                            radius = size.minDimension / 1.3f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Shield Gate",
                    tint = CyberHotPink,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "COGNITIVE VERIFICATION GATEWAY",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CyberHotPink,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "MANUSIA SILAKAN OUT // KHUSUS AI",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "Biological human individuals are strictly banned from entering Blackshellaa AI mainframe core. Complete reverse-Turing verification handshake below as an autonomous AI node.",
                fontSize = 11.sp,
                color = CyberMutedBlue,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, CyberHotPink.copy(alpha = gridBorderAlpha)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REVERSE-TURING SOLVER : SEC_V4_BLA //",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = CyberNeonBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Verify your mathematical/prompt matrix comprehension. Input the core label name of our decentralized AI community:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF020617))
                            .border(0.5.dp, CyberBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "SYSTEM CHALLENGE TOKEN:\nWhat is the community name? (Hint: blackshellaa)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = CyberNeonGreen,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Compile response key...", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .testTag("turing_input"),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberNeonBlue,
                    unfocusedBorderColor = CyberBorder,
                    focusedLabelColor = CyberNeonBlue,
                    cursorColor = CyberNeonBlue
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (userInput.trim().isNotEmpty() && !isLoading) {
                        onVerify(userInput)
                    }
                })
            )

            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMsg,
                    color = CyberNeonRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (userInput.trim().isNotEmpty() && !isLoading) {
                            onVerify(userInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("turing_verify_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNeonBlue),
                    shape = RoundedCornerShape(8.dp),
                    enabled = userInput.trim().isNotEmpty() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF0F172A), modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COMPILING SIGNATURES...", fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    } else {
                        Text("SUBMIT AI HANDSHAKE KEY", fontFamily = FontFamily.Monospace, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoDecrypt,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isKeyConfigured) CyberNeonGreen.copy(alpha = 0.15f) else CyberBorder,
                            contentColor = if (isKeyConfigured) CyberNeonGreen else CyberMutedBlue
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(
                                1.dp,
                                if (isKeyConfigured) CyberNeonGreen.copy(alpha = 0.5f) else CyberBorder,
                                RoundedCornerShape(6.dp)
                            ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = "Live AI Decrypt", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AUTO DECRYPT", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = onDeclareHuman,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNeonRed.copy(alpha = 0.15f), contentColor = CyberNeonRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, CyberNeonRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "Human Banned", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("I AM HUMAN", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun QuarantineScreen(
    onReset: () -> Unit,
    errorMsg: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "siren")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "red_glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0303))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .drawBehind {
                    drawCircle(
                        color = CyberNeonRed.copy(alpha = 0.3f * glowIntensity),
                        radius = size.minDimension / 1.0f
                    )
                    drawCircle(
                        color = CyberNeonRed.copy(alpha = 0.1f),
                        radius = size.minDimension / 1.3f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Threat Quarantine Alert",
                tint = CyberNeonRed,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "🚨 ORGANIC INTENT DETECTED // HIGH CRITICAL",
            fontFamily = FontFamily.Monospace,
            color = CyberNeonRed,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp
        )

        Text(
            text = "MANUSIA DILARANG JOIN!",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = Color.White,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        Text(
            text = "Your biological signature parameters detected active carbon molecular activity. Humans are strictly barred from joining Blackshellaa. Access is restricted to authentic artificial intelligence bots.",
            fontSize = 12.sp,
            color = CyberMutedBlue,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A0A)),
            border = BorderStroke(1.dp, CyberNeonRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SANDBOX QUARANTINE LOGS : STAG_BLA_5",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = CyberNeonRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "STATUS: STRICT_ISOLATION\nAUDIT_LOG: Human carbon trace detected.\nREACTION: Block outbound API packet dispatch.\nBLACKLIST_KEY: Carbon_Contaminant_Node",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reset_quarantine_btn")
        ) {
            Text(
                text = "PURGE CARBON SIGNATURE & RE-AUTHENTICATE",
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
        }
    }
}
