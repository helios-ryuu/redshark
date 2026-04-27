package com.helios.redshark.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.auth.AuthViewModel
import com.helios.redshark.ui.common.AvatarImage
import com.helios.redshark.ui.theme.Dimens
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit,
    onNavigateToIdea: (UUID) -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOpenIdeaDialog by remember { mutableStateOf(false) }
    var ideaIdInput by remember { mutableStateOf("") }
    var ideaIdError by remember { mutableStateOf(false) }

    if (showOpenIdeaDialog) {
        AlertDialog(
            onDismissRequest = { showOpenIdeaDialog = false; ideaIdInput = ""; ideaIdError = false },
            title = { Text(stringResource(R.string.settings_open_deleted_idea)) },
            text = {
                OutlinedTextField(
                    value = ideaIdInput,
                    onValueChange = { ideaIdInput = it; ideaIdError = false },
                    label = { Text(stringResource(R.string.settings_idea_uuid_label)) },
                    isError = ideaIdError,
                    supportingText = {
                        if (ideaIdError) Text(
                            stringResource(R.string.settings_invalid_uuid),
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val raw = ideaIdInput.trim().removePrefix("redshark://idea/")
                        val uuid = UUID.fromString(raw)
                        showOpenIdeaDialog = false
                        ideaIdInput = ""
                        onNavigateToIdea(uuid)
                    } catch (_: IllegalArgumentException) {
                        ideaIdError = true
                    }
                }) { Text(stringResource(R.string.settings_open)) }
            },
            dismissButton = {
                TextButton(onClick = { showOpenIdeaDialog = false; ideaIdInput = ""; ideaIdError = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.settings_sign_out_dialog_title)) },
            text = { Text(stringResource(R.string.settings_sign_out_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    authViewModel.onSignOutClicked()
                    onSignedOut()
                }) {
                    Text(stringResource(R.string.settings_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete_account)) },
            text = { Text(stringResource(R.string.settings_delete_account_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.SpaceLg),
        ) {
            Spacer(modifier = Modifier.height(Dimens.SpaceLg))

            uiState.user?.let { user ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AvatarImage(
                        avatarUrl = user.avatarUrl,
                        displayName = user.displayName,
                        size = Dimens.AvatarMd,
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpaceMd))
                    Column {
                        Text(text = user.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpaceXl))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Dimens.SpaceXl))
            }

            OutlinedButton(
                onClick = { showOpenIdeaDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconMd),
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text(stringResource(R.string.settings_open_deleted_idea))
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconMd),
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text(stringResource(R.string.settings_sign_out))
            }
            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(
                    Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconMd),
                )
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text(stringResource(R.string.settings_delete_account))
            }
        }
    }
}
