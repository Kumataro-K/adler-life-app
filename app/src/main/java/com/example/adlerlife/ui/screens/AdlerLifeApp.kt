package com.example.adlerlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.adlerlife.ui.components.ActionLogRow
import com.example.adlerlife.ui.components.CategoryChips
import com.example.adlerlife.ui.components.GentleCard
import com.example.adlerlife.ui.components.TrajectoryRow
import com.example.adlerlife.viewmodel.AdlerViewModel

private enum class Destination(
    val label: String,
    val icon: @Composable () -> Unit
) {
    NOW("いま", { Icon(Icons.Outlined.Waves, contentDescription = null) }),
    LOG("軌跡", { Icon(Icons.Outlined.SelfImprovement, contentDescription = null) }),
    REFLECT("夜", { Icon(Icons.Outlined.NightsStay, contentDescription = null) }),
    COACH("対話", { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) }),
    MAP("暦", { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdlerLifeApp(viewModel: AdlerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionLogs by viewModel.actionLogs.collectAsStateWithLifecycle()
    val trajectories by viewModel.trajectories.collectAsStateWithLifecycle()
    val coaching by viewModel.coaching.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf(Destination.NOW) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Adler Life")
                        Text(
                            text = "日々を踊るように味わう",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (destination) {
                Destination.NOW -> item {
                    NowScreen(
                        desire = uiState.impulseInput.desire,
                        mood = uiState.impulseInput.mood.toFloat(),
                        energy = uiState.impulseInput.energyLevel.toFloat(),
                        suggestion = uiState.currentSuggestion,
                        isLoading = uiState.isLoadingSuggestion,
                        onDesireChange = { viewModel.updateDesire(it) },
                        onMoodChange = { viewModel.updateMood(it) },
                        onEnergyChange = { viewModel.updateEnergy(it) },
                        onSuggest = { viewModel.requestSuggestion() }
                    )
                }

                Destination.LOG -> {
                    item {
                        LogComposer(
                            action = uiState.actionDraft,
                            feeling = uiState.feelingDraft,
                            onActionChange = { viewModel.updateActionDraft(it) },
                            onFeelingChange = { viewModel.updateFeelingDraft(it) },
                            onSave = { viewModel.saveActionLog() },
                            onCategoryChange = { viewModel.updateCategory(it) },
                            selectedCategory = uiState.selectedCategory
                        )
                    }
                    items(actionLogs) { log -> ActionLogRow(log) }
                }

                Destination.REFLECT -> item {
                    ReflectionScreen(
                        actions = uiState.reflectionInput.actionsSummary,
                        moment = uiState.reflectionInput.memorableMoment,
                        joy = uiState.reflectionInput.smallJoy,
                        insight = uiState.reflectionInsight,
                        isLoading = uiState.isLoadingReflection,
                        onActionsChange = { viewModel.updateReflectionActions(it) },
                        onMomentChange = { viewModel.updateReflectionMoment(it) },
                        onJoyChange = { viewModel.updateReflectionJoy(it) },
                        onSubmit = { viewModel.submitReflection() }
                    )
                }

                Destination.COACH -> item {
                    CoachScreen(summary = coaching.summary, prompt = coaching.prompt)
                }

                Destination.MAP -> {
                    item {
                        GentleCard(
                            title = "目標ではなく、軌跡を見る",
                            subtitle = "色は行動の種類だけを静かに示します。達成率は出しません。"
                        ) {}
                    }
                    items(trajectories) { item -> TrajectoryRow(item) }
                }
            }
        }
    }
}

@Composable
private fun NowScreen(
    desire: String,
    mood: Float,
    energy: Float,
    suggestion: String,
    isLoading: Boolean,
    onDesireChange: (String) -> Unit,
    onMoodChange: (Float) -> Unit,
    onEnergyChange: (Float) -> Unit,
    onSuggest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GentleCard(
            title = "今日の衝動ログ",
            subtitle = "“やるべき”ではなく、“いま惹かれるもの”を置いてみます。"
        ) {
            OutlinedTextField(
                value = desire,
                onValueChange = onDesireChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("今やりたいこと") },
                placeholder = { Text("たとえば、少し歩きたい / 誰かと話したい") }
            )
            Text("今の気分 ${mood.toInt()}")
            Slider(value = mood, onValueChange = onMoodChange, valueRange = 0f..100f)
            Text("エネルギー ${energy.toInt()}")
            Slider(value = energy, onValueChange = onEnergyChange, valueRange = 0f..100f)
            Button(onClick = onSuggest) {
                Text("小さな行動を受け取る")
            }
        }
        GentleCard(
            title = "AIからのそっとした提案",
            subtitle = "正解ではなく、今の自分に試せそうなひとつ。"
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = suggestion, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun LogComposer(
    action: String,
    feeling: String,
    selectedCategory: com.example.adlerlife.data.model.ActionCategory,
    onActionChange: (String) -> Unit,
    onFeelingChange: (String) -> Unit,
    onCategoryChange: (com.example.adlerlife.data.model.ActionCategory) -> Unit,
    onSave: () -> Unit
) {
    GentleCard(
        title = "行動の記録",
        subtitle = "できたかどうかではなく、何をして、どう感じたかだけを残します。"
    ) {
        OutlinedTextField(
            value = action,
            onValueChange = onActionChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("何をしたか") }
        )
        OutlinedTextField(
            value = feeling,
            onValueChange = onFeelingChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("どう感じたか") }
        )
        CategoryChips(selected = selectedCategory, onSelect = onCategoryChange)
        Button(onClick = onSave) {
            Text("そっと残す")
        }
    }
}

@Composable
private fun ReflectionScreen(
    actions: String,
    moment: String,
    joy: String,
    insight: com.example.adlerlife.data.model.CoachingInsight,
    isLoading: Boolean,
    onActionsChange: (String) -> Unit,
    onMomentChange: (String) -> Unit,
    onJoyChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GentleCard(
            title = "一日の振り返り",
            subtitle = "夜に、意味づけを静かに見つけます。評価はしません。"
        ) {
            OutlinedTextField(
                value = actions,
                onValueChange = onActionsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("今日やったこと") }
            )
            OutlinedTextField(
                value = moment,
                onValueChange = onMomentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("印象に残った瞬間") }
            )
            OutlinedTextField(
                value = joy,
                onValueChange = onJoyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("小さな喜び") }
            )
            Button(onClick = onSubmit) {
                Text("意味を受け取る")
            }
        }
        GentleCard(
            title = "価値観のフィードバック",
            subtitle = "“あなたらしさ”を決めつけず、仮説として返します。"
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = insight.summary)
                Text(text = insight.prompt, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CoachScreen(summary: String, prompt: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GentleCard(
            title = "AIコーチング",
            subtitle = "指示ではなく問いかけで、自分の興味や傾向に気づくための時間です。"
        ) {
            Text(text = summary)
            Text(text = prompt, color = MaterialTheme.colorScheme.primary)
        }
        GentleCard(
            title = "通知の姿勢",
            subtitle = "アプリ内でも“急かさない”ことを明示します。"
        ) {
            Text("通知文例: 「もし余白があれば、今日の小さな喜びを1つだけ置いてみませんか。」")
        }
    }
}
