package com.moluccasdev.poskasirqris.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moluccasdev.poskasirqris.POSApplication
import com.moluccasdev.poskasirqris.ui.screens.CreateOrderScreen
import com.moluccasdev.poskasirqris.ui.screens.OrderScreen
import com.moluccasdev.poskasirqris.ui.screens.PaymentScreen
import com.moluccasdev.poskasirqris.ui.screens.CalculatorScreen
import com.moluccasdev.poskasirqris.ui.screens.ReportScreen
import com.moluccasdev.poskasirqris.ui.screens.SettingsScreen

enum class Screen {
    ORDER, CREATE_ORDER, CALCULATOR, PAYMENT, REPORT, SETTINGS
}

@Composable
fun POSKasirApp() {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as POSApplication).container
    val factory = remember { ViewModelFactory(appContainer.appRepository) }

    // VM Instances
    val orderViewModel: OrderViewModel = viewModel(factory = factory)
    val calculatorViewModel: CalculatorViewModel = viewModel(factory = factory)
    val reportViewModel: ReportViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)

    var currentScreen by remember { mutableStateOf(Screen.ORDER) }

    // Intercept hardware back button:
    // If not on the ORDER screen, navigate to the ORDER screen instead of exiting the app.
    if (currentScreen != Screen.ORDER) {
        BackHandler(enabled = true) {
            currentScreen = Screen.ORDER
        }
    }

    // STRICT PHONE VERSION: Scaffold with Bottom Navigation Bar
    // We hide the bottom bar on full-screen focused flows (CREATE_ORDER & PAYMENT).
    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.CREATE_ORDER && currentScreen != Screen.PAYMENT) {
                BottomNavigationBar(
                    activeScreen = currentScreen,
                    onScreenSelected = { selected ->
                        currentScreen = selected
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            RenderScreen(
                screen = currentScreen,
                orderVM = orderViewModel,
                calcVM = calculatorViewModel,
                reportVM = reportViewModel,
                settingsVM = settingsViewModel,
                onNavigateToCreateOrder = { currentScreen = Screen.CREATE_ORDER },
                onNavigateToPayment = { currentScreen = Screen.PAYMENT },
                onNavigateBackToOrderList = { currentScreen = Screen.ORDER }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    activeScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val selectedOrder = activeScreen == Screen.ORDER || activeScreen == Screen.CREATE_ORDER
        
        // Define explicit visible color palette for Navigation items
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.outline,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationBarItem(
            selected = selectedOrder,
            onClick = { onScreenSelected(Screen.ORDER) },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Order") },
            label = { Text("Order", fontWeight = FontWeight.Bold) },
            colors = itemColors,
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = activeScreen == Screen.CALCULATOR,
            onClick = { onScreenSelected(Screen.CALCULATOR) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Calculator") },
            label = { Text("Calculator", fontWeight = FontWeight.Bold) },
            colors = itemColors,
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = activeScreen == Screen.REPORT,
            onClick = { onScreenSelected(Screen.REPORT) },
            icon = { Icon(Icons.Default.List, contentDescription = "Laporan") },
            label = { Text("Laporan", fontWeight = FontWeight.Bold) },
            colors = itemColors,
            alwaysShowLabel = true
        )
        NavigationBarItem(
            selected = activeScreen == Screen.SETTINGS,
            onClick = { onScreenSelected(Screen.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Setting") },
            label = { Text("Setting", fontWeight = FontWeight.Bold) },
            colors = itemColors,
            alwaysShowLabel = true
        )
    }
}

@Composable
fun RenderScreen(
    screen: Screen,
    orderVM: OrderViewModel,
    calcVM: CalculatorViewModel,
    reportVM: ReportViewModel,
    settingsVM: SettingsViewModel,
    onNavigateToCreateOrder: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateBackToOrderList: () -> Unit
) {
    when (screen) {
        Screen.ORDER -> OrderScreen(
            orderVM = orderVM,
            onAddOrderClick = onNavigateToCreateOrder,
            onEditOrderClick = onNavigateToCreateOrder,
            onSelectOrderForPayment = {
                // Tapping draft goes directly to Payment checkout screen and populates total amount re-calculated re-actively
                calcVM.prepareCheckout(orderVM.cartTotal)
                calcVM.onKeyPress("C")
                onNavigateToPayment()
            }
        )
        Screen.CREATE_ORDER -> CreateOrderScreen(
            orderVM = orderVM,
            calcVM = calcVM,
            onNavigateToPayment = onNavigateToPayment,
            onNavigateBack = onNavigateBackToOrderList
        )
        Screen.CALCULATOR -> CalculatorScreen(calcVM)
        Screen.PAYMENT -> PaymentScreen(calcVM, orderVM, onNavigateBack = onNavigateBackToOrderList)
        Screen.REPORT -> ReportScreen(reportVM)
        Screen.SETTINGS -> SettingsScreen(settingsVM)
    }
}

// Ensure context is available locally
private val LocalContext = androidx.compose.ui.platform.LocalContext
