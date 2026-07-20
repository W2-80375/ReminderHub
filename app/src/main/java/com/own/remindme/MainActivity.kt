package com.own.remindme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.own.remindme.presentation.navigation.NavGraph
import com.own.remindme.ui.theme.ReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            ReminderTheme {
                NavGraph()
            }
        }

    }

}





