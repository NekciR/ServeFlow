package com.rickendy.serveflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.rickendy.serveflow.ui.components.dialog.DialogViewModel
import com.rickendy.serveflow.ui.components.dialog.GlobalDialogHost
import com.rickendy.serveflow.ui.navigation.AppNavHost
import com.rickendy.serveflow.ui.theme.ServeFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServeFlowTheme (darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                    GlobalDialogHost()
                }
            }
        }
    }
}