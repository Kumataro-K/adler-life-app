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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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
    selectedFeeling: String,
    isSaving: Boolean,
    onMoodChange: (Int) -> Unit,
    onEnergyChange: (Int) -> Unit,
    onTagsChange: (Set<String>) -> Unit,
    onFeelingChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "ありのままを記録しましょう。今日の気分や体調をあなたのペースで残せます。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(traceLogs, key = { it.id }) { log ->
                TraceLogRow(log)
            }
        }
        GentleCard(
            title = "今日の気分を記録する",
            subtitle = "今日の気持ちを残しましょう。フォームは縦にスクロールできます。"
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("今日あったこと（複数選択可）", style = MaterialTheme.typography.titleSmall)
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
                thumbColor = Color(0xFF2D6A4F),
                activeTrackColor = Color(0xFF52B788)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
            Text("現在: $value", style = MaterialTheme.typography.bodySmall, color = centerValueColor)
            Text("100", style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
        }
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GentleCard(
            title = "きろく",
            subtitle = "あなたのペースで大丈夫です。気分の波をゆっくり見てみましょう。"
        ) {
            TabRow(selectedTabIndex = selectedPeriod) {
                Tab(text = { Text("今週") }, selected = selectedPeriod == 0, onClick = { onSelectPeriod(0) })
                Tab(text = { Text("今月") }, selected = selectedPeriod == 1, onClick = { onSelectPeriod(1) })
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(32.dp)
                        .height(220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("100", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
                    Spacer(modifier = Modifier.weight(1f))
                    Text("50", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
                    Spacer(modifier = Modifier.weight(1f))
                    Text("0", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
                }
                MoodBarChart(
                    dataPoints = chartData,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                chartData.forEach { (date, _) ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF888888),
                        fontSize = 9.sp
                    )
                }
            }
            Text(
                text = "縦軸：気分（0〜100）　横軸：日付",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 4.dp)
            )
            RecordDaysCard(recordDays = recordDays)
            AdBanner()
        }
    }
}

@Composable
private fun MoodBarChart(
    dataPoints: List<Pair<String, Int?>>,
    modifier: Modifier = Modifier
) {
    val barColor = Color(0xFF52B788)
    val axisColor = Color(0xFF888888)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(start = 8.dp, bottom = 24.dp, end = 8.dp, top = 8.dp)
    ) {
        if (dataPoints.isEmpty()) return@Canvas

        val chartWidth = size.width
        val chartHeight = size.height
        val slotWidth = chartWidth / dataPoints.size
        val barWidth = slotWidth * 0.6f
        val gap = slotWidth * 0.4f

        drawLine(
            color = axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, chartHeight),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = axisColor,
            start = Offset(0f, chartHeight),
            end = Offset(chartWidth, chartHeight),
            strokeWidth = 2.dp.toPx()
        )

        listOf(0, 50, 100).forEach { value ->
            val y = chartHeight - (value / 100f * chartHeight)
            drawLine(
                color = axisColor.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        dataPoints.forEachIndexed { index, (_, value) ->
            if (value != null) {
                val barHeight = value / 100f * chartHeight
                val left = index * (barWidth + gap) + gap / 2
                val top = chartHeight - barHeight
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun RecordDaysCard(recordDays: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "📅", fontSize = 28.sp)
            Text(
                text = "${recordDays}日",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2D6A4F)
            )
            Text(
                text = "記録日数",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun CalendarScreen(
    month: YearMonth,
    summaries: List<TraceDaySummary>,
    thisMonthLogs: List<TraceLog>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val summaryMap = summaries.associateBy { it.date }
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOffset = firstDay.dayOfWeek.value % 7
    val avgMood = thisMonthLogs.takeIf { it.isNotEmpty() }?.map { it.mood }?.average()?.toInt()
    val avgEnergy = thisMonthLogs.takeIf { it.isNotEmpty() }?.map { it.energy }?.average()?.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GentleCard(
            title = "気分の暦",
            subtitle = "あなたのペースで大丈夫です。色でゆるやかな流れを見られます。"
        ) {
            LegendCard()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Text("‹", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = month.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.JAPAN)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNextMonth) {
                    Text("›", style = MaterialTheme.typography.titleLarge)
                }
            }
            CalendarWeekHeader()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var dayNumber = 1
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayIndex in 0 until 7) {
                            val cellIndex = week * 7 + dayIndex
                            if (cellIndex < firstDayOffset || dayNumber > daysInMonth) {
                                CalendarDayCell(
                                    dayLabel = "",
                                    color = moodToColor(null),
                                    isCurrentMonth = false
                                ) {}
                            } else {
                                val date = month.atDay(dayNumber)
                                val summary = summaryMap[date]
                                CalendarDayCell(
                                    dayLabel = dayNumber.toString(),
                                    color = moodToColor(summary?.averageMood),
                                    isCurrentMonth = true,
                                    onClick = { onSelectDate(date) }
                                )
                                dayNumber++
                            }
                        }
                    }
                }
            }
            MonthSummaryCard(avgMood = avgMood, avgEnergy = avgEnergy)
        }
    }
}

