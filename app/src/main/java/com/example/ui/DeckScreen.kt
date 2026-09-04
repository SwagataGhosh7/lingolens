package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.Flashcard
import com.example.utils.TtsHelper
import com.example.viewmodel.LingoLensViewModel
import com.example.viewmodel.QuizStats

enum class DeckTab {
    DASHBOARD,
    LIST,
    QUIZ
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckScreen(viewModel: LingoLensViewModel, ttsHelper: TtsHelper) {
    val flashcards by viewModel.flashcards.collectAsState()
    val quizStats by viewModel.quizStats.collectAsState()
    var selectedTab by remember { mutableStateOf(DeckTab.DASHBOARD) }

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == DeckTab.DASHBOARD,
                onClick = { selectedTab = DeckTab.DASHBOARD },
                text = { Text("Progress") },
                icon = { Icon(Icons.Default.Insights, contentDescription = "Progress Dashboard") }
            )
            Tab(
                selected = selectedTab == DeckTab.LIST,
                onClick = { selectedTab = DeckTab.LIST },
                text = { Text("Deck (${flashcards.size})") },
                icon = { Icon(Icons.Default.ViewList, contentDescription = "Deck List") }
            )
            Tab(
                selected = selectedTab == DeckTab.QUIZ,
                onClick = { selectedTab = DeckTab.QUIZ },
                text = { Text("Quiz") },
                icon = { Icon(Icons.Default.Psychology, contentDescription = "Quiz Mode") }
            )
        }

        when (selectedTab) {
            DeckTab.DASHBOARD -> {
                DashboardView(
                    flashcards = flashcards,
                    quizStats = quizStats,
                    onNavigateToQuiz = { selectedTab = DeckTab.QUIZ },
                    onNavigateToList = { selectedTab = DeckTab.LIST },
                    onResetStats = { viewModel.resetStats() }
                )
            }
            DeckTab.LIST -> {
                if (flashcards.isEmpty()) {
                    EmptyDeckState(
                        message = "No flashcards in your deck yet",
                        description = "Use the Camera screen to capture real-world objects and translate them to build your study deck."
                    )
                } else {
                    FlashcardListView(
                        flashcards = flashcards,
                        onDelete = { viewModel.deleteFlashcard(it) },
                        ttsHelper = ttsHelper
                    )
                }
            }
            DeckTab.QUIZ -> {
                if (flashcards.isEmpty()) {
                    EmptyDeckState(
                        message = "Deck is empty",
                        description = "Add some words to your deck before taking a quiz!"
                    )
                } else {
                    QuizView(
                        flashcards = flashcards,
                        viewModel = viewModel,
                        ttsHelper = ttsHelper
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardView(
    flashcards: List<Flashcard>,
    quizStats: QuizStats,
    onNavigateToQuiz: () -> Unit,
    onNavigateToList: () -> Unit,
    onResetStats: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Quiz Statistics?") },
            text = { Text("This will reset your correct guess count, total attempts, and quiz sessions to zero. Your saved flashcards will not be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetStats()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Vocabulary Progress",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Track your object discovery and quiz mastery over time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        // Metrics 2x2 Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatMetricCard(
                title = "Words Learned",
                value = "${flashcards.size}",
                subtitle = "Total in deck",
                icon = Icons.Default.MenuBook,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Quiz Correct",
                value = "${quizStats.correctGuesses}",
                subtitle = "Items guessed",
                icon = Icons.Default.CheckCircle,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatMetricCard(
                title = "Quiz Accuracy",
                value = "${quizStats.accuracyPercentage}%",
                subtitle = "${quizStats.correctGuesses} of ${quizStats.totalGuesses} tries",
                icon = Icons.Default.TrendingUp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            StatMetricCard(
                title = "Quizzes Finished",
                value = "${quizStats.quizzesCompleted}",
                subtitle = "Completed runs",
                icon = Icons.Default.EmojiEvents,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }

        // Language Distribution Breakdown
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Language Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (flashcards.isEmpty()) {
                    Text(
                        text = "No saved cards yet. Capture objects in the Camera tab to populate languages.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val grouped = flashcards.groupBy { it.targetLanguage }
                    val total = flashcards.size.toFloat()

                    grouped.forEach { (language, list) ->
                        val count = list.size
                        val ratio = count / total
                        val percentage = (ratio * 100).toInt()

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = language,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$count words ($percentage%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToQuiz,
                modifier = Modifier.weight(1f),
                enabled = flashcards.isNotEmpty()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Quiz")
            }

            OutlinedButton(
                onClick = onNavigateToList,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ViewList, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Deck")
            }
        }

        // Reset statistics option
        if (quizStats.totalGuesses > 0 || quizStats.quizzesCompleted > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { showResetDialog = true }
                ) {
                    Icon(
                        Icons.Default.RotateLeft,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Quiz Stats",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun EmptyDeckState(
    message: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FlashcardListView(
    flashcards: List<Flashcard>,
    onDelete: (Flashcard) -> Unit,
    ttsHelper: TtsHelper
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = flashcards, key = { it.id }) { flashcard ->
            SwipeToDeleteContainer(
                item = flashcard,
                onDelete = onDelete
            ) {
                FlashcardItem(flashcard, ttsHelper)
            }
        }
    }
}

@Composable
fun QuizView(
    flashcards: List<Flashcard>,
    viewModel: LingoLensViewModel,
    ttsHelper: TtsHelper
) {
    var quizList by remember(flashcards) { mutableStateOf(flashcards) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }
    var userGuess by remember { mutableStateOf("") }
    var hasCheckedGuess by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    if (currentIndex >= quizList.size) {
        isQuizCompleted = true
    }

    if (isQuizCompleted) {
        LaunchedEffect(Unit) {
            viewModel.recordQuizCompleted()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "Quiz Completed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Score: $score of ${quizList.size} correct",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        quizList = flashcards.shuffled()
                        currentIndex = 0
                        isRevealed = false
                        userGuess = ""
                        hasCheckedGuess = false
                        isCorrect = false
                        score = 0
                        isQuizCompleted = false
                    }
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shuffle")
                }

                Button(
                    onClick = {
                        currentIndex = 0
                        isRevealed = false
                        userGuess = ""
                        hasCheckedGuess = false
                        isCorrect = false
                        score = 0
                        isQuizCompleted = false
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry")
                }
            }
        }
    } else {
        val currentCard = quizList[currentIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quiz Progress Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ${currentIndex + 1} of ${quizList.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / quizList.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Flashcard Box
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Target Language Pill
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Target: ${currentCard.targetLanguage}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentCard.englishWord,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "What is this in ${currentCard.targetLanguage}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Revealed Answer or Hidden Prompt
                    if (!isRevealed) {
                        // Guess Input Field
                        OutlinedTextField(
                            value = userGuess,
                            onValueChange = { userGuess = it },
                            label = { Text("Type your guess") },
                            placeholder = { Text("Enter ${currentCard.targetLanguage} word") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (userGuess.isNotBlank()) {
                                        val cleanGuess = userGuess.trim().lowercase()
                                        val cleanAnswer = currentCard.translatedWord.trim().lowercase()
                                        val correct = cleanGuess == cleanAnswer
                                        isCorrect = correct
                                        if (correct) score++
                                        hasCheckedGuess = true
                                        viewModel.recordQuizGuess(correct)
                                    }
                                    isRevealed = true
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (userGuess.isNotBlank()) {
                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        val cleanGuess = userGuess.trim().lowercase()
                                        val cleanAnswer = currentCard.translatedWord.trim().lowercase()
                                        val correct = cleanGuess == cleanAnswer
                                        isCorrect = correct
                                        if (correct) score++
                                        hasCheckedGuess = true
                                        viewModel.recordQuizGuess(correct)
                                        isRevealed = true
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Check Guess")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    keyboardController?.hide()
                                    isRevealed = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reveal")
                            }
                        }
                    } else {
                        // Answer revealed section
                        AnimatedVisibility(
                            visible = isRevealed,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                if (hasCheckedGuess) {
                                    Surface(
                                        color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                                contentDescription = null,
                                                tint = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isCorrect) "Correct Guess!" else "Your Guess: \"$userGuess\"",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Translation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentCard.translatedWord,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    IconButton(
                                        onClick = {
                                            ttsHelper.speak(currentCard.translatedWord, currentCard.targetLanguage)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.VolumeUp,
                                            contentDescription = "Pronounce word",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                if (!hasCheckedGuess) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Did you get it right?",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.recordQuizGuess(false)
                                                if (currentIndex < quizList.size - 1) {
                                                    currentIndex++
                                                    isRevealed = false
                                                    userGuess = ""
                                                    hasCheckedGuess = false
                                                    isCorrect = false
                                                } else {
                                                    isQuizCompleted = true
                                                }
                                            }
                                        ) {
                                            Text("Missed it")
                                        }

                                        Button(
                                            onClick = {
                                                score++
                                                viewModel.recordQuizGuess(true)
                                                if (currentIndex < quizList.size - 1) {
                                                    currentIndex++
                                                    isRevealed = false
                                                    userGuess = ""
                                                    hasCheckedGuess = false
                                                    isCorrect = false
                                                } else {
                                                    isQuizCompleted = true
                                                }
                                            }
                                        ) {
                                            Text("Got it right!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            isRevealed = false
                            userGuess = ""
                            hasCheckedGuess = false
                            isCorrect = false
                        }
                    },
                    enabled = currentIndex > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Card")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }

                Button(
                    onClick = {
                        if (currentIndex < quizList.size - 1) {
                            currentIndex++
                            isRevealed = false
                            userGuess = ""
                            hasCheckedGuess = false
                            isCorrect = false
                        } else {
                            isQuizCompleted = true
                        }
                    }
                ) {
                    Text(if (currentIndex == quizList.size - 1) "Finish" else "Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Card")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    item: Flashcard,
    onDelete: (Flashcard) -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    color = color,
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        content()
    }
}

@Composable
fun FlashcardItem(flashcard: Flashcard, ttsHelper: TtsHelper) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = flashcard.englishWord,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = flashcard.translatedWord,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = flashcard.targetLanguage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = { ttsHelper.speak(flashcard.translatedWord, flashcard.targetLanguage) }) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "Speak")
            }
        }
    }
}
