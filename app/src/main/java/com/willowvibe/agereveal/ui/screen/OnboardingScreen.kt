package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 3-step onboarding flow shown on first launch.
 *
 * Step 1 — Name + Birth date (required)
 * Step 2 — Optional birth time + location
 * Step 3 — Accent colour picker + "Enter My Cosmos"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    onComplete: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (index <= step) WarmTeal else WarmSurfaceSoft),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally { -it } + fadeOut(tween(300))
                } else {
                    slideInHorizontally { -it } + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally { it } + fadeOut(tween(300))
                }
            },
            label = "onboarding_step",
            modifier = Modifier.weight(1f),
        ) { currentStep ->
            when (currentStep) {
                0 -> StepNameAndBirthDate(
                    onNameChanged = viewModel::onNameChanged,
                    onDateSelected = { date ->
                        viewModel.onBirthDateSelected(date)
                        step = 1
                    },
                )
                1 -> StepTimeAndLocation(
                    onTimeSelected = viewModel::onBirthTimeSelected,
                    onNext = { step = 2 },
                )
                2 -> StepCosmicVibe(onEnter = onComplete)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Name + Birth date
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepNameAndBirthDate(
    onNameChanged: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val todayMillis = LocalDate.now()
        .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                        if (!date.isAfter(LocalDate.now())) {
                            selectedDate = date
                            onDateSelected(date)
                        }
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Let's build your Cosmic ID",
            fontFamily = SerifFamily,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            color = WarmInk,
            textAlign = TextAlign.Center,
        )
        Text(
            "Your name and birth date unlock your entire cosmic profile.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkDim,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                onNameChanged(it)
            },
            placeholder = { Text("Your name", color = WarmInkDim) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = WarmInk),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WarmTeal,
                unfocusedBorderColor = WarmSurfaceSoft,
                cursorColor = WarmTeal,
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WarmSurface)
                .clickable { showDialog = true }
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = selectedDate?.toString() ?: "Tap to pick your birth date",
                fontFamily = SerifFamily,
                fontSize = if (selectedDate != null) 22.sp else 16.sp,
                color = if (selectedDate != null) WarmTeal else WarmInkDim,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Optional birth time + location
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepTimeAndLocation(
    onTimeSelected: (LocalTime?) -> Unit,
    onNext: () -> Unit,
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    val timePickerState = rememberTimePickerState()

    if (showTimeDialog) {
        DatePickerDialog(
            onDismissRequest = { showTimeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    selectedTime = time
                    onTimeSelected(time)
                    showTimeDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("Cancel") }
            },
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                TimePicker(state = timePickerState)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Fine-tune your chart",
            fontFamily = SerifFamily,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            color = WarmInk,
            textAlign = TextAlign.Center,
        )
        Text(
            "Add your birth time for exact Lagna and Nakshatra.\nSkip if you don't know it.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkDim,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WarmSurface)
                .clickable { showTimeDialog = true }
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = selectedTime?.toString() ?: "Tap to add birth time (optional)",
                fontFamily = SerifFamily,
                fontSize = if (selectedTime != null) 18.sp else 14.sp,
                color = if (selectedTime != null) WarmTeal else WarmInkDim,
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                onTimeSelected(null)
                onNext()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("I don't know my birth time", color = WarmInkDim)
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmTeal,
                contentColor = WarmBlack,
            ),
        ) {
            Text("Next", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3 — Accent colour + final CTA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepCosmicVibe(onEnter: () -> Unit) {
    val accentOptions = listOf(
        "Teal" to WarmTeal,
        "Amber" to WarmAmber,
        "Rose" to Color(0xFFF43F5E),
        "Lavender" to Color(0xFFA78BFA),
        "Sage" to Color(0xFF34D399),
    )
    var selected by remember { mutableStateOf(accentOptions.first()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Choose your cosmic vibe",
            fontFamily = SerifFamily,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            color = WarmInk,
            textAlign = TextAlign.Center,
        )
        Text(
            "Pick an accent that feels like you.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkDim,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            accentOptions.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(option.second)
                        .then(
                            if (isSelected) Modifier.border(3.dp, WarmInk, CircleShape)
                            else Modifier
                        )
                        .clickable { selected = option },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            selected.first,
            fontFamily = SerifFamily,
            fontSize = 18.sp,
            color = WarmInk,
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onEnter,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmTeal,
                contentColor = WarmBlack,
            ),
        ) {
            Text("Enter My Cosmos", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}
