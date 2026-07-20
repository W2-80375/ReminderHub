package com.own.remindme.presentation.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.own.remindme.presentation.components.chip.CategoryChip
import com.own.remindme.presentation.home.Category

@Composable
fun CategoryRow(

    categories: List<Category>,

    selected: Int,

    onCategoryClick: (Int) -> Unit

) {

    LazyRow(

        contentPadding = PaddingValues(vertical = 8.dp)

    ) {

        items(categories) {

            CategoryChip(

                title = it.title,

                color = it.color,

                selected = selected == it.id

            ) {

                onCategoryClick(it.id)

            }

        }

    }

}