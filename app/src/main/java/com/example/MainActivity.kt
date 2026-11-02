package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.TailoringRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.GoogleSignInHelper
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.order.AddEditOrderScreen
import com.example.ui.order.OrderDetailScreen
import com.example.ui.theme.Purple40
import com.example.ui.theme.RTailoringTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RTailoringTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    val credentialManager = remember { CredentialManager.create(context) }

    // Real-time Auth listener
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Auto sign-in attempt on startup
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            GoogleSignInHelper.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    isCheckingAuth = false
                },
                onUnauthenticated = {
                    isCheckingAuth = false
                },
                scope = coroutineScope
            )
        } else {
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Purple40)
        }
    } else if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        TailoringAppNav(
            onSignOut = {
                GoogleSignInHelper.signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
fun TailoringAppNav(onSignOut: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val repository = remember { TailoringRepository(context) }

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("dashboard") {
            DashboardScreen(
                repository = repository,
                onNavigateToAddOrder = { navController.navigate("add_order") },
                onNavigateToOrderDetail = { orderId -> navController.navigate("order_detail/$orderId") },
                onSignOut = onSignOut
            )
        }

        composable("add_order") {
            AddEditOrderScreen(
                repository = repository,
                orderIdToEdit = null,
                onNavigateBack = { navController.popBackStack() },
                onOrderSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_order/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")
            AddEditOrderScreen(
                repository = repository,
                orderIdToEdit = orderId,
                onNavigateBack = { navController.popBackStack() },
                onOrderSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { editId -> navController.navigate("edit_order/$editId") }
            )
        }
    }
}
