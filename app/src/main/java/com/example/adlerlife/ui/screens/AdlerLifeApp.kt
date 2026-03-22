package com.example.adlerlife.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adlerlife.BuildConfig
import com.example.adlerlife.data.model.TraceDaySummary
import com.example.adlerlife.data.model.TraceLog
import com.example.adlerlife.ui.components.AdBanner
import com.example.adlerlife.ui.components.CalendarDayCell
import com.example.adlerlife.ui.components.GentleCard
import com.example.adlerlife.ui.components.TraceLogRow
import com.example.adlerlife.util.DISCLAIMER_TEXT
import com.example.adlerlife.util.PRIVACY_POLICY_TEXT
import com.example.adlerlife.util.exportLogs
import com.example.adlerlife.util.shareText
import com.example.adlerlife.viewmodel.AdlerViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val feelingOptions = listOf(
    "😊 嬉しい",
    "😌 穏やか",
    "😐 ふつう",
    "😟 不安",
    "😔 悲しい",
    "😤 イライラ",
    "😴 疲れた",
    "😰 プレッシャー",
    "🥺 寂しい",
    "😤 もやもや",
    "🤗 感謝",
    "😶 無気力"
)

private val tagOptions = listOf(
    "💼 仕事",
    "📚 勉強",
    "👥 人間関係",
    "🏃 運動",
    "🍽️ 食事",
    "😴 睡眠",
    "🏥 体調",
    "🎮 趣味",
    "🏠 家事",
    "🌿 休息",
    "🚶 外出",
    "💊 通院",
    "👨‍👩‍👧 家族",
    "❤️ 恋愛",
    "💰 お金",
    "🌤️ 天気の影響"
)

private enum class Destination(
    val label: String,
    val icon: @Composable () -> Unit
) {
    TRACE("軌跡", { Icon(Icons.Outlined.Spa, contentDescription = null) }),
    RECORDS("きろく", { Icon(Icons.Outlined.Park, contentDescription = null) }),
    CALENDAR("暦", { Icon(Icons.Outlined.Park, contentDescription = null) }),
    SETTINGS("設定", { Icon(Icons.Outlined.Settings, contentDescription = null) })
}

