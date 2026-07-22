@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.braincamp.salarypusher.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.braincamp.salarypusher.domain.model.CoinDenomination
import com.braincamp.salarypusher.ui.components.PrimaryButton
import com.braincamp.salarypusher.ui.theme.SalaryPusherTheme
import com.braincamp.salarypusher.util.annualCentsToHourlyCents
import com.braincamp.salarypusher.util.formatCents
import java.time.DayOfWeek

// ─────────────────────────────────────────────────────────────────────────────
// Task 4.2 — Welcome Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingWelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🪙",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Salary Pusher",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your salary. Made tangible.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        // How it works
        HowItWorksItem("💼", "Enter your salary and work schedule")
        HowItWorksItem("⏰", "Coins drop into the machine as you earn them")
        HowItWorksItem("🎰", "Tap to drop coins through the pegs and push them off the edge")

        Spacer(modifier = Modifier.height(48.dp))
        PrimaryButton(text = "Get Started", onClick = onGetStarted)
    }
}

@Composable
private fun HowItWorksItem(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Task 4.3 — Salary Input Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingSalaryScreen(onNext: (hourlySalaryCents: Long) -> Unit) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) } // 0=Hourly, 1=Annual
    var inputText by rememberSaveable { mutableStateOf("") }

    val hourlyCents: Long = remember(tabIndex, inputText) {
        val raw = inputText.toLongOrNull() ?: 0L
        val rawCents = raw * 100L
        if (tabIndex == 0) rawCents else annualCentsToHourlyCents(rawCents)
    }
    val isValid = hourlyCents > 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("What do you earn?", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We'll use this to calculate your coin drop rate.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0; inputText = "" }) {
                Text("Hourly", modifier = Modifier.padding(vertical = 12.dp))
            }
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1; inputText = "" }) {
                Text("Annual", modifier = Modifier.padding(vertical = 12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it.filter { c -> c.isDigit() } },
            label = { Text(if (tabIndex == 0) "Hourly rate (dollars)" else "Annual salary (dollars)") },
            prefix = { Text("$") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live preview of hourly rate
        if (isValid) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hourly rate", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = formatCents(hourlyCents),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            text = "Next",
            onClick = { onNext(hourlyCents) },
            enabled = isValid
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Task 4.4 — Work Schedule Screen
// ─────────────────────────────────────────────────────────────────────────────

private val ALL_DAYS = listOf(
    DayOfWeek.MONDAY    to "Mon",
    DayOfWeek.TUESDAY   to "Tue",
    DayOfWeek.WEDNESDAY to "Wed",
    DayOfWeek.THURSDAY  to "Thu",
    DayOfWeek.FRIDAY    to "Fri",
    DayOfWeek.SATURDAY  to "Sat",
    DayOfWeek.SUNDAY    to "Sun"
)

@Composable
fun OnboardingScheduleScreen(
    onNext: (workDays: Set<DayOfWeek>, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit
) {
    var selectedDays by rememberSaveable {
        mutableStateOf(setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        ))
    }

    val startState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)
    val endState   = rememberTimePickerState(initialHour = 17, initialMinute = 0, is24Hour = false)

    val isValid = selectedDays.isNotEmpty() &&
        (endState.hour * 60 + endState.minute) > (startState.hour * 60 + startState.minute)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("When do you work?", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Coins only drop during your shift.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Day picker chips
        Text("Work days", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ALL_DAYS.forEach { (day, label) ->
                FilterChip(
                    selected = day in selectedDays,
                    onClick = {
                        selectedDays = if (day in selectedDays) selectedDays - day
                        else selectedDays + day
                    },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Shift start time picker
        Text("Shift start", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(12.dp))
        TimePicker(
            state = startState,
            colors = TimePickerDefaults.colors(
                clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                selectorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Shift end time picker
        Text("Shift end", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(12.dp))
        TimePicker(
            state = endState,
            colors = TimePickerDefaults.colors(
                clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                selectorColor = MaterialTheme.colorScheme.primary
            )
        )

        if (!isValid && selectedDays.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "End time must be after start time",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.error
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(
            text = "Next",
            onClick = {
                onNext(selectedDays, startState.hour, startState.minute, endState.hour, endState.minute)
            },
            enabled = isValid
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Task 4.5 — Coin Denomination Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingDenominationScreen(onNext: (denomination: CoinDenomination) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(CoinDenomination.QUARTER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Choose your coin", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Smaller coins mean more drops. Larger coins mean each drop counts more.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        CoinDenomination.entries.forEach { denomination ->
            DenominationCard(
                denomination = denomination,
                isSelected = denomination == selected,
                onClick = { selected = denomination }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(text = "Next", onClick = { onNext(selected) })
    }
}

@Composable
private fun DenominationCard(
    denomination: CoinDenomination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant

    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = denomination.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = denomination.playStyleHint,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = formatCents(denomination.valueInCents.toLong()),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Task 4.6 — Notification Permission Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingNotificationsScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("🔔", style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Stay in the loop", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Get notified when your shift starts, coins are stacking up, and at the end of the week.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        NotificationFeatureRow("⏰", "Shift Start", "Know when your coins start dropping")
        NotificationFeatureRow("🪙", "Board Status", "Get nudged when the board is getting full")
        NotificationFeatureRow("🍕", "Lunch Nudge", "A midday reminder to check in")
        NotificationFeatureRow("💰", "End of Shift", "See what you pushed off today")
        NotificationFeatureRow("💵", "Weekly Payday", "Your Friday earnings summary")

        Spacer(modifier = Modifier.height(40.dp))
        PrimaryButton(text = "Enable Notifications", onClick = onFinish)
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onFinish) {
            Text(
                "Skip for now",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun NotificationFeatureRow(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0D0B1E)
@Composable
private fun WelcomePreview() {
    SalaryPusherTheme { OnboardingWelcomeScreen(onGetStarted = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0B1E)
@Composable
private fun SalaryPreview() {
    SalaryPusherTheme { OnboardingSalaryScreen(onNext = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0B1E)
@Composable
private fun DenominationPreview() {
    SalaryPusherTheme { OnboardingDenominationScreen(onNext = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0B1E)
@Composable
private fun NotificationsPreview() {
    SalaryPusherTheme { OnboardingNotificationsScreen(onFinish = {}) }
}
