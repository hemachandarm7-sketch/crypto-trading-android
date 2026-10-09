package com.tradingdashboard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tradingdashboard.presentation.screens.DashboardScreen
import com.tradingdashboard.presentation.screens.TradesScreen
import com.tradingdashboard.presentation.screens.UploadScreen
import com.tradingdashboard.presentation.screens.AnalyticsScreen
import com.tradingdashboard.presentation.screens.SettingsScreen
import com.tradingdashboard.presentation.screens.TradeDetailScreen
import com.tradingdashboard.presentation.screens.LoginScreen
import com.tradingdashboard.presentation.screens.SignupScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object Trades : Screen("trades")
    object Upload : Screen("upload")
    object Analytics : Screen("analytics")
    object Settings : Screen("settings")
    object TradeDetail : Screen("trade_detail/{tradeId}") {
        fun createRoute(tradeId: String) = "trade_detail/$tradeId"
    }
}

@Composable
fun TradingNavHost(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    isUserAuthenticated: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isUserAuthenticated) Screen.Dashboard.route else Screen.Login.route,
        modifier = modifier
    ) {
        // Authentication Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { userId ->
                    // Clear the back stack and navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = { userId ->
                    // Clear the back stack and navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAddTradeClick = { navController.navigate(Screen.Trades.route) },
                onTradeClick = { tradeId ->
                    navController.navigate(Screen.TradeDetail.createRoute(tradeId))
                }
            )
        }

        composable(Screen.Trades.route) {
            TradesScreen(
                onTradeClick = { tradeId ->
                    navController.navigate(Screen.TradeDetail.createRoute(tradeId))
                }
            )
        }

        composable(Screen.Upload.route) {
            UploadScreen()
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.TradeDetail.route) { backStackEntry ->
            val tradeId = backStackEntry.arguments?.getString("tradeId") ?: return@composable
            TradeDetailScreen(
                tradeId = tradeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