private enum class LegalDocument { DISCLAIMER, PRIVACY }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdlerLifeApp(
    viewModel: AdlerViewModel,
    showDisclaimerInitially: Boolean,
    onDisclaimerAccepted: () -> Unit,
    onTraceSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val traceLogs by viewModel.traceLogs.collectAsStateWithLifecycle()
    val monthSummaries by viewModel.monthSummaries.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedRecordPeriod.collectAsStateWithLifecycle()
    val recordLogs by viewModel.recordLogs.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val thisMonthLogs by viewModel.thisMonthLogs.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(Destination.TRACE) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showInitialDisclaimer by rememberSaveable { mutableStateOf(showDisclaimerInitially) }
    var openDocument by remember { mutableStateOf<LegalDocument?>(null) }
    val context = LocalContext.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    LaunchedEffect(showDisclaimerInitially) {
        if (showDisclaimerInitially) showInitialDisclaimer = true
    }

    selectedDate?.let { date ->
        ModalBottomSheet(onDismissRequest = { selectedDate = null }) {
            DayLogSheet(date = date, logs = viewModel.logsForDate(date))
        }
    }

    openDocument?.let { document ->
        LegalDocumentDialog(
            title = if (document == LegalDocument.DISCLAIMER) "ご利用の前に" else "プライバシーポリシー",
            text = if (document == LegalDocument.DISCLAIMER) DISCLAIMER_TEXT else PRIVACY_POLICY_TEXT,
            onDismiss = { openDocument = null }
        )
    }

    if (showInitialDisclaimer) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("ご利用の前に") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(DISCLAIMER_TEXT)
                    TextButton(onClick = { openDocument = LegalDocument.PRIVACY }) {
                        Text("プライバシーポリシーを見る")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInitialDisclaimer = false
                        onDisclaimerAccepted()
                    }
                ) {
                    Text("同意して始める 🌿")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Park,
                            contentDescription = null,
                            tint = Color(0xFF2D6A4F),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Forest Mood",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF2D6A4F)
                            )
                            Text(
                                text = "毎日の気分に寄り添う",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEach { item ->
                    NavigationBarItem(
                        selected = destination == item,
                        onClick = { destination = item },
                        icon = item.icon,
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isDark) {
                        Brush.verticalGradient(listOf(Color.Black, Color.Black))
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE8F5E9),
                                Color(0xFFF1F8E9),
                                Color(0xFFF8FBF8)
                            )
                        )
                    }
                )
                .padding(padding)
        ) {
            when (destination) {
                Destination.TRACE -> TraceScreen(
                    traceLogs = traceLogs,
                    mood = uiState.traceInput.mood,
                    energy = uiState.traceInput.energy,
                    selectedTags = uiState.traceInput.tags,
                    selectedFeeling = uiState.traceInput.feeling,
                    isSaving = uiState.isSavingTrace,
                    onMoodChange = viewModel::updateMood,
                    onEnergyChange = viewModel::updateEnergy,
                    onTagsChange = viewModel::updateTags,
                    onFeelingChange = viewModel::updateFeeling,
                    onSave = { viewModel.saveTrace(onTraceSaved) }
                )

                Destination.RECORDS -> RecordsScreen(
                    selectedPeriod = selectedPeriod,
                    onSelectPeriod = viewModel::selectRecordPeriod,
                    recordDays = viewModel.countRecordedDays(recordLogs),
                    chartData = chartData
                )

                Destination.CALENDAR -> CalendarScreen(
                    month = uiState.selectedMonth,
                    summaries = monthSummaries,
                    thisMonthLogs = thisMonthLogs,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onSelectDate = { selectedDate = it }
                )

                Destination.SETTINGS -> SettingsScreen(
                    onShowDisclaimer = { openDocument = LegalDocument.DISCLAIMER },
                    onShowPrivacy = { openDocument = LegalDocument.PRIVACY },
                    onExport = { shareText(context, exportLogs(traceLogs)) },
                    versionName = BuildConfig.VERSION_NAME
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TraceScreen(
    traceLogs: List<TraceLog>,
    mood: Int,
    energy: Int,
    selectedTags: Set<String>,
    selectedFeeling: String?,
    isSaving: Boolean,
    onMoodChange: (Int) -> Unit,
    onEnergyChange: (Int) -> Unit,
    onTagsChange: (Set<String>) -> Unit,
    onFeelingChange: (String) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            GentleCard(title = "今日のきろく") {
                Text("今日の活動タグ", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagOptions.forEach { tag ->
                        FilterChip(
                            selected = tag in selectedTags,
                            onClick = {
                                onTagsChange(
                                    if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                                )
                            },
                            label = { Text(tag, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF52B788),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text("今の気持ちをひとこと", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    feelingOptions.forEach { option ->
                        FilterChip(
                            selected = selectedFeeling == option,
                            onClick = { onFeelingChange(option) },
                            label = { Text(option, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2D6A4F),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                ScoreSlider(
                    title = "今日の気分",
                    value = mood,
                    guidance = when (mood) {
                        in 0..20 -> "😔 とてもつらい"
                        in 21..40 -> "😟 少しつらい"
                        in 41..60 -> "😐 ふつう"
                        in 61..80 -> "🙂 まあまあ良い"
                        else -> "😊 とても良い"
                    },
                    centerValueColor = Color(0xFF2D6A4F),
                    onValueChange = onMoodChange
                )
                ScoreSlider(
                    title = "身体のエネルギー",
                    value = energy,
                    guidance = when (energy) {
                        in 0..20 -> "🪫 ほとんど動けない"
                        in 21..40 -> "😴 疲れ気味"
                        in 41..60 -> "😐 普通に動ける"
                        in 61..80 -> "⚡ 元気がある"
                        else -> "🔥 とても活力がある"
                    },
                    centerValueColor = Color(0xFF888888),
                    onValueChange = onEnergyChange
                )
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("今日の気持ちを残す 🌿")
                    }
                }
                AdBanner()
            }
        }
        item {
            Text(
                "これまでの軌跡",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2D6A4F),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(traceLogs) { log ->
            TraceLogRow(log = log)
        }
    }
}

@Composable
private fun ScoreSlider(
    title: String,
    value: Int,
    guidance: String,
    centerValueColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = guidance,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2D6A4F)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF52B788),
                activeTrackColor = Color(0xFF52B788),
                inactiveTrackColor = Color(0xFFD8F3DC)
            )
        )
    }
}

@Composable
private fun RecordsScreen(
    selectedPeriod: Int,
    onSelectPeriod: (Int) -> Unit,
    recordDays: Int,
    chartData: List<Pair<String, Int?>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GentleCard(title = "継続の記録") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("記録した日数", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "$recordDays 日",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF1B4332),
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Park,
                    contentDescription = null,
                    tint = Color(0xFFB7E4C7),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        TabRow(
            selectedTabIndex = if (selectedPeriod == 7) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF2D6A4F),
            divider = {}
        ) {
            Tab(selected = selectedPeriod == 7, onClick = { onSelectPeriod(7) }) {
                Text("7日間", modifier = Modifier.padding(vertical = 12.dp))
            }
            Tab(selected = selectedPeriod == 30, onClick = { onSelectPeriod(30) }) {
                Text("30日間", modifier = Modifier.padding(vertical = 12.dp))
            }
        }

        GentleCard(title = "気分の推移") {
            Spacer(modifier = Modifier.height(16.dp))
            if (chartData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("データがありません", color = Color.Gray)
                }
            } else {
                SimpleLineChart(data = chartData)
            }
        }
        AdBanner()
    }
}

