package com.apptolast.login.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.apptolast.customlogin.domain.model.UserSession
import login.composeapp.generated.resources.Res
import login.composeapp.generated.resources.cd_profile_icon_default
import login.composeapp.generated.resources.cd_profile_picture
import login.composeapp.generated.resources.profile_screen_default_username
import login.composeapp.generated.resources.profile_screen_email_not_verified
import login.composeapp.generated.resources.profile_screen_email_verified
import login.composeapp.generated.resources.profile_screen_option_account_details_subtitle
import login.composeapp.generated.resources.profile_screen_option_account_details_title
import login.composeapp.generated.resources.profile_screen_option_security_subtitle
import login.composeapp.generated.resources.profile_screen_option_security_title
import login.composeapp.generated.resources.profile_screen_option_settings_subtitle
import login.composeapp.generated.resources.profile_screen_option_settings_title
import login.composeapp.generated.resources.profile_screen_sign_out_button
import login.composeapp.generated.resources.profile_screen_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.profile_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.userSession != null -> {
                ProfileContent(
                    modifier = Modifier.padding(paddingValues),
                    userSession = uiState.userSession!!,
                    onAction = viewModel::onAction
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    userSession: UserSession,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaultUsername = stringResource(Res.string.profile_screen_default_username)
    val verifiedText = stringResource(Res.string.profile_screen_email_verified)
    val notVerifiedText = stringResource(Res.string.profile_screen_email_not_verified)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER: PHOTO AND NAME ---
        Box(contentAlignment = Alignment.BottomEnd) {
            ProfileImage(
                photoUrl = userSession.photoUrl,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        // Edit badge
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            tonalElevation = 4.dp
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = userSession.displayName ?: defaultUsername,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = userSession.email.orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        // --- CARD: VERIFICATION STATUS ---
        val statusColor = if (userSession.isEmailVerified) Color(0xFF2E7D32) else Color(0xFFC62828)
        val bgColor =
            if (userSession.isEmailVerified) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = bgColor
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (userSession.isEmailVerified) Icons.Default.VerifiedUser else Icons.Default.Warning,
                    contentDescription = null,
                    tint = statusColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (userSession.isEmailVerified) verifiedText else notVerifiedText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // --- OPTIONS SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileOptionItem(
                    icon = Icons.Default.Person,
                    title = stringResource(Res.string.profile_screen_option_account_details_title),
                    subtitle = stringResource(Res.string.profile_screen_option_account_details_subtitle)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                ProfileOptionItem(
                    icon = Icons.Default.Settings,
                    title = stringResource(Res.string.profile_screen_option_settings_title),
                    subtitle = stringResource(Res.string.profile_screen_option_settings_subtitle)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp
                )
                ProfileOptionItem(
                    icon = Icons.Default.Security,
                    title = stringResource(Res.string.profile_screen_option_security_title),
                    subtitle = stringResource(Res.string.profile_screen_option_security_subtitle)
                )
            }
        }
        // Flexible spacer to push the button down
        Spacer(modifier = Modifier.height(32.dp))
        // --- SIGN OUT BUTTON ---
        Button(
            onClick = { onAction(ProfileAction.SignOutClicked) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Text(
                text = stringResource(Res.string.profile_screen_sign_out_button),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileOptionItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector =
                Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * Profile image component with proper loading and error states using Coil.
 */
@Composable
private fun ProfileImage(
    photoUrl: String?,
    modifier: Modifier = Modifier,
) {
    val hasValidUrl = !photoUrl.isNullOrBlank()
    val profilePictureDescription = stringResource(Res.string.cd_profile_picture)

    if (hasValidUrl) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = profilePictureDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            error = {
                ProfilePlaceholder()
            }
        )
    } else {
        ProfilePlaceholder(modifier = modifier)
    }
}

/**
 * Default placeholder shown when no profile image is available or on error.
 */
@Composable
private fun ProfilePlaceholder(modifier: Modifier = Modifier) {
    val defaultIconDescription = stringResource(Res.string.cd_profile_icon_default)

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = defaultIconDescription,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
