package com.jaehyeon.basic.biometricauth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jaehyeon.basic.biometricauth.ui.theme.BiometricAuthTheme
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity() {

    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiometricAuthTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) {
                        println("Activity result: $it")
                    }
                    var text by remember {
                        mutableStateOf("")
                    }
                    
                    ObserveAsEvent(flow = promptManager.promptResults) { result ->
                        if (result is BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
                            if (Build.VERSION.SDK_INT >= 30) {
                                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                    )
                                }
                                enrollLauncher.launch(enrollIntent)
                            }
                        }
                        
                        text = when (result) {
                            is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                                "Authentication error : ${result.error}"
                            }
                            BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                                "Authentication failed"
                            }
                            BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                                "Authentication not set"
                            }
                            BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                                "Authentication success"
                            }
                            BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                                "Feature unavailable"
                            }
                            BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                                "Hardware unavailable"
                            }
                        }
                    }
                    
                    
                    // event is not state
//                    val biometricResult by promptManager.promptResults.collectAsState(
//                        initial = null,
//                        context = Dispatchers.Main.immediate
//                    )
//                    LaunchedEffect(biometricResult) {
//                        if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
//                            if (Build.VERSION.SDK_INT >= 30) {
//                                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
//                                    putExtra(
//                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
//                                    )
//                                }
//                                enrollLauncher.launch(enrollIntent)
//                            }
//                        }
//                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            promptManager.showBiometricPrompt(
                                title = "Sample prompt",
                                description = "Sample prompt description"
                            )
                        }) {
                            Text(text = "Authenticate")
                        }
                        
                        Text(text = text)
                    }
                }
            }
        }
    }
}

