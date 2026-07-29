package com.rtiqa.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.mobile.R
import com.rtiqa.mobile.domain.model.Course
import com.rtiqa.mobile.domain.model.UserProfile
import com.rtiqa.mobile.ui.components.CourseCard
import com.rtiqa.mobile.ui.components.XpCoinChip

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    courses: List<Course>,
    onCourseClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onToggleBookmark: (String, Boolean) -> Unit,
    onToggleDownload: (String, Boolean) -> Unit,
    onToggleLanguage: () -> Unit,
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showNotificationsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Profile Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ai_tutor_avatar_1785095337393),
                            contentDescription = "الصورة الشخصية",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.welcome_user, userProfile.name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.user_level_subtitle, userProfile.level),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "الإشعارات",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.testTag("lang_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "تغيير اللغة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (showNotificationsDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "التنبيهات والإشعارات", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NotificationCard(
                                title = "تمت المزامنة أوتوماتيكياً 🔄",
                                time = "منذ 5 دقائق",
                                desc = "تم حفظ جميع تقدمك واختباراتك في قاعدة البيانات الملحية والسحابية بنجاح."
                            )
                            NotificationCard(
                                title = "مستشار الذكاء الاصطناعي 🤖",
                                time = "ساعة واحدة",
                                desc = "قام المعلم الذكي بإعداد خطة مراجعة مخصصة لدرس المعالجة اللغوية."
                            )
                            NotificationCard(
                                title = "إنجاز جديد في الانتظار! 🏆",
                                time = "أمس",
                                desc = "أكمل اختباراً واحداً إضافياً لفتح وسام \"خبير الاختبارات\" واستلام 300 XP."
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showNotificationsDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تم القراءة")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // XP and Coins Bar
            XpCoinChip(
                xp = userProfile.xp,
                coins = userProfile.coins,
                streak = userProfile.streakDays,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onNavigate("ai_tutor") },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1785095314710),
                        contentDescription = "الشعار الرئيسي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "المعلم الذكي",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.ai_tutor_banner_title),
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.ai_tutor_banner_desc),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions Hub
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionItem(
                    icon = Icons.Default.AutoAwesome,
                    label = stringResource(R.string.quick_action_ai_tutor),
                    bgColor = Color(0xFF312E81),
                    onClick = { onNavigate("ai_tutor") }
                )
                QuickActionItem(
                    icon = Icons.Default.Quiz,
                    label = stringResource(R.string.quick_action_quiz),
                    bgColor = Color(0xFF831843),
                    onClick = { onNavigate("quiz") }
                )
                QuickActionItem(
                    icon = Icons.Default.Download,
                    label = stringResource(R.string.quick_action_downloads),
                    bgColor = Color(0xFF065F46),
                    onClick = { onNavigate("downloads") }
                )
                QuickActionItem(
                    icon = Icons.Default.MenuBook,
                    label = stringResource(R.string.quick_action_catalog),
                    bgColor = Color(0xFF1E3A8A),
                    onClick = { onNavigate("courses") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Featured / Continue Learning Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_continue_recommended),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.see_all),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigate("courses") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(courses) { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course.id) },
                onToggleBookmark = { onToggleBookmark(course.id, course.isBookmarked) },
                onToggleDownload = { onToggleDownload(course.id, course.isDownloaded) },
                isArabic = isArabic,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav
        }
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun NotificationCard(title: String, time: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
