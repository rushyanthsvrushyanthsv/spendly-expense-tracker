package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ExpenseRepository
import com.example.ui.SpendlyApp
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SpendlyTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = ExpenseRepository(database.expenseDao(), applicationContext)
        ExpenseViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SlateSurface
                ) {
                    SpendlyApp(viewModel = viewModel)
                }
            }
        }
    }
}
