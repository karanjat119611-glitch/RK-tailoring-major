package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DressType
import com.example.data.model.OrderStatus
import com.example.ui.theme.StatusDeliveredBg
import com.example.ui.theme.StatusDeliveredFg
import com.example.ui.theme.StatusReadyBg
import com.example.ui.theme.StatusReadyFg
import com.example.ui.theme.StatusReceivedBg
import com.example.ui.theme.StatusReceivedFg
import com.example.ui.theme.StatusStitchingBg
import com.example.ui.theme.StatusStitchingFg
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun OrderStatusBadge(status: String) {
    val orderStatus = OrderStatus.fromCode(status)
    val (bg, fg, icon) = when (orderStatus) {
        OrderStatus.RECEIVED -> Triple(StatusReceivedBg, StatusReceivedFg, Icons.Default.HourglassTop)
        OrderStatus.STITCHING -> Triple(StatusStitchingBg, StatusStitchingFg, Icons.Default.ContentCut)
        OrderStatus.READY -> Triple(StatusReadyBg, StatusReadyFg, Icons.Default.CheckCircle)
        OrderStatus.DELIVERED -> Triple(StatusDeliveredBg, StatusDeliveredFg, Icons.Default.LocalShipping)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${orderStatus.labelHi} (${orderStatus.labelEn})",
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DressTypeBadge(dressType: String) {
    val type = DressType.fromCode(dressType)
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = type.labelHi,
            color = Color(0xFF000000),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun MetricStatCard(
    labelHi: String,
    labelEn: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("metric_${labelEn.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF000000)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = labelHi,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )
            Text(
                text = labelEn,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF000000)
            )
        }
    }
}

@Composable
fun TailoringSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("कस्टमर नाम या मोबाइल नंबर खोजें...", fontSize = 14.sp, color = Color(0xFF555555)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF000000)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = Color(0xFF000000)
                    )
                }
            }
        },
        textStyle = TextStyle(color = Color(0xFF000000), fontSize = 15.sp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_input"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF000000),
            unfocusedBorderColor = Color(0xFF94A3B8),
            focusedTextColor = Color(0xFF000000),
            unfocusedTextColor = Color(0xFF000000)
        ),
        singleLine = true
    )
}

@Composable
fun OfflineNoticeBanner() {
    Surface(
        color = Color(0xFFFEF3C7),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ऑफलाइन मोड: सभी नाप व ऑर्डर फ़ोन में सुरक्षित रहेंगे और इंटरनेट मिलते ही स्वतः बैकअप होंगे।",
                color = Color(0xFF000000),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 15.sp
            )
        }
    }
}

fun callCustomer(context: Context, phone: String) {
    val clean = phone.replace("[^0-9+]".toRegex(), "")
    if (clean.isNotBlank()) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        context.startActivity(intent)
    }
}

fun messageCustomerOnWhatsApp(
    context: Context,
    phone: String,
    customerName: String,
    dressType: String
) {
    val clean = phone.replace("[^0-9]".toRegex(), "")
    val formattedPhone = if (clean.length == 10) "91$clean" else clean
    val dress = DressType.fromCode(dressType).labelHi

    val message = "नमस्ते $customerName जी!\n\nआरके टेलरिंग मेजर (RK Tailoring Major) से सूचना:\nआपका $dress का सिलाई नाप हमारे पास सुरक्षित दर्ज है।\n\nधन्यवाद!\n- आरके टेलरिंग मेजर"

    try {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encoded"))
        context.startActivity(intent)
    } catch (e: Exception) {
        callCustomer(context, phone)
    }
}
