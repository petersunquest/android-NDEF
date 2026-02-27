package com.beamio.android_ntag

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Beamio 钱包创建引导页：BeamioTag + PIN，与 LoadingPage/CreateUsernamePinScreen 流程一致。
 */
@Composable
fun OnboardingScreen(
    onCreateComplete: (privateKeyHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var step by remember { mutableStateOf(1) }
    var beamioTag by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var tagValid by remember { mutableStateOf(false) }
    var tagStatus by remember { mutableStateOf("idle") }
    var tagError by remember { mutableStateOf("") }
    var lastValidatedTag by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create Beamio Wallet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your unique identity in the commerce layer.",
            fontSize = 16.sp,
            color = Color(0xFF64748b),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (step) {
            1 -> {
                val trimmedTag = beamioTag.trim().replace(Regex("^@+"), "")
                val formatValid = trimmedTag.length in 3..20 && trimmedTag.matches(Regex("^[a-zA-Z0-9_.]+\$"))
                LaunchedEffect(beamioTag) {
                    if (!formatValid || trimmedTag.length < 3) {
                        tagStatus = "idle"
                        tagError = ""
                        return@LaunchedEffect
                    }
                    if (tagStatus == "valid" && trimmedTag == lastValidatedTag) return@LaunchedEffect
                    delay(3000)
                    tagStatus = "checking"
                    val available = withContext(Dispatchers.IO) {
                        BeamioWalletService.checkBeamioAccountAvailable(trimmedTag)
                    }
                    tagStatus = if (available) "valid" else "invalid"
                    tagError = if (available) "" else "@$trimmedTag is already taken"
                    if (available) lastValidatedTag = trimmedTag
                }
                Text(text = "BeamioTag (3–20 chars)", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = beamioTag,
                    onValueChange = {
                        beamioTag = it.trim().replace(Regex("^@+"), "")
                        tagValid = beamioTag.length in 3..20 && beamioTag.matches(Regex("^[a-zA-Z0-9_.]+\$"))
                        tagStatus = "idle"
                        tagError = ""
                        lastValidatedTag = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("tagname") },
                    enabled = !loading && tagStatus != "checking"
                )
                if (tagStatus == "checking") {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(text = "Checking availability...", fontSize = 13.sp, color = Color(0xFF64748b))
                    }
                } else if (tagStatus == "invalid" && tagError.isNotEmpty()) {
                    Text(text = tagError, fontSize = 14.sp, color = Color(0xFFef4444), modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text(
                        text = "Permanent. Cannot be changed later.",
                        fontSize = 13.sp,
                        color = Color(0xFFf97316),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tagValid && tagStatus == "valid" && !loading
                ) {
                    Text("Next")
                }
            }
            2 -> {
                Text(text = "Set Password (6+ chars)", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        error = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Encrypts your keys locally") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !loading
                )
                Text(
                    text = "Beamio is non-custodial. We cannot reset this.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748b),
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (error.isNotEmpty()) {
                    Text(text = error, fontSize = 14.sp, color = Color(0xFFef4444), modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                if (loading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Creating wallet & registering...", fontSize = 14.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { step = 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (pin.trim().length < 6) {
                                    error = "Password must be 6+ characters"
                                    return@Button
                                }
                                loading = true
                                error = ""
                                Thread {
                                    val result = BeamioWalletService.createRecover(beamioTag.trim(), pin.trim())
                                    activity?.runOnUiThread {
                                        loading = false
                                        if (result != null) {
                                            onCreateComplete(result.privateKeyHex)
                                        } else {
                                            error = "Create failed. BeamioTag may be taken or network error."
                                        }
                                    }
                                }.start()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = pin.trim().length >= 6
                        ) {
                            Text("Create Wallet")
                        }
                    }
                }
            }
        }
    }
}
