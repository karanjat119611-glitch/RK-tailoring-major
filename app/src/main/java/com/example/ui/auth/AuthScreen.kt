package com.example.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Purple40

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFBAE6FD),
                        Color(0xFFE0F2FE),
                        Color(0xFFEBF5FB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Logo
            Image(
                painter = painterResource(id = R.drawable.ic_rk_tailor_logo),
                contentDescription = "RK Tailoring Major Logo",
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hindi & English Branding
            Text(
                text = "आरके टेलरिंग मेजर",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            )
            Text(
                text = "RK Tailoring Major",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "लेडीज सिलाई, सम्पूर्ण नाप रिकॉर्ड, फोटो अटैचमेंट व क्लाउड बैकअप",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature Highlights Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureRow(
                        icon = Icons.Default.Straighten,
                        titleHi = "सिलाई के सम्पूर्ण नाप",
                        titleEn = "Complete Ladies Measurements (लंबाई, छाती, कमर, आस्तीन)",
                        iconTint = Purple40
                    )
                    FeatureRow(
                        icon = Icons.Default.PhotoCamera,
                        titleHi = "मल्टी-फोटो अपलोड",
                        titleEn = "Cloth, Design & Pattern Photos (कपड़े व डिज़ाइन फोटो)",
                        iconTint = GoldAccent
                    )
                    FeatureRow(
                        icon = Icons.Default.AutoAwesome,
                        titleHi = "कस्टमर व नाप रिकॉर्ड खोज",
                        titleEn = "Customer Directory & Quick Measurement Search",
                        iconTint = Color(0xFF0D9488)
                    )
                    FeatureRow(
                        icon = Icons.Default.CloudDone,
                        titleHi = "क्लाउड बैकअप व ऑफलाइन मोड",
                        titleEn = "Works offline & auto syncs to Cloud",
                        iconTint = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFF991B1B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Google Sign-In Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    GoogleSignInHelper.onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { err ->
                            isLoading = false
                            errorMessage = err
                        },
                        scope = coroutineScope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple40,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("लॉगिन हो रहा है...", fontSize = 16.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Google Sign In",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "गूगल से लॉगिन करें / Sign in with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "सुरक्षित लॉगिन द्वारा आपका टेलरिंग डेटा क्लाउड में सुरक्षित रहता है",
                fontSize = 12.sp,
                color = Color(0xFF000000),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    titleHi: String,
    titleEn: String,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = titleHi,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )
            Text(
                text = titleEn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000)
            )
        }
    }
}
