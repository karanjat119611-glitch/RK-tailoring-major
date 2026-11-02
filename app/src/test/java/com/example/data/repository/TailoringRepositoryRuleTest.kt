package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.TailoringOrder
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TailoringRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createOrder_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = TailoringRepository(firestore)

        val order = TailoringOrder(
            customerName = "Sunita Sharma",
            customerPhone = "9876543210",
            dressType = "blouse",
            status = "received",
            totalAmount = 500.0,
            advanceAmount = 200.0,
            pendingAmount = 300.0,
            notes = "Special border design"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(order)
        }
        assertTrue("Order creation should succeed", result.isSuccess)
        val orderId = result.getOrThrow()
        assertTrue(orderId.isNotBlank())

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getOrderById(orderId)
        }
        assertTrue(fetched.isSuccess)
        val retrieved = fetched.getOrThrow()
        assertNotNull(retrieved)
        assertEquals("Sunita Sharma", retrieved?.customerName)
    }

    @Test
    fun observeOrders_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = TailoringRepository(firestore)

        val order = TailoringOrder(
            customerName = "Pooja Verma",
            customerPhone = "9812345678",
            dressType = "suit_kurti",
            status = "received",
            totalAmount = 600.0,
            advanceAmount = 300.0,
            pendingAmount = 300.0
        )
        val orderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(order).getOrThrow()
        }

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOrders().first { list -> list.any { it.id == orderId } }
        }
        assertTrue(emittedList.any { it.id == orderId })
    }

    @Test
    fun updateOrderStatus_validTransition_updatesStatus() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = TailoringRepository(firestore)

        val order = TailoringOrder(
            customerName = "Anita Singh",
            customerPhone = "9123456780",
            dressType = "lehenga",
            status = "received",
            totalAmount = 1500.0,
            advanceAmount = 500.0,
            pendingAmount = 1000.0
        )
        val orderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(order).getOrThrow()
        }

        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateOrderStatus(orderId, "stitching")
        }
        assertTrue(updateResult.isSuccess)

        val updatedOrder = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getOrderById(orderId).getOrThrow()
        }
        assertEquals("stitching", updatedOrder?.status)
    }

    @Test
    fun getOrder_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = TailoringRepository(firestore)
        val orderId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createOrder(
                TailoringOrder(
                    customerName = "Private Order",
                    customerPhone = "9000000000",
                    dressType = "blouse",
                    status = "received",
                    totalAmount = 400.0,
                    advanceAmount = 100.0,
                    pendingAmount = 300.0
                )
            ).getOrThrow()
        }

        // Bob signs in and tries to read Alice's order
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(ALICE_UID).collection("orders").document(orderId).get().await()
            }
            fail("Expected FirebaseFirestoreException for cross-user read")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException() ?: (e as? FirebaseFirestoreException)
            assertNotNull("Expected FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun observeOrders_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = TailoringRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeOrders().first()
            }
            fail("Expected exception when observing without auth")
        } catch (e: Exception) {
            assertTrue(
                "Expected IllegalStateException or PERMISSION_DENIED, got $e",
                e is IllegalStateException || e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            )
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@tailortest.com"
        const val BOB_EMAIL = "bob@tailortest.com"
        const val ALICE_UID = "alice_123"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
