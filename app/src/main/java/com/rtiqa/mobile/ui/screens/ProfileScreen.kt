package com.rtiqa.mobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtiqa.mobile.R
import com.rtiqa.mobile.domain.model.UserProfile
import androidx.compose.ui.res.stringResource

import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onNavigateToAdmin: () -> Unit = {},
    isArabic: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showCertificateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Learner Card Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_header_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.xp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${userProfile.xp}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.coins), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${userProfile.coins}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.level), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${userProfile.level}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    if (userProfile.isAdmin) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToAdmin,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "لوحة تحكم المسؤول", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Verified Certificate Section
            Text(
                text = stringResource(R.string.certificates_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCertificateDialog = true }
                    .testTag("certificate_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CardMembership,
                                contentDescription = "شهادة",
                                tint = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.accredited_cert),
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "موثق",
                            tint = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.cert_ai_title),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cert_issued_to, userProfile.name),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "انقر للاستعراض 🔍",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showCertificateDialog) {
                AlertDialog(
                    onDismissRequest = { showCertificateDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CardMembership,
                                contentDescription = "شهادة اعتماد",
                                tint = Color(0xFFF59E0B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "شهادة إتمام معتمدة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "منصة رتقاء للتعليم الذكي",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تشهد أن المتعلم: ${userProfile.name}",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "قد أتم بنجاح كافة متطلبات الدورة التدريبية:\n\"تطوير تطبيقات الذكاء الاصطناعي بلغة كوتلن\"",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "رمز التوثيق:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text(text = "RTQ-2026-AI-8891", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showCertificateDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("إغلاق")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Achievements List
            Text(
                text = stringResource(R.string.achievements_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            AchievementItem(stringResource(R.string.achievement_ai_pioneer), stringResource(R.string.achievement_ai_pioneer_desc), "+200 XP", true)
            AchievementItem(stringResource(R.string.achievement_consistent), stringResource(R.string.achievement_consistent_desc), "+150 XP", true)
            AchievementItem(stringResource(R.string.achievement_quiz_master), stringResource(R.string.achievement_quiz_master_desc), "+300 XP", false)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AchievementItem(title: String, desc: String, reward: String, unlocked: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "وسام الإنجاز",
                tint = if (unlocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = reward, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
}
