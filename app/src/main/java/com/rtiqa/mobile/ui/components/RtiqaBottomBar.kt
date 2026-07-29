package com.rtiqa.mobile.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.rtiqa.mobile.R

sealed class NavItem(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    object Home : NavItem("home", R.string.nav_home, Icons.Default.Home)
    object Courses : NavItem("courses", R.string.nav_courses, Icons.Default.MenuBook)
    object AiTutor : NavItem("ai_tutor", R.string.nav_ai_tutor, Icons.Default.AutoAwesome)
    object Quiz : NavItem("quiz", R.string.nav_quiz, Icons.Default.Quiz)
    object Downloads : NavItem("downloads", R.string.nav_downloads, Icons.Default.Download)
    object Profile : NavItem("profile", R.string.nav_profile, Icons.Default.Person)
}

@Composable
fun RtiqaBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem.Home,
        NavItem.Courses,
        NavItem.AiTutor,
        NavItem.Quiz,
        NavItem.Downloads,
        NavItem.Profile
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    label = "navBg"
                )
                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "navContent"
                )

                IconButton(
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier
                        .testTag("nav_item_${item.route}")
                        .clip(RoundedCornerShape(16.dp))
                        .background(animatedBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.titleRes),
                                tint = animatedContentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
