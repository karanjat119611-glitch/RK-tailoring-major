package com.example.ui.order

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DressType
import com.example.data.model.OrderStatus
import com.example.data.model.TailoringOrder
import com.example.data.repository.TailoringRepository
import com.example.ui.theme.LightBlueBackground
import com.example.ui.theme.Purple40
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditOrderScreen(
    repository: TailoringRepository,
    orderIdToEdit: String? = null,
    onNavigateBack: () -> Unit,
    onOrderSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(orderIdToEdit != null) }
    var isSaving by remember { mutableStateOf(false) }

    // Form Fields (Payment, Status & Delivery Date removed)
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedDressType by remember { mutableStateOf(DressType.SUIT_KURTI.code) }
    var notes by remember { mutableStateOf("") }

    val photoUris = remember { mutableStateListOf<String>() }
    val measurements = remember { mutableStateMapOf<String, String>() }

    // Load existing order if editing
    LaunchedEffect(orderIdToEdit) {
        if (orderIdToEdit != null) {
            val result = repository.getOrderById(orderIdToEdit)
            val order = result.getOrNull()
            if (order != null) {
                customerName = order.customerName
                customerPhone = order.customerPhone
                selectedDressType = order.dressType
                notes = order.notes
                photoUris.clear()
                photoUris.addAll(order.photoUris)
                measurements.clear()
                measurements.putAll(order.measurements)
                if (order.measurements.containsKey("dotPoint") && !measurements.containsKey("description")) {
                    measurements["description"] = order.measurements["dotPoint"].orEmpty()
                }
            }
            isLoading = false
        }
    }

    // Photo picker launcher (up to 5 photos)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val savedPaths = withContext(Dispatchers.IO) {
                    copyUrisToInternalStorage(context, uris)
                }
                photoUris.addAll(savedPaths)
            }
        }
    }

    Scaffold(
        containerColor = LightBlueBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (orderIdToEdit == null) "नया सिलाई नाप (New Order)" else "नाप सम्पादित करें (Edit Order)",
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Customer Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Purple40,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "कस्टमर विवरण / Customer Details",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                        }

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("कस्टमर का नाम * (Customer Name)", color = Color(0xFF000000)) },
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_customer_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000),
                                focusedBorderColor = Color(0xFF000000)
                            )
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("मोबाइल नंबर * (Mobile Number)", color = Color(0xFF000000)) },
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_customer_phone"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000),
                                focusedBorderColor = Color(0xFF000000)
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF000000)
                                )
                            }
                        )
                    }
                }

                // Section 2: Dress Type Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = Purple40,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "परिधान का प्रकार / Dress Type",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000)
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DressType.entries.forEach { dress ->
                                val selected = selectedDressType == dress.code
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedDressType = dress.code },
                                    label = {
                                        Text(
                                            text = "${dress.labelHi} (${dress.labelEn})",
                                            fontSize = 12.sp,
                                            color = if (selected) Color.White else Color(0xFF000000),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Purple40,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Section 3: Detailed Measurements
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = Purple40,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "सिलाई के सम्पूर्ण नाप / Measurements",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                                Text(
                                    text = "इंच (Inches) में माप दर्ज करें",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF000000)
                                )
                            }
                        }

                        // Ladies & Rajputi Poshak Measurement Fields
                        // hips, sleeve length, sleeve round, shoulder, armhole, daman removed
                        // sleeve ordered before waist, length ordered after waist
                        val measurementFields = listOf(
                            Triple("topLength", "लंबाई (Length)", "उदा. 26"),
                            Triple("chest", "सीना (Chest)", "उदा. 34"),
                            Triple("sleeve", "आस्तीन (Sleeve)", "उदा. 15"),
                            Triple("waist", "कमर (Waist)", "उदा. 28"),
                            Triple("length", "लंबाई (Length)", "उदा. 38"),
                            Triple("neckFront", "गला आगे (Front Neck)", "उदा. 6"),
                            Triple("neckBack", "गला पीछे (Back Neck)", "उदा. 7.5"),
                            Triple("pantLength", "लहंगा / सलवार लंबाई (Bottom Length)", "उदा. 38"),
                            Triple("bottomMohri", "मोहरी (Bottom Mohri)", "उदा. 6.5"),
                            Triple("description", "नोट / विवरण (Note / Description)", "उदा. विशेष विवरण")
                        )

                        // 2 Columns Grid for Measurements
                        for (i in measurementFields.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val field1 = measurementFields[i]
                                OutlinedTextField(
                                    value = measurements[field1.first] ?: "",
                                    onValueChange = { measurements[field1.first] = it },
                                    label = { Text(field1.second, fontSize = 11.sp, color = Color(0xFF000000), fontWeight = FontWeight.Bold) },
                                    placeholder = { Text(field1.third, fontSize = 11.sp) },
                                    textStyle = TextStyle(color = Color(0xFF000000), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("measurement_${field1.first}"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF000000),
                                        unfocusedTextColor = Color(0xFF000000),
                                        focusedBorderColor = Color(0xFF000000)
                                    )
                                )

                                if (i + 1 < measurementFields.size) {
                                    val field2 = measurementFields[i + 1]
                                    OutlinedTextField(
                                        value = measurements[field2.first] ?: "",
                                        onValueChange = { measurements[field2.first] = it },
                                        label = { Text(field2.second, fontSize = 11.sp, color = Color(0xFF000000), fontWeight = FontWeight.Bold) },
                                        placeholder = { Text(field2.third, fontSize = 11.sp) },
                                        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("measurement_${field2.first}"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color(0xFF000000),
                                            unfocusedTextColor = Color(0xFF000000),
                                            focusedBorderColor = Color(0xFF000000)
                                        )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Section 4: Multi-Photo Upload (Cloth, Pattern, Design)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = null,
                                    tint = Purple40,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "कपड़े व डिज़ाइन के फोटो (${photoUris.size}/5)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF000000)
                                    )
                                    Text(
                                        text = "Fabric, Neck & Embroidery Photos",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF000000)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                                modifier = Modifier.testTag("button_add_photos")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("फोटो जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (photoUris.isEmpty()) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "कोई फोटो नहीं जोड़ी गई। कपड़े का रंग, पैटर्न या डिज़ाइन का फोटो जोड़ने के लिए ऊपर बटन दबाएं।",
                                    fontSize = 12.sp,
                                    color = Color(0xFF000000),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(photoUris) { uriString ->
                                    Box(
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF000000), RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = File(uriString),
                                            contentDescription = "Order Photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.7f),
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(22.dp)
                                                .clickable { photoUris.remove(uriString) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove photo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 5: Design Notes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "डिज़ाइन निर्देश व विशेष नोट्स / Special Notes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("डिज़ाइन निर्देश / अस्तर / लटकन / स्पेशल नोट्स", color = Color(0xFF000000)) },
                            placeholder = { Text("उदा. आगे वी-गला, पीछे डोरी लटकन, कॉटन अस्तर...") },
                            textStyle = TextStyle(color = Color(0xFF000000), fontSize = 14.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("input_notes"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000),
                                focusedBorderColor = Color(0xFF000000)
                            )
                        )
                    }
                }

                // Save Order Button
                Button(
                    onClick = {
                        if (customerName.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("कृपया कस्टमर का नाम दर्ज करें!")
                            }
                            return@Button
                        }
                        if (customerPhone.isBlank() || customerPhone.length < 5) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("कृपया मान्य मोबाइल नंबर दर्ज करें!")
                            }
                            return@Button
                        }

                        isSaving = true
                        val orderToSave = TailoringOrder(
                            id = orderIdToEdit ?: "",
                            customerName = customerName,
                            customerPhone = customerPhone,
                            dressType = selectedDressType,
                            status = "received",
                            measurements = measurements.toMap(),
                            notes = notes,
                            photoUris = photoUris.toList(),
                            deliveryDate = "",
                            totalAmount = 0.0,
                            advanceAmount = 0.0,
                            pendingAmount = 0.0
                        )

                        coroutineScope.launch {
                            val result = if (orderIdToEdit != null) {
                                repository.updateOrder(orderToSave)
                            } else {
                                repository.createOrder(orderToSave)
                            }
                            isSaving = false
                            if (result.isSuccess) {
                                onOrderSaved()
                            } else {
                                snackbarHostState.showSnackbar(
                                    "ऑर्डर सुरक्षित करने में त्रुटि: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("button_save_order"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("सहेजा जा रहा है...", color = Color.White)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (orderIdToEdit == null) "नाप सुरक्षित करें (Save Measurements)" else "अपडेट सुरक्षित करें (Update Order)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Helper to copy selected photos from Content URIs into app internal filesDir
private fun copyUrisToInternalStorage(context: Context, uris: List<Uri>): List<String> {
    val results = mutableListOf<String>()
    val dir = File(context.filesDir, "order_photos")
    if (!dir.exists()) dir.mkdirs()

    for (uri in uris) {
        try {
            val fileName = "tailor_${UUID.randomUUID()}.jpg"
            val destFile = File(dir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destFile.exists() && destFile.length() > 0) {
                results.add(destFile.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return results
}
