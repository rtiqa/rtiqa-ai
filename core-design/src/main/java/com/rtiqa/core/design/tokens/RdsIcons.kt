package com.rtiqa.core.design.tokens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rtiqa Design System Standardized System Icons
 */
object RdsIcons {
    // Navigation
    val Back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val Forward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val Menu: ImageVector = Icons.Default.Menu
    val Home: ImageVector = Icons.Default.Home
    val Courses: ImageVector = Icons.Default.Book
    val Quiz: ImageVector = Icons.Default.Quiz
    val Profile: ImageVector = Icons.Default.Person
    val Settings: ImageVector = Icons.Default.Settings
    val Admin: ImageVector = Icons.Default.AdminPanelSettings

    // AI & Intelligence
    val AiSparkle: ImageVector = Icons.Default.AutoAwesome

    // Offline & Sync
    val Offline: ImageVector = Icons.Default.SignalCellularConnectedNoInternet0Bar
    val Download: ImageVector = Icons.Default.Download
    val Downloaded: ImageVector = Icons.Default.DownloadDone

    // Feedback & System
    val Success: ImageVector = Icons.Default.CheckCircle
    val Error: ImageVector = Icons.Default.Error
    val Warning: ImageVector = Icons.Default.Warning
    val Info: ImageVector = Icons.Default.Info
    val Check: ImageVector = Icons.Default.Check
    val Close: ImageVector = Icons.Default.Close
    val Lock: ImageVector = Icons.Default.Lock
    val Search: ImageVector = Icons.Default.Search
    val List: ImageVector = Icons.AutoMirrored.Filled.List
}
