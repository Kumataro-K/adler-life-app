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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
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
import com.example.adlerlife.data.model.ChatMessage
import com.example.adlerlife.data.model.TraceDaySummary
import com.example.adlerlife.data.model.TraceLog
import com.example.adlerlife.ui.components.AdBanner
import com.example.adlerlife.ui.components.CalendarDayCell
import com.example.adlerlife.ui.components.ChatBubble
import com.example.adlerlife.ui.components.GentleCard
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
    TRACE("軌跡", { Icon(Icons.Outlined.Spa, contentDescription = null) }),
    CHAT("対話", { Icon(Icons.Outlined.SelfImprovement, contentDescription = null) }),
    CALENDAR("暦", { Icon(Icons.Outlined.Park, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdlerLifeApp(viewModel: AdlerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val traceLogs by viewModel.traceLogs.collectAsStateWithLifecycle()
    val monthSummaries by viewModel.monthSummaries.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf(Destination.TRACE) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    selectedDate?.let { date ->
        ModalBottomSheet(onDismissRequest = { selectedDate = null }) {
            DayLogSheet(date = date, logs = viewModel.logsForDate(date))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Forest Mood", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "今日どんな気分だった？ を静かに残す",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
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
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when (destination) {
                Destination.TRACE -> TraceScreen(
                    traceLogs = traceLogs,
                    mood = uiState.traceInput.mood,
                    energy = uiState.traceInput.energy,
                    whatHappened = uiState.traceInput.whatHappened,
                    feeling = uiState.traceInput.feeling,
                    isSaving = uiState.isSavingTrace,
                    onMoodChange = viewModel::updateMood,
                    onEnergyChange = viewModel::updateEnergy,
                    onWhatHappenedChange = viewModel::updateWhatHappened,
                    onFeelingChange = viewModel::updateFeeling,
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
    mood: Float,
    energy: Float,
    whatHappened: String,
    feeling: String,
    isSaving: Boolean,
    onMoodChange: (Float) -> Unit,
    onEnergyChange: (Float) -> Unit,
    onWhatHappenedChange: (String) -> Unit,
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
            text = "よい・わるいを決めずに、今日の気分や体調を森に置くように記録します。",
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
            title = "今日の記録",
            subtitle = "フォームは縦にスクロールできるので、ボタンまでやさしく辿れます。"
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ForestSlider(
                    title = "今日の気分",
                    leftLabel = "😔 つらい",
                    rightLabel = "😊 よい",
                    value = mood,
                    onValueChange = onMoodChange
                )
                ForestSlider(
                    title = "体のエネルギー",
                    leftLabel = "🪫 低い",
                    rightLabel = "⚡ 高い",
                    value = energy,
                    onValueChange = onEnergyChange
                )
                OutlinedTextField(
                    value = whatHappened,
                    onValueChange = onWhatHappenedChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("今日あったこと") },
                    placeholder = { Text("今日どんなことがありましたか？") },
                    minLines = 3,
                    maxLines = 3
                )
                OutlinedTextField(
                    value = feeling,
                    onValueChange = onFeelingChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("今の気持ちをひとこと") },
                    placeholder = { Text("気持ちを自由に書いてください") },
                    minLines = 3,
                    maxLines = 3
                )
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("記録する 🌿")
                    }
                }
                AdBanner()
            }
        }
    }
}

@Composable
private fun ForestSlider(
    title: String,
    leftLabel: String,
    rightLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$title ${"%.2f".format(value)}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(leftLabel, style = MaterialTheme.typography.labelLarge)
            Text(rightLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ChatScreen(
    messages: List<ChatMessage>,
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
            title = "森と対話するように振り返る",
            subtitle = "共感と問いかけだけを返し、評価や採点はしません。"
        ) {
            Button(onClick = onReflectToday) {
                Text("今日の記録を振り返る")
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
            title = "今のことばを続ける",
            subtitle = "会話はその日限り。明日になればまた新しい森から始まります。"
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("今の気持ち") },
                placeholder = { Text("たとえば、胸がつまる感じ / 少し軽くなった など") }
            )
            Button(onClick = onSend, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("送信する")
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
    val firstDayOffset = firstDay.dayOfWeek.value % 7

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GentleCard(
            title = "気分の暦",
            subtitle = "色はその日の気分平均だけを静かに映し、比較はしません。"
        ) {
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
            AdBanner()
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
            Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
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
