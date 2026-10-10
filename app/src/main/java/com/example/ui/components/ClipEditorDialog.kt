package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClipItem
import com.example.ui.theme.AutoClipPrimary
import com.example.ui.theme.AutoClipSecondary

@Composable
fun ClipEditorDialog(
    clip: ClipItem,
    onDismiss: () -> Unit,
    onSave: (clipId: String, newTitle: String, newStart: Int, newEnd: Int) -> Unit
) {
    var title by remember { mutableStateOf(clip.title) }
    var startSec by remember { mutableIntStateOf(clip.startSeconds) }
    var endSec by remember { mutableIntStateOf(clip.endSeconds) }

    val duration = (endSec - startSec).coerceAtLeast(1)

    fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return String.format("%02d:%02d", m, s)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Fine-Tune Clip & Trim",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Clip Hook Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AutoClipPrimary,
                        unfocusedBorderColor = Color(0xFF3B4058)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clip_editor_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Duration Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1F2335),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resulting Duration:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB3B8CC)
                        )
                        Text(
                            text = "${duration}s (${formatTime(startSec)} - ${formatTime(endSec)})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AutoClipSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start Time Adjuster
                Text(
                    text = "Start Timestamp: ${formatTime(startSec)} (${startSec}s)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeStepButton("-5s") { startSec = (startSec - 5).coerceAtLeast(0) }
                    TimeStepButton("-1s") { startSec = (startSec - 1).coerceAtLeast(0) }
                    TimeStepButton("+1s") {
                        if (startSec + 1 < endSec) startSec += 1
                    }
                    TimeStepButton("+5s") {
                        if (startSec + 5 < endSec) startSec += 5
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // End Time Adjuster
                Text(
                    text = "End Timestamp: ${formatTime(endSec)} (${endSec}s)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeStepButton("-5s") {
                        if (endSec - 5 > startSec + 2) endSec -= 5
                    }
                    TimeStepButton("-1s") {
                        if (endSec - 1 > startSec + 2) endSec -= 1
                    }
                    TimeStepButton("+1s") { endSec += 1 }
                    TimeStepButton("+5s") { endSec += 5 }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(clip.id, title, startSec, endSec)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AutoClipPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("clip_editor_save_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF141724),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TimeStepButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF262B3F),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE2E6F5),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
