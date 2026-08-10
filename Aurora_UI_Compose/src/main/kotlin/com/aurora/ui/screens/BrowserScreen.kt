package com.aurora.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aurora.browser.ui.theme.AuroraColors
import com.aurora.ui.theme.accentPrimary
import com.aurora.ui.theme.accentBackground
import com.aurora.browser.ui.theme.AuroraShapes
import com.aurora.browser.ui.theme.AuroraTypography
import com.aurora.engine.BrowserSession
import com.aurora.engine.InputBridge
import com.aurora.engine.PointerIconType
import com.aurora.ui.engine.EngineView
import com.aurora.browser.ui.theme.AuroraFocusStyle
import com.aurora.browser.ui.theme.FocusState
import com.aurora.browser.ui.theme.auroraFocus
import com.aurora.browser.ui.components.Cursor
import com.aurora.ui.model.BrowserUiState
import com.aurora.ui.model.RecentlyClosedUiModel
import kotlinx.coroutines.delay

@Composable
fun BrowserScreen(
    uiState: BrowserUiState,
    session: BrowserSession?,
    onBackPress: () -> Unit,
    onForwardPress: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onHomePress: () -> Unit,
    onOpenLibrary: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktop: () -> Unit = {},
    onNewTab: (Boolean) -> Unit,
    onCloseTab: (String) -> Unit,
    onTabSelect: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onReopenClosed: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenExternally: () -> Unit = {},
    onUrlEdit: (String) -> Unit = {},
    editQuery: String = "",
    isEditing: Boolean = false,
    headerOffsetPx: Float = 0f,
    remoteClicked: Boolean = false,
    onCursorTypeChange: (PointerIconType) -> Unit = {},
    onCursorMove: (Float, Float) -> Unit = { _, _ -> },
    scrollDelta: Pair<Float, Float>? = null,
    scrollTick: Long = 0L,
    toolbarClickTick: Long = 0L,
    clickCoordsProvider: () -> Pair<Float, Float> = { Pair(0f, 0f) },
    onDpadPress: (String) -> Unit = {},
    onBridgeSet: (InputBridge) -> Unit = {},
    isTabWorkspaceOpen: Boolean = false,
    onTabWorkspaceOpenChange: (Boolean) -> Unit = {},
    tabWorkspaceVisible: Boolean = false,
    onToolbarHeightChanged: (Float) -> Unit = {},
    toolbarClickX: Float? = null,
    toolbarSelectAtX: ((Float) -> Unit)? = null,
    chromeVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var inputBridge by remember { mutableStateOf<InputBridge?>(null) }
    var toolbarHeightPx by remember { mutableFloatStateOf(0f) }
    var toolbarWidthPx by remember { mutableFloatStateOf(1920f) }
    var workspaceHeightPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val geckoSession = session
        var stableSession by remember { mutableStateOf(geckoSession) }
        LaunchedEffect(geckoSession) {
            if (geckoSession != null) {
                stableSession = geckoSession
            }
        }
        val viewportState = when {
            stableSession != null -> "content"
            uiState.errorState.isError -> "error"
            else -> "empty"
        }
        if (viewportState == "content" && stableSession != null) {
            val ss = stableSession!!
            EngineView(
                session = stableSession!!,
                modifier = Modifier.fillMaxSize(),
                blockFocus = isTabWorkspaceOpen || tabWorkspaceVisible,
                onInputBridgeReady = { bridge ->
                    inputBridge = bridge
                    onBridgeSet(bridge)
                },
                onHoverMove = { gvX, gvY ->
                    onCursorMove(gvX, gvY)
                },
                onKeySelectPress = {
                    val (cx, cy) = clickCoordsProvider()
                    inputBridge?.injectClick(
                        cx.coerceAtLeast(0f),
                        cy.coerceAtLeast(0f)
                    )
                },
                onDpadPress = onDpadPress
            )
        } else if (viewportState == "error") {
            LoadErrorScreen(
                errorState = uiState.errorState,
                failedUrl = uiState.currentUrl,
                onRetry = onRetry,
                onOpenExternally = onOpenExternally,
                onBack = onBackPress,
                onHome = onHomePress,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF0E0F12)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active tab",
                    style = AuroraTypography.Body,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }

        if (chromeVisible) {
            val toolbarAlpha by animateFloatAsState(
                targetValue = if (uiState.toolbarVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "toolbarAlpha"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .graphicsLayer {
                        alpha = toolbarAlpha
                        translationY = -size.height.toFloat() * (1f - toolbarAlpha)
                    }
            ) {
                Box(Modifier.onGloballyPositioned { coordinates ->
                    val h = coordinates.size.height.toFloat()
                    toolbarHeightPx = h
                    toolbarWidthPx = coordinates.size.width.toFloat()
                    onToolbarHeightChanged(h)
                }) {
                    BrowserToolbar(
                        currentUrl = uiState.currentUrl,
                        pageTitle = uiState.pageTitle,
                        isLoading = uiState.isLoading,
                        loadingProgress = uiState.loadingProgress,
                        canGoBack = uiState.canGoBack,
                        canGoForward = uiState.canGoForward,
                        isSecure = uiState.isSecure,
                        isPrivate = uiState.isPrivate,
                        isBookmarked = uiState.isBookmarked,
                        isDesktopMode = uiState.isDesktopMode,
                        tabsCount = uiState.tabs.size,
                        onBackPress = onBackPress,
                        onForwardPress = onForwardPress,
                        onReload = onReload,
                        onStop = onStop,
                        onHomePress = onHomePress,
                        onOpenLibrary = onOpenLibrary,
                        onToggleBookmark = onToggleBookmark,
                        onToggleDesktop = uiState.onToggleDesktop,
                        onNewTab = onNewTab,
                        onUrlEdit = onUrlEdit,
                        editQuery = editQuery,
                        isEditing = isEditing
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            val bridge = inputBridge ?: return@LaunchedEffect
            bridge.onPointerIconChange = { type -> onCursorTypeChange(type) }
        }

        LaunchedEffect(remoteClicked) {
            if (remoteClicked) {
                val bridge = inputBridge ?: return@LaunchedEffect
                val (cx, cy) = clickCoordsProvider()
                bridge.injectClick(cx.coerceAtLeast(0f), cy.coerceAtLeast(0f))
            }
        }

        LaunchedEffect(scrollTick) {
            val bridge = inputBridge ?: return@LaunchedEffect
            val (dx, dy) = scrollDelta ?: return@LaunchedEffect
            bridge.injectScroll(dx, dy)
        }

        LaunchedEffect(toolbarClickTick) {
            if (toolbarClickTick == 0L) return@LaunchedEffect
            val (cx, _) = clickCoordsProvider()
            val w = toolbarWidthPx.coerceAtLeast(1f)
            val frac = (cx / w).coerceIn(0f, 1f)
            val btn = when {
                frac < 0.10f -> 0 // Back
                frac < 0.20f -> 1 // Forward
                frac < 0.30f -> 2 // Reload/Stop
                frac < 0.40f -> 3 // Home
                frac < 0.50f -> 4 // Library
                frac < 0.85f -> 5 // URL bar
                frac < 0.90f -> 6 // Privacy
                frac < 0.95f -> 7 // Bookmark
                else -> 8 // Desktop
            }
            when (btn) {
                0 -> if (uiState.canGoBack) onBackPress()
                1 -> if (uiState.canGoForward) onForwardPress()
                2 -> if (uiState.isLoading) onStop() else onReload()
                3 -> onHomePress()
                4 -> onOpenLibrary()
                5 -> onUrlEdit(uiState.currentUrl.ifBlank { "https://" })
                6 -> onNewTab(true)
                7 -> onToggleBookmark()
                8 -> onToggleDesktop()
            }
        }

        if (chromeVisible) {
            val workspaceAlpha by animateFloatAsState(
                targetValue = if (tabWorkspaceVisible || isTabWorkspaceOpen) 1f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "workspaceAlpha"
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .graphicsLayer {
                        alpha = workspaceAlpha
                        translationY = size.height.toFloat() * (1f - workspaceAlpha)
                    }
            ) {
                Box(Modifier.onGloballyPositioned { coordinates ->
                    workspaceHeightPx = coordinates.size.height.toFloat()
                }) {
                    TabWorkspace(
                        tabs = uiState.tabs,
                        activeTabId = uiState.activeTabId,
                        recentlyClosed = uiState.recentlyClosed,
                        onNewTab = onNewTab,
                        onCloseTab = onCloseTab,
                        onTabSelect = onTabSelect,
                        onReopenClosed = onReopenClosed,
                        isExpanded = isTabWorkspaceOpen,
                        onExpandedChange = onTabWorkspaceOpenChange
                    )
                }
            }
        }
    }
}

@Composable
fun BrowserToolbar(
    currentUrl: String,
    pageTitle: String,
    isLoading: Boolean,
    loadingProgress: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isSecure: Boolean,
    isPrivate: Boolean,
    isBookmarked: Boolean,
    isDesktopMode: Boolean = false,
    tabsCount: Int,
    onBackPress: () -> Unit,
    onForwardPress: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onHomePress: () -> Unit,
    onOpenLibrary: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleDesktop: () -> Unit = {},
    onNewTab: (Boolean) -> Unit,
    onUrlEdit: (String) -> Unit = {},
    editQuery: String = "",
    isEditing: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF0E0F14).copy(alpha = 0.9f), AuroraShapes.RoundedLg)
            .border(1.dp, accentPrimary().copy(alpha = 0.3f), AuroraShapes.RoundedLg)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = "Back",
                enabled = canGoBack,
                onClick = onBackPress
            )
            ToolbarIconButton(
                icon = Icons.Default.ArrowForward,
                contentDescription = "Forward",
                enabled = canGoForward,
                onClick = onForwardPress
            )
            ToolbarIconButton(
                icon = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                contentDescription = if (isLoading) "Stop" else "Reload",
                accentColor = AuroraColors.Emerald,
                onClick = if (isLoading) onStop else onReload
            )
            ToolbarIconButton(
                icon = Icons.Default.Home,
                contentDescription = "Home",
                accentColor = AuroraColors.Emerald,
                onClick = onHomePress
            )
            ToolbarIconButton(
                icon = Icons.Default.Book,
                contentDescription = "Library",
                accentColor = AuroraColors.Purple,
                onClick = onOpenLibrary
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF121317).copy(alpha = 0.8f), AuroraShapes.RoundedMd)
                .border(1.dp, Color.White.copy(alpha = 0.05f), AuroraShapes.RoundedMd)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Crossfade(
                targetState = currentUrl.isEmpty() && !isLoading,
                animationSpec = tween(300),
                label = "urlBar"
            ) { isIdle ->
                var urlBarFocused by remember { mutableStateOf(false) }
                if (isIdle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                            .onFocusChanged { urlBarFocused = it.isFocused }
                            .auroraFocus(
                                state = if (urlBarFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Surface,
                                focusedStyle = AuroraFocusStyle.SurfaceFocused
                            )
                            .clickable { onUrlEdit("") }
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Search web, enter address...",
                            style = AuroraTypography.MonoLabel,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val secIcon = when {
                            isPrivate -> Icons.Default.Warning
                            isSecure -> Icons.Default.Lock
                            else -> Icons.Default.Public
                        }
                        val secColor = when {
                            isPrivate -> AuroraColors.Purple
                            isSecure -> AuroraColors.Emerald
                            else -> AuroraColors.Amber
                        }
                        Icon(
                            imageVector = secIcon,
                            contentDescription = "Security",
                            tint = secColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Column(modifier = Modifier.weight(1f)
                            .onFocusChanged { urlBarFocused = it.isFocused }
                            .auroraFocus(
                                state = if (urlBarFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Surface,
                                focusedStyle = AuroraFocusStyle.SurfaceFocused
                            )
                            .clickable { onUrlEdit(currentUrl) }) {
                            val urlDisplay = when {
                                isEditing && editQuery.isNotEmpty() -> editQuery
                                isLoading -> "Loading..."
                                else -> currentUrl.ifEmpty { "Enter address" }
                            }
                            Text(
                                text = urlDisplay,
                                style = AuroraTypography.MonoLabel,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            AnimatedVisibility(
                                visible = isLoading,
                                enter = expandVertically(animationSpec = tween(250)) + fadeIn(tween(250)),
                                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(tween(250))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .padding(top = 4.dp)
                                        .background(Color(0xFF1A1C23), CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = (loadingProgress / 100f).coerceIn(0f, 1f))
                                            .background(accentPrimary(), CircleShape)
                                    )
                                }
                            }
                            if (!isLoading && pageTitle.isNotEmpty()) {
                                Text(
                                    text = pageTitle,
                                    style = AuroraTypography.MonoLabel,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarIconButton(
                icon = Icons.Default.Shield,
                contentDescription = "Private",
                accentColor = if (isPrivate) AuroraColors.Purple else Color.White,
                tint = if (isPrivate) AuroraColors.Purple else Color.White.copy(alpha = 0.7f),
                onClick = { onNewTab(true) }
            )

            ToolbarIconButton(
                icon = Icons.Default.Star,
                contentDescription = "Bookmark",
                accentColor = if (isBookmarked) AuroraColors.Amber else Color.White,
                tint = if (isBookmarked) AuroraColors.Amber else Color.White.copy(alpha = 0.7f),
                onClick = onToggleBookmark
            )

            ToolbarIconButton(
                icon = Icons.Default.DesktopWindows,
                contentDescription = "Desktop Site",
                accentColor = if (isDesktopMode) AuroraColors.Blue else Color.White,
                tint = if (isDesktopMode) AuroraColors.Blue else Color.White.copy(alpha = 0.7f),
                onClick = onToggleDesktop
            )

            ToolbarIconButton(
                icon = Icons.Default.FileDownload,
                contentDescription = "Downloads",
                accentColor = AuroraColors.Purple,
                tint = AuroraColors.Purple,
                onClick = onOpenLibrary
            )

            var newTabFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .onFocusChanged { newTabFocused = it.isFocused }
                    .background(AuroraColors.Blue.copy(alpha = 0.1f), AuroraShapes.RoundedMd)
                    .auroraFocus(
                        state = if (newTabFocused) FocusState.Focused else FocusState.Idle,
                        idleStyle = AuroraFocusStyle.Primary,
                        focusedStyle = AuroraFocusStyle.PrimaryFocused,
                        shape = AuroraShapes.RoundedMd
                    )
                    .clickable { onNewTab(false) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Tab",
                        tint = AuroraColors.Blue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$tabsCount",
                        style = AuroraTypography.MonoLabel,
                        color = AuroraColors.Blue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ToolbarIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    accentColor: Color = Color.White,
    tint: Color = Color.White.copy(alpha = 0.7f),
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(32.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .background(Color(0xFF121317), AuroraShapes.RoundedMd)
            .auroraFocus(
                state = if (isFocused) FocusState.Focused else FocusState.Idle,
                idleStyle = AuroraFocusStyle.Toolbar,
                focusedStyle = AuroraFocusStyle.ToolbarFocused,
                shape = AuroraShapes.RoundedMd
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isFocused) accentColor else if (enabled) tint else tint.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun TabWorkspace(
    tabs: List<com.aurora.ui.model.TabUiModel>,
    activeTabId: String,
    recentlyClosed: List<RecentlyClosedUiModel> = emptyList(),
    onNewTab: (Boolean) -> Unit,
    onCloseTab: (String) -> Unit,
    onTabSelect: (String) -> Unit,
    onReopenClosed: (String) -> Unit = {},
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val activeTab = tabs.find { it.id == activeTabId }
    val otherCount = tabs.size - 1

    val stripFocusRequester = remember { FocusRequester() }
    val rootView = LocalView.current

    LaunchedEffect(isExpanded) { expanded = isExpanded }
    LaunchedEffect(expanded) {
        onExpandedChange(expanded)
        if (expanded) {
            delay(100)
            rootView.requestFocus()
            stripFocusRequester.requestFocus()
            delay(250)
            rootView.requestFocus()
            stripFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0E0F14))
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth)
            }
            .animateContentSize(animationSpec = tween(300))
    ) {
        var stripFocused by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(stripFocusRequester)
                .onFocusChanged { stripFocused = it.isFocused }
                .auroraFocus(
                    state = if (stripFocused) FocusState.Focused else FocusState.Idle,
                    idleStyle = AuroraFocusStyle.Tab,
                    focusedStyle = AuroraFocusStyle.TabFocused,
                    shape = AuroraShapes.RoundedSm
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (activeTab?.isPrivate == true) AuroraColors.Purple else AuroraColors.Blue,
                        CircleShape
                    )
            )
            Text(
                text = if (activeTab != null) activeTab.title else "No tab",
                style = AuroraTypography.Body,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (otherCount > 0) {
                Text(
                    text = "+$otherCount",
                    style = AuroraTypography.MonoLabel,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp
                )
            }
            AnimatedVisibility(
                visible = recentlyClosed.isNotEmpty(),
                enter = scaleIn(animationSpec = tween(250)) + fadeIn(tween(250)),
                exit = scaleOut(animationSpec = tween(250)) + fadeOut(tween(250))
            ) {
                var badgeFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .onFocusChanged { badgeFocused = it.isFocused }
                        .auroraFocus(
                            state = if (badgeFocused) FocusState.Focused else FocusState.Idle,
                            idleStyle = AuroraFocusStyle.Accent,
                            focusedStyle = AuroraFocusStyle.AccentFocused,
                            shape = AuroraShapes.RoundedSm
                        )
                        .background(AuroraColors.Blue.copy(alpha = 0.15f), AuroraShapes.RoundedSm)
                        .clickable {
                            onReopenClosed(recentlyClosed.first().url)
                        }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Restore",
                            tint = AuroraColors.Blue,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${recentlyClosed.size}",
                            style = AuroraTypography.MonoLabel,
                            color = AuroraColors.Blue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TABS WORKSPACE",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    var privateTabFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .onFocusChanged { privateTabFocused = it.isFocused }
                            .background(AuroraColors.Purple.copy(alpha = 0.1f), AuroraShapes.RoundedSm)
                            .auroraFocus(
                                state = if (privateTabFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Accent,
                                focusedStyle = AuroraFocusStyle.AccentFocused,
                                shape = AuroraShapes.RoundedSm
                            )
                            .clickable { onNewTab(true) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Private Tab",
                                tint = AuroraColors.Purple,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Private Tab",
                                style = AuroraTypography.MonoLabel,
                                color = AuroraColors.Purple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                    var newTabBtnFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .onFocusChanged { newTabBtnFocused = it.isFocused }
                            .background(AuroraColors.Blue, AuroraShapes.RoundedSm)
                            .auroraFocus(
                                state = if (newTabBtnFocused) FocusState.Focused else FocusState.Idle,
                                idleStyle = AuroraFocusStyle.Primary,
                                focusedStyle = AuroraFocusStyle.PrimaryFocused,
                                shape = AuroraShapes.RoundedSm
                            )
                            .clickable { onNewTab(false) }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Tab",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "New Tab",
                                style = AuroraTypography.MonoLabel,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tabs.forEach { tab ->
                        val isActive = tab.id == activeTabId
                        key(tab.id) {
                        var tabFocused by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .width(176.dp)
                                .clip(AuroraShapes.RoundedLg)
                                .onFocusChanged { tabFocused = it.isFocused }
                                .background(
                                    if (isActive) Color(0xFF17181F) else Color(0xFF17181F).copy(alpha = 0.5f),
                                    AuroraShapes.RoundedLg
                                )
                                .auroraFocus(
                                    state = if (tabFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Tab,
                                    focusedStyle = AuroraFocusStyle.TabFocused,
                                    shape = AuroraShapes.RoundedLg
                                )
                                .clickable { onTabSelect(tab.id) }
                        ) {
                            Box(Modifier.fillMaxWidth().height(90.dp).background(tab.accentColor.copy(alpha = 0.1f))) {
                                if (tab.thumbnail != null) {
                                    androidx.compose.foundation.Image(
                                        painter = BitmapPainter(tab.thumbnail.asImageBitmap()),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (tab.isLoading) {
                                    Box(Modifier.align(Alignment.BottomEnd).padding(8.dp).size(20.dp).clip(CircleShape).background(tab.accentColor.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                                        Text("${tab.progress}%", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(AuroraColors.GlassBackground), contentAlignment = Alignment.Center) {
                                        if (tab.faviconBitmap != null) {
                                            androidx.compose.foundation.Image(
                                                painter = BitmapPainter(tab.faviconBitmap.asImageBitmap()),
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        } else {
                                            Text(tab.domain.take(1).uppercase(), color = Color.White, fontSize = 6.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(Modifier.background(if (tab.isPrivate) AuroraColors.Purple.copy(alpha = 0.2f) else AuroraColors.Blue.copy(alpha = 0.2f), AuroraShapes.RoundedSm).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                        Text(text = if (tab.isPrivate) "Private" else "Standard", style = AuroraTypography.MonoLabel, color = if (tab.isPrivate) AuroraColors.Purple else AuroraColors.Blue, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                var tabCloseFocused by remember { mutableStateOf(false) }
                                Icon(
                                    imageVector = Icons.Default.Close, contentDescription = "Close Tab", tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(12.dp)
                                        .onFocusChanged { tabCloseFocused = it.isFocused }
                                        .auroraFocus(
                                            state = if (tabCloseFocused) FocusState.Focused else FocusState.Idle,
                                            idleStyle = AuroraFocusStyle.Accent,
                                            focusedStyle = AuroraFocusStyle.AccentFocused
                                        )
                                        .clickable { onCloseTab(tab.id) }
                                )
                            }
                            Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                                Text(text = tab.title, style = AuroraTypography.Body, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.White)
                                Spacer(Modifier.height(2.dp))
                                Text(text = tab.domain, style = AuroraTypography.MonoLabel, color = Color.White.copy(alpha = 0.35f), fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        }
                    }
                }

                if (recentlyClosed.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "RECENTLY CLOSED",
                        style = AuroraTypography.MonoLabel,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    recentlyClosed.take(5).forEach { entry ->
                        var entryFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { entryFocused = it.isFocused }
                                .auroraFocus(
                                    state = if (entryFocused) FocusState.Focused else FocusState.Idle,
                                    idleStyle = AuroraFocusStyle.Surface,
                                    focusedStyle = AuroraFocusStyle.SurfaceFocused,
                                    shape = AuroraShapes.RoundedSm
                                )
                                .clickable { onReopenClosed(entry.url) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.title,
                                style = AuroraTypography.MonoLabel,
                                color = AuroraColors.Blue,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = entry.url,
                                style = AuroraTypography.MonoLabel,
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
