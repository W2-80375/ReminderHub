package com.own.remindme.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

sealed class AppIcon {
    data class Vector(val imageVector: ImageVector) : AppIcon()
    data class Drawable(@DrawableRes val resId: Int) : AppIcon()
}

@Composable
fun AppIcon(
    icon: AppIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    when (icon) {
        is AppIcon.Vector -> {
            Icon(
                imageVector = icon.imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }

        is AppIcon.Drawable -> {
            Icon(
                painter = painterResource(id = icon.resId),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}