@Composable
private fun SimpleLineChart(data: List<Pair<String, Int?>>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val maxVal = 100f
        
        val validPoints = data.mapIndexedNotNull { index, pair ->
            pair.second?.let { value ->
                val stepX = width / (data.size - 1).coerceAtLeast(1)
                Offset(
                    x = index * stepX,
                    y = height - (value.toFloat() / maxVal * height)
                )
            }
        }

        // Draw grid lines
        for (i in 0..4) {
            val y = height - (i * height / 4)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line
        for (i in 0 until validPoints.size - 1) {
            drawLine(
                color = Color(0xFF52B788),
                start = validPoints[i],
                end = validPoints[i + 1],
                strokeWidth = 3.dp.toPx()
            )
        }

        // Draw dots
        validPoints.forEach { point ->
            drawCircle(
                color = Color(0xFF2D6A4F),
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarScreen(
    month: YearMonth,
    summaries: List<TraceDaySummary>,
    thisMonthLogs: List<TraceLog>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val summaryMap = remember(summaries) { summaries.associateBy { it.date } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GentleCard(title = month.format(DateTimeFormatter.ofPattern("yyyy年 MM月", Locale.JAPANESE))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.Park, contentDescription = "前月", modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.Park, contentDescription = "次月", modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val daysInMonth = month.lengthOfMonth()
            val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7 // 0=Sun, 1=Mon...
            
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("日", "月", "火", "水", "木", "金", "土").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = 7
            ) {
                repeat(firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(44.dp))
                }

                for (day in 1..daysInMonth) {
                    val date = month.atDay(day)
                    val summary = summaryMap[date]
                    CalendarDayCell(
                        dayLabel = day.toString(),
                        color = summary?.color ?: Color.Transparent,
                        isCurrentMonth = true,
                        onClick = { onSelectDate(date) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("今月のログ", style = MaterialTheme.typography.titleSmall, color = Color(0xFF2D6A4F))
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(thisMonthLogs) { log ->
                TraceLogRow(log = log)
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    onShowDisclaimer: () -> Unit,
    onShowPrivacy: () -> Unit,
    onExport: () -> Unit,
    versionName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("アプリの設定", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2D6A4F))

        GentleCard(title = "一般") {
            SettingsItem(title = "データをエクスポート", onClick = onExport)
            SettingsItem(title = "ご利用の前に", onClick = onShowDisclaimer)
            SettingsItem(title = "プライバシーポリシー", onClick = onShowPrivacy)
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Version $versionName",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun SettingsItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Icon(Icons.Default.Park, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
    }
}

@Composable
private fun DayLogSheet(date: LocalDate, logs: List<TraceLog>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 500.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (logs.isEmpty()) {
            Text("この日の記録はありません。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs) { log ->
                    TraceLogRow(log = log)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun LegalDocumentDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}
