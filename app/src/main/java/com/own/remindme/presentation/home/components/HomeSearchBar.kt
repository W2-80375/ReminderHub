package com.own.remindme.presentation.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.own.remindme.ui.theme.AppIcons

@Composable
fun HomeSearchBar(

    value: String,

    onValueChange: (String) -> Unit

) {

    OutlinedTextField(

        modifier = Modifier.fillMaxWidth(),

        value = value,

        onValueChange = onValueChange,

        placeholder = {

            Text("Search reminders")

        },

        leadingIcon = {

            Icon(AppIcons.Search, null)

        },

        singleLine = true

    )

}