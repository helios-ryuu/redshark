package com.helios.redshark.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.redshark.R
import com.helios.redshark.ui.common.AvatarImage

private val PREDEFINED_SKILLS = listOf(
    "Android", "iOS", "Web", "Backend", "UI/UX", "ML/AI",
    "DevOps", "Data", "Security", "Product",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileEditScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var displayName by rememberSaveable { mutableStateOf(uiState.user?.displayName ?: "") }
    var bio by rememberSaveable { mutableStateOf(uiState.user?.bio ?: "") }
    val selectedSkills = remember { mutableStateListOf<String>().also { list ->
        uiState.user?.skills?.let { list.addAll(it) }
    }}

    // Sync initial values once user loads
    LaunchedEffect(uiState.user) {
        if (displayName.isBlank() && uiState.user != null) {
            displayName = uiState.user!!.displayName
            bio = uiState.user!!.bio ?: ""
            selectedSkills.clear()
            selectedSkills.addAll(uiState.user!!.skills)
        }
    }

    LaunchedEffect(uiState.savedSuccess) {
        if (uiState.savedSuccess) {
            viewModel.onSavedHandled()
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onUploadAvatar(context, userId, it) }
    }

    val isNameValid = displayName.trim().length in 3..50
    val isBioValid = bio.trim().length <= 280

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar with upload trigger
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(96.dp))
                } else {
                    AvatarImage(
                        avatarUrl = uiState.user?.avatarUrl,
                        displayName = displayName.ifBlank { uiState.user?.displayName ?: "" },
                        size = 96.dp,
                    )
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.profile_avatar_change_cd),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = stringResource(R.string.profile_avatar_change_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { if (it.length <= 50) displayName = it },
                label = { Text(stringResource(R.string.profile_field_display_name)) },
                supportingText = {
                    Text(
                        stringResource(R.string.auth_display_name_helper, displayName.trim().length),
                        color = if (!isNameValid && displayName.isNotEmpty())
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                isError = displayName.isNotEmpty() && !isNameValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 280) bio = it },
                label = { Text(stringResource(R.string.profile_field_bio)) },
                supportingText = {
                    Text(
                        stringResource(R.string.profile_bio_helper, bio.trim().length),
                        color = if (!isBioValid)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                isError = !isBioValid,
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.profile_field_skills),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                PREDEFINED_SKILLS.forEach { skill ->
                    FilterChip(
                        selected = skill in selectedSkills,
                        onClick = {
                            if (skill in selectedSkills) selectedSkills.remove(skill)
                            else selectedSkills.add(skill)
                        },
                        label = { Text(skill) },
                        modifier = Modifier.padding(end = 8.dp, bottom = 4.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.onSaveProfile(userId, displayName, bio, selectedSkills.toList())
                    },
                    enabled = isNameValid && isBioValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}