@Composable
private fun LegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "気分の色ガイド",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF2D6A4F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = Color(0xFFEF9A9A), label = "つらい\n(0〜33)")
                LegendItem(color = Color(0xFFFFE082), label = "ふつう\n(34〜66)")
                LegendItem(color = Color(0xFF81C784), label = "よい\n(67〜100)")
                LegendItem(color = Color(0xFFE0E0E0), label = "記録\nなし")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color = color, shape = RoundedCornerShape(6.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun MonthSummaryCard(avgMood: Int?, avgEnergy: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "今月のまとめ",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF2D6A4F)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MonthStatItem(
                    icon = "😊",
                    label = "平均気分",
                    value = avgMood?.let { "$it / 100" } ?: "記録なし"
                )
                MonthStatItem(
                    icon = "⚡",
                    label = "平均エネルギー",
                    value = avgEnergy?.let { "$it / 100" } ?: "記録なし"
                )
            }
        }
    }
}

@Composable
private fun MonthStatItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF2D6A4F)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666)
        )
    }
}

private fun moodToColor(mood: Int?): Color {
    return when {
        mood == null -> Color(0xFFE0E0E0)
        mood <= 33 -> Color(0xFFEF9A9A)
        mood <= 66 -> Color(0xFFFFE082)
        else -> Color(0xFF81C784)
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
        GentleCard(
            title = "設定",
            subtitle = "記録はあなたの味方です。必要な情報をここにまとめています。"
        ) {
            SettingsRow(title = "免責事項", subtitle = "セルフケアアプリとしての位置づけを確認") { onShowDisclaimer() }
            SettingsRow(title = "プライバシーポリシー", subtitle = "データ保存と広告配信について") { onShowPrivacy() }
            SettingsRow(title = "記録をエクスポート 📤", subtitle = "共有シートでメモやメールへ送る") { onExport() }
            Text(
                text = "バージョン情報  $versionName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
        GentleCard(
            title = "ご利用の前に",
            subtitle = "今日の気持ちを残しましょう。いつでも全文を確認できます。"
        ) {
            Text(
                text = DISCLAIMER_TEXT,
                modifier = Modifier.clickable { onShowDisclaimer() },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
    }
}

@Composable
private fun LegalDocumentDialog(title: String, text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

@Composable
private fun CalendarWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("日", "月", "火", "水", "木", "金", "土").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = when (label) {
                    "日" -> Color(0xFFE57373)
                    "土" -> Color(0xFF64B5F6)
                    else -> Color(0xFF666666)
                }
            )
        }
    }
}

@Composable
private fun DayLogSheet(date: LocalDate, logs: List<TraceLog>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPAN)),
            style = MaterialTheme.typography.titleLarge
        )
        if (logs.isEmpty()) {
            Text("この日の記録はまだありません。")
        } else {
            LazyColumn(
                modifier = Modifier.height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    TraceLogRow(log)
                }
            }
        }
    }
}
