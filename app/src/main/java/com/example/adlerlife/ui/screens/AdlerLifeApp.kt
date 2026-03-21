package com.example.adlerlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adlerlife.data.model.TraceDaySummary
import com.example.adlerlife.data.model.TraceLog
import com.example.adlerlife.ui.components.CalendarDayCell
import com.example.adlerlife.ui.components.ChatBubble
import com.example.adlerlife.ui.components.GentleCard
import com.example.adlerlife.ui.components.MetricPill
import com.example.adlerlife.ui.components.TraceLogRow
import com.example.adlerlife.viewmodel.AdlerViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class Destination(
    val label: String,
    val icon: @Composable () -> Unit
) {
    LOG("軌跡", { Icon(Icons.Outlined.SelfImprovement, contentDescription = null) }),
    CHAT("対話", { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) }),
    CALENDAR("暦", { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdlerLifeApp(viewModel: AdlerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val traceLogs by viewModel.traceLogs.collectAsStateWithLifecycle()
    val monthSummaries by viewModel.monthSummaries.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf(Destination.LOG) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    selectedDate?.let { date ->
        ModalBottomSheet(onDismissRequest = { selectedDate = null }) {
            DayLogSheet(
                date = date,
                logs = viewModel.logsForDate(date)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Adler Life")
                        Text(
                            text = "比較ではなく、今日の手ざわりを残す",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
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
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when (destination) {
                Destination.LOG -> TraceScreen(
                    traceLogs = traceLogs,
                    what = uiState.traceInput.what,
                    howFelt = uiState.traceInput.howFelt,
                    mood = uiState.traceInput.mood,
                    energy = uiState.traceInput.energy,
                    isSaving = uiState.isSavingTrace,
                    onWhatChange = viewModel::updateWhat,
                    onHowFeltChange = viewModel::updateHowFelt,
                    onMoodChange = viewModel::updateMood,
                    onEnergyChange = viewModel::updateEnergy,
                    onSave = viewModel::saveTrace
                )

                Destination.CHAT -> ChatScreen(
                    messages = uiState.chatMessages,
                    aiReply = uiState.aiReply,
                    draft = uiState.chatDraft,
                    isLoading = uiState.isLoadingConversation,
                    onReflectToday = viewModel::reflectToday,
                    onDraftChange = viewModel::updateChatDraft,
                    onSend = viewModel::sendChatMessage
                )

                Destination.CALENDAR -> CalendarScreen(
                    month = uiState.selectedMonth,
                    summaries = monthSummaries,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onSelectDate = { selectedDate = it }
                )
            }
        }
    }
}

@Composable
private fun TraceScreen(
    traceLogs: List<TraceLog>,
    what: String,
    howFelt: String,
    mood: Float,
    energy: Float,
    isSaving: Boolean,
    onWhatChange: (String) -> Unit,
    onHowFeltChange: (String) -> Unit,
    onMoodChange: (Float) -> Unit,
    onEnergyChange: (Float) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "記録はいつでも、何度でも。できたかどうかではなく、その瞬間の手ざわりを置いておきます。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(traceLogs, key = { it.id }) { log ->
                TraceLogRow(log)
            }
        }
        GentleCard(
            title = "新しい軌跡を残す",
            subtitle = "フォームは閉じずに置いておき、必要なときにだけ触れられるようにします。"
        ) {
            OutlinedTextField(
                value = what,
                onValueChange = onWhatChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("何をしたか") }
            )
            OutlinedTextField(
                value = howFelt,
                onValueChange = onHowFeltChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("どう感じたか") }
            )
            MoodSlider(
                title = "今の気分",
                leftLabel = "😔",
                rightLabel = "😊",
                value = mood,
                onValueChange = onMoodChange
            )
            MoodSlider(
                title = "エネルギーレベル",
                leftLabel = "低",
                rightLabel = "高",
                value = energy,
                onValueChange = onEnergyChange
            )
            Button(onClick = onSave, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("保存する")
                }
            }
        }
    }
}

@Composable
private fun MoodSlider(
    title: String,
    leftLabel: String,
    rightLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$title ${"%.2f".format(value)}", style = MaterialTheme.typography.bodyMedium)
        Slider(value = value, onValueChange = onValueChange, valueRange = 0f..1f)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(leftLabel, style = MaterialTheme.typography.labelLarge)
            Text(rightLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ChatScreen(
    messages: List<com.example.adlerlife.data.model.ChatMessage>,
    aiReply: String,
    draft: String,
    isLoading: Boolean,
    onReflectToday: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GentleCard(
            title = "対話",
            subtitle = "評価ではなく問いかけで、今日の軌跡にもう一度ふれてみます。"
        ) {
            Button(onClick = onReflectToday) {
                Text("今日の軌跡を振り返る")
            }
            Text(text = aiReply, color = MaterialTheme.colorScheme.primary)
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(message)
            }
        }
        GentleCard(
            title = "追加入力",
            subtitle = "会話履歴はその日限りで、アプリを閉じると残しません。"
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("今の言葉を置く") }
            )
            Button(onClick = onSend, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("送信")
                }
            }
        }
    }
}

@Composable
private fun CalendarScreen(
    month: YearMonth,
    summaries: List<TraceDaySummary>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val summaryMap = summaries.associateBy { it.date }
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDayOffset = (firstDay.dayOfWeek.value % 7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GentleCard(
            title = "暦",
            subtitle = "色はその日の気分平均だけを示し、達成率や判定は表示しません。"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "前の月")
                }
                Text(
                    text = month.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.JAPAN)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "次の月")
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
                                CalendarDayCell(dayLabel = "", color = MaterialTheme.colorScheme.background, isCurrentMonth = false) {}
                            } else {
                                val date = month.atDay(dayNumber)
                                val summary = summaryMap[date]
                                CalendarDayCell(
                                    dayLabel = dayNumber.toString(),
                                    color = summary?.color ?: androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                                    isCurrentMonth = true,
                                    onClick = { onSelectDate(date) }
                                )
                                dayNumber++
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("日", "月", "火", "水", "木", "金", "土").forEach { label ->
            Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
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
