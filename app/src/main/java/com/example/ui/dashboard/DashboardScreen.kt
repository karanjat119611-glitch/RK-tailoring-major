package com.example.ui.dashboard

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DressType
import com.example.data.model.TailoringOrder
import com.example.data.repository.TailoringRepository
import com.example.ui.components.DressTypeBadge
import com.example.ui.components.MetricStatCard
import com.example.ui.components.OfflineNoticeBanner
import com.example.ui.components.TailoringSearchBar
import com.example.ui.components.callCustomer
import com.example.ui.components.messageCustomerOnWhatsApp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightBlueBackground
import com.example.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: TailoringRepository,
    onNavigateToAddOrder: () -> Unit,
    onNavigateToOrderDetail: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val ordersState by repository.observeOrders().collectAsStateWithLifecycle(initialValue = null)

    var searchQuery by remember { mutableStateOf("") }
    var selectedDressFilter by remember { mutableStateOf("all") }

    Scaffold(
        containerColor = LightBlueBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rk_tailor_logo),
                            contentDescription = "RK Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "आरके टेलरिंग मेजर (RK Tailoring Major)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "लेडीज सिलाई व नाप रिकॉर्ड सिस्टम",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("button_sign_out")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddOrder,
                containerColor = Purple40,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_order")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Order",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "नया नाप लिखें (New Order)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        if (ordersState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Purple40)
            }
        } else {
            val allOrders = ordersState ?: emptyList()

            // Calculations based on customer and dress type (status removed)
            val totalOrdersCount = allOrders.size
            val suitsCount = allOrders.count { it.dressType == DressType.SUIT_KURTI.code }
            val poshakCount = allOrders.count { it.dressType == DressType.RAJPUTI_POSHAK.code }
            val blouseCount = allOrders.count { it.dressType == DressType.BLOUSE.code }
            val otherDressCount = totalOrdersCount - suitsCount - poshakCount - blouseCount

            // Filtering based on search and dress type
            val filteredOrders = allOrders.filter { order ->
                val matchesDress = if (selectedDressFilter == "all") {
                    true
                } else if (selectedDressFilter == "other") {
                    order.dressType != DressType.SUIT_KURTI.code &&
                            order.dressType != DressType.BLOUSE.code &&
                            order.dressType != DressType.RAJPUTI_POSHAK.code
                } else {
                    order.dressType == selectedDressFilter
                }
                val matchesSearch = if (searchQuery.isBlank()) true else {
                    order.customerName.contains(searchQuery, ignoreCase = true) ||
                            order.customerPhone.contains(searchQuery, ignoreCase = true)
                }
                matchesDress && matchesSearch
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    OfflineNoticeBanner()
                }

                // Metric Summary Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                labelHi = "कुल ग्राहक",
                                labelEn = "Total",
                                value = totalOrdersCount.toString(),
                                icon = Icons.Default.ListAlt,
                                color = Purple40,
                                isSelected = selectedDressFilter == "all",
                                onClick = { selectedDressFilter = "all" }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                labelHi = "राजपूती पोशाक",
                                labelEn = "Poshak",
                                value = poshakCount.toString(),
                                icon = Icons.Default.ContentCut,
                                color = GoldAccent,
                                isSelected = selectedDressFilter == DressType.RAJPUTI_POSHAK.code,
                                onClick = { selectedDressFilter = DressType.RAJPUTI_POSHAK.code }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MetricStatCard(
                                labelHi = "कुर्ती / सूट",
                                labelEn = "Suits",
                                value = suitsCount.toString(),
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF16A34A),
                                isSelected = selectedDressFilter == DressType.SUIT_KURTI.code,
                                onClick = { selectedDressFilter = DressType.SUIT_KURTI.code }
                            )
                        }
                    }
                }

                // Search Bar
                item {
                    TailoringSearchBar(
                        query = searchQuery,
                        onQueryChanged = { searchQuery = it }
                    )
                }

                // Filter Chips by Dress Type
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDressFilter == "all",
                                onClick = { selectedDressFilter = "all" },
                                label = {
                                    Text(
                                        text = "सभी ग्राहक ($totalOrdersCount)",
                                        color = Color(0xFF000000),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Purple40.copy(alpha = 0.2f)
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedDressFilter == DressType.RAJPUTI_POSHAK.code,
                                onClick = { selectedDressFilter = DressType.RAJPUTI_POSHAK.code },
                                label = {
                                    Text(
                                        text = "राजपूती पोशाक ($poshakCount)",
                                        color = Color(0xFF000000),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedDressFilter == DressType.SUIT_KURTI.code,
                                onClick = { selectedDressFilter = DressType.SUIT_KURTI.code },
                                label = {
                                    Text(
                                        text = "सूट / कुर्ती ($suitsCount)",
                                        color = Color(0xFF000000),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedDressFilter == DressType.BLOUSE.code,
                                onClick = { selectedDressFilter = DressType.BLOUSE.code },
                                label = {
                                    Text(
                                        text = "ब्लाउज ($blouseCount)",
                                        color = Color(0xFF000000),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                        if (otherDressCount > 0) {
                            item {
                                FilterChip(
                                    selected = selectedDressFilter == "other",
                                    onClick = { selectedDressFilter = "other" },
                                    label = {
                                        Text(
                                            text = "अन्य परिधान ($otherDressCount)",
                                            color = Color(0xFF000000),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Empty State
                if (filteredOrders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Straighten,
                                    contentDescription = null,
                                    tint = Purple40.copy(alpha = 0.5f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "कोई परिणाम नहीं मिला" else "अभी कोई सिलाई नाप दर्ज नहीं है",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "अन्य नाम या मोबाइल नंबर से खोजें" else "पहला कस्टमर नाप जोड़ने के लिए नीचे दिया गया बटन दबाएं",
                                    fontSize = 13.sp,
                                    color = Color(0xFF000000)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredOrders, key = { it.id }) { order ->
                        OrderDashboardCard(
                            order = order,
                            onClick = { onNavigateToOrderDetail(order.id) },
                            onCall = { callCustomer(context, order.customerPhone) },
                            onWhatsApp = {
                                messageCustomerOnWhatsApp(
                                    context = context,
                                    phone = order.customerPhone,
                                    customerName = order.customerName,
                                    dressType = order.dressType
                                )
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderDashboardCard(
    order: TailoringOrder,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Customer Name & Dress Type Badge (Status badge removed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Purple40.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Purple40,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = order.customerName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                        Text(
                            text = "फ़ोन: ${order.customerPhone}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF000000)
                        )
                    }
                }
                DressTypeBadge(dressType = order.dressType)
            }

            // Info Badges: Measurements count, Photos count, Notes indication (Delivery date removed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mCount = order.measurements.filter { it.value.isNotBlank() }.size
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$mCount नाप दर्ज",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (order.photoUris.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${order.photoUris.size} फोटो",
                                fontSize = 11.sp,
                                color = Color(0xFF000000),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (order.notes.isNotBlank()) {
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "नोट्स दर्ज",
                            fontSize = 11.sp,
                            color = Color(0xFF000000),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Actions Strip (Status advance removed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("कॉल", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // WhatsApp
                OutlinedButton(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("व्हाट्सएप", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    color = Purple40.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Text(
                        text = "नाप पर्ची देखें >",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple40,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
