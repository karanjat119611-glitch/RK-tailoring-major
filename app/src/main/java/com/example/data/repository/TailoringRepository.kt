package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.TailoringOrder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TailoringRepository(private val db: FirebaseFirestore) {

    // Resolves custom database ID from firebase_applet_config.xml
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing tailoring data.")
    }

    /**
     * Observes real-time tailoring orders for the authenticated tailor.
     * When offline, Firestore automatically delivers cached documents.
     */
    fun observeOrders(): Flow<List<TailoringOrder>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/orders"
        emitAll(
            db.collection("users").document(uid).collection("orders")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TailoringOrder::class.java).sortedByDescending { it.createdAt }
                }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    /**
     * Creates a new tailoring order document with server timestamps.
     */
    suspend fun createOrder(order: TailoringOrder): Result<String> {
        val uid = requireUserId()
        val orderId = if (order.id.isNotBlank()) order.id else "ord_${UUID.randomUUID().toString().replace("-", "").substring(0, 12)}"
        val docRef = db.collection("users").document(uid).collection("orders").document(orderId)

        val payload = HashMap<String, Any?>()
        payload["userId"] = uid
        payload["customerName"] = order.customerName.trim()
        payload["customerPhone"] = order.customerPhone.trim()
        payload["dressType"] = order.dressType
        payload["status"] = order.status
        payload["totalAmount"] = order.totalAmount
        payload["advanceAmount"] = order.advanceAmount
        payload["pendingAmount"] = (order.totalAmount - order.advanceAmount).coerceAtLeast(0.0)
        payload["notes"] = order.notes
        payload["deliveryDate"] = order.deliveryDate
        payload["measurements"] = order.measurements
        payload["photoUris"] = order.photoUris
        payload["createdAt"] = FieldValue.serverTimestamp()
        payload["updatedAt"] = FieldValue.serverTimestamp()

        return try {
            docRef.set(payload).await()
            Result.success(orderId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Updates an existing tailoring order.
     */
    suspend fun updateOrder(order: TailoringOrder): Result<Unit> {
        val uid = requireUserId()
        require(order.id.isNotBlank()) { "Order ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("orders").document(order.id)

        val updates = HashMap<String, Any?>()
        updates["customerName"] = order.customerName.trim()
        updates["customerPhone"] = order.customerPhone.trim()
        updates["dressType"] = order.dressType
        updates["status"] = order.status
        updates["totalAmount"] = order.totalAmount
        updates["advanceAmount"] = order.advanceAmount
        updates["pendingAmount"] = (order.totalAmount - order.advanceAmount).coerceAtLeast(0.0)
        updates["notes"] = order.notes
        updates["deliveryDate"] = order.deliveryDate
        updates["measurements"] = order.measurements
        updates["photoUris"] = order.photoUris
        updates["updatedAt"] = FieldValue.serverTimestamp()

        return try {
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Quick status update (e.g. Received -> Stitching -> Ready -> Delivered).
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("orders").document(orderId)

        return try {
            docRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Deletes an order.
     */
    suspend fun deleteOrder(orderId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("orders").document(orderId)

        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Fetches single order by ID.
     */
    suspend fun getOrderById(orderId: String): Result<TailoringOrder?> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("orders").document(orderId)

        return try {
            val snapshot = docRef.get().await()
            val order = snapshot.toObject(TailoringOrder::class.java)
            Result.success(order)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }
}
