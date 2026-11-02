package com.example.ui.order

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.DressType
import com.example.data.model.TailoringOrder
import com.example.data.repository.TailoringRepository
import com.example.ui.components.DressTypeBadge
import com.example.ui.components.callCustomer
import com.example.ui.components.messageCustomerOnWhatsApp
import com.example.ui.theme.LightBlueBackground
import com.example.ui.theme.Purple40
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    repository: TailoringRepository,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var order by remember { mutableStateOf<TailoringOrder?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullScreenPhotoUrl by remember { mutableStateOf<String?>(null) }

    fun refreshOrder() {
        coroutineScope.launch {
            val result = repository.getOrderById(orderId)
            order = result.getOrNull()
            isLoading = false
        }
    }

    LaunchedEffect(orderId) {
        refreshOrder()
    }

    Scaffold(
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "सिलाई नाप पर्ची",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "आरके टेलरिंग मेजर (RK Tailoring Major)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(orderId) },
                        modifier = Modifier.testTag("button_edit_order")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Order",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Order",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple40,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Purple40)
            }
        } else if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("ऑर्डर प्राप्त नहीं हुआ।", color = Color(0xFF000000))
            }
        } else {
            val ord = order!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer & Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ord.customerName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = "मोबाइल नंबर: ${ord.customerPhone}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF000000)
                                )
                            }
                            DressTypeBadge(dressType = ord.dressType)
                        }

                        // Call & WhatsApp Quick Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { callCustomer(context, ord.customerPhone) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_call_customer"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("कॉल करें (Call)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    messageCustomerOnWhatsApp(
                                        context = context,
                                        phone = ord.customerPhone,
                                        customerName = ord.customerName,
                                        dressType = ord.dressType
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_whatsapp_customer"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("व्हाट्सएप संदेश", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Measurement Sheet Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "सिलाई के सम्पूर्ण नाप (Measurements)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                            IconButton(onClick = {
                                val text = buildMeasurementShareText(ord)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Tailoring Measurement", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "नाप कॉपी हो गया (Copied)!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Purple40
                                )
                            }
                        }

                        val labels = mapOf(
                            "topLength" to "लंबाई (Length)",
                            "chest" to "सीना (Chest)",
                            "sleeve" to "आस्तीन (Sleeve)",
                            "waist" to "कमर (Waist)",
                            "length" to "लंबाई (Length)",
                            "neckFront" to "गला आगे (Front Neck)",
                            "neckBack" to "गला पीछे (Back Neck)",
                            "pantLength" to "लहंगा / सलवार लंबाई (Bottom Length)",
                            "bottomMohri" to "मोहरी (Bottom Mohri)",
                            "description" to "नोट / विवरण (Note / Description)",
                            "dotPoint" to "नोट / विवरण (Note / Description)"
                        )

                        val preferredOrder = listOf(
                            "topLength",
                            "chest",
                            "sleeve",
                            "waist",
                            "length",
                            "neckFront",
                            "neckBack",
                            "pantLength",
                            "bottomMohri",
                            "description",
                            "dotPoint"
                        )

                        if (ord.measurements.isEmpty()) {
                            Text(
                                text = "कोई नाप दर्ज नहीं किया गया।",
                                fontSize = 13.sp,
                                color = Color(0xFF000000)
                            )
                        } else {
                            val activeMeasurements = ord.measurements.filter { it.value.isNotBlank() }
                            val sortedMeasurements = activeMeasurements.entries.sortedBy { entry ->
                                val index = preferredOrder.indexOf(entry.key)
                                if (index != -1) index else 999
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sortedMeasurements.forEach { (key, value) ->
                                    Surface(
                                        color = Color(0xFFF0F8FF),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text(
                                                text = labels[key] ?: key,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF000000)
                                            )
                                            Text(
                                                text = if (key == "description" || key == "dotPoint" || !value.all { it.isDigit() || it == '.' || it == '/' || it == '-' }) value else "$value\"",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF000000)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Photos Gallery Section
                if (ord.photoUris.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "संलग्न फोटो (${ord.photoUris.size}) / Attached Photos",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(ord.photoUris) { path ->
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = "Cloth Photo",
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF000000), RoundedCornerShape(8.dp))
                                            .clickable { fullScreenPhotoUrl = path },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes Card
                if (ord.notes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "डिज़ाइन निर्देश व नोट्स (Design Instructions)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                            Text(
                                text = ord.notes,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF000000),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Full screen photo dialog
    if (fullScreenPhotoUrl != null) {
        Dialog(onDismissRequest = { fullScreenPhotoUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { fullScreenPhotoUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(fullScreenPhotoUrl!!),
                    contentDescription = "Full photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ऑर्डर डिलीट करें?", color = Color(0xFF000000), fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप इस सिलाई ऑर्डर को हटाना चाहते हैं? यह क्रिया वापस नहीं ली जा सकती।", color = Color(0xFF000000)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            repository.deleteOrder(orderId)
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("हाँ, डिलीट करें", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("रद्द करें", color = Color(0xFF000000))
                }
            }
        )
    }
}

private fun buildMeasurementShareText(order: TailoringOrder): String {
    val dress = DressType.fromCode(order.dressType).labelHi
    val sb = StringBuilder()
    sb.appendLine("🧵 *आरके टेलरिंग मेजर (RK Tailoring Major)*")
    sb.appendLine("कस्टमर: ${order.customerName}")
    sb.appendLine("मोबाइल: ${order.customerPhone}")
    sb.appendLine("परिधान: $dress")
    sb.appendLine("--- सिलाई नाप (Measurements) ---")
    val labels = mapOf(
        "topLength" to "लंबाई (Length)",
        "chest" to "सीना (Chest)",
        "sleeve" to "आस्तीन (Sleeve)",
        "waist" to "कमर (Waist)",
        "length" to "लंबाई (Length)",
        "neckFront" to "गला आगे (Front Neck)",
        "neckBack" to "गला पीछे (Back Neck)",
        "pantLength" to "लहंगा / सलवार लंबाई (Bottom Length)",
        "bottomMohri" to "मोहरी (Bottom Mohri)",
        "description" to "नोट / विवरण (Note / Description)",
        "dotPoint" to "नोट / विवरण (Note / Description)"
    )
    val preferredOrder = listOf(
        "topLength", "chest", "sleeve", "waist", "length", "neckFront", "neckBack", "pantLength", "bottomMohri", "description", "dotPoint"
    )
    val activeMeasurements = order.measurements.filter { it.value.isNotBlank() }
    val sortedMeasurements = activeMeasurements.entries.sortedBy { entry ->
        val idx = preferredOrder.indexOf(entry.key)
        if (idx != -1) idx else 999
    }
    sortedMeasurements.forEach { (k, v) ->
        val label = labels[k] ?: k
        if (k == "description" || k == "dotPoint" || !v.all { it.isDigit() || it == '.' || it == '/' || it == '-' }) {
            sb.appendLine("$label: $v")
        } else {
            sb.appendLine("$label: $v\"")
        }
    }
    if (order.notes.isNotBlank()) sb.appendLine("नोट्स: ${order.notes}")
    return sb.toString()
}
