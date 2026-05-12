package com.helios.redshark.ui.profile

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileViewScreen(
    userId: String,
    currentUserId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToMessage: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId, currentUserId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.user?.displayName ?: stringResource(R.string.profile_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (uiState.isOwner) {
                        IconButton(onClick = onNavigateToEdit) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(R.string.profile_not_found), style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> uiState.user?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Hero banner with avatar overlapping its bottom edge
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.ProfileHeroBannerHeight)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer,
                                        )
                                    )
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = Dimens.ProfileAvatarOverlap)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(3.dp),
                        ) {
                            AvatarImage(
                                avatarUrl = user.avatarUrl,
                                displayName = user.displayName,
                                size = Dimens.AvatarLg,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.ProfileAvatarOverlap + Dimens.SpaceLg))

                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (!uiState.isOwner) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceLg))
                        Button(onClick = onNavigateToMessage) {
                            Text(stringResource(R.string.profile_action_message))
                        }
                    }

                    if (!user.bio.isNullOrBlank() || user.skills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceLg))
                        ElevatedCard(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.SpaceXl),
                        ) {
                            Column(modifier = Modifier.padding(Dimens.SpaceLg)) {
                                if (!user.bio.isNullOrBlank()) {
                                    Text(
                                        text = user.bio,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                                if (user.skills.isNotEmpty()) {
                                    if (!user.bio.isNullOrBlank()) Spacer(modifier = Modifier.height(Dimens.SpaceMd))
                                    Text(
                                        text = stringResource(R.string.profile_field_skills),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.SpaceXs))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                                    ) {
                                        user.skills.forEach { skill ->
                                            AssistChip(onClick = {}, label = { Text(skill) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceLg))
                }
            }
        }
    }
}
