package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TailoringOrder(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val dressType: String = "suit_kurti", // suit_kurti, blouse, salwar, lehenga, frock_gown, other
    val status: String = "received", // received, stitching, ready, delivered
    val measurements: Map<String, String> = emptyMap(),
    val notes: String = "",
    val photoUris: List<String> = emptyList(),
    val deliveryDate: String = "", // YYYY-MM-DD
    val totalAmount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun toFirestoreMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "dressType" to dressType,
            "status" to status,
            "totalAmount" to totalAmount,
            "advanceAmount" to advanceAmount,
            "pendingAmount" to pendingAmount,
            "notes" to notes,
            "deliveryDate" to deliveryDate,
            "measurements" to measurements,
            "photoUris" to photoUris
        )
        return map.filterValues { it != null }
    }
}

enum class OrderStatus(val code: String, val labelHi: String, val labelEn: String) {
    RECEIVED("received", "नया ऑर्डर", "New Order"),
    STITCHING("stitching", "सिलाई जारी", "In Stitching"),
    READY("ready", "तैयार माल", "Ready for Pickup"),
    DELIVERED("delivered", "डिलीवर हो गया", "Delivered");

    companion object {
        fun fromCode(code: String): OrderStatus = entries.find { it.code == code } ?: RECEIVED
    }
}

enum class DressType(val code: String, val labelHi: String, val labelEn: String) {
    SUIT_KURTI("suit_kurti", "सूट / कुर्ती", "Suit / Kurti"),
    BLOUSE("blouse", "ब्लाउज", "Blouse"),
    RAJPUTI_POSHAK("rajputi_poshak", "राजपूती पोशाक", "Rajputi Poshak"),
    SALWAR("salwar", "सलवार / पैंट", "Salwar / Pant"),
    LEHENGA("lehenga", "लहंगा / चोली", "Lehenga / Choli"),
    FROCK_GOWN("frock_gown", "गाउन / फ्रॉक", "Gown / Frock"),
    OTHER("other", "अन्य परिधान", "Other");

    companion object {
        fun fromCode(code: String): DressType = entries.find { it.code == code } ?: SUIT_KURTI
    }
}
