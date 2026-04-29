package life.ubiqui.support

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Floating support button + ticket sheet. Use as an overlay:
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     YourScreen()
 *     SupportFAB(
 *         client = supportClient,
 *         modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
 *     )
 * }
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportFAB(
    client: SupportClient,
    modifier: Modifier = Modifier,
) {
    var sheetOpen by remember { mutableStateOf(false) }

    FloatingActionButton(
        onClick = { sheetOpen = true },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.SupportAgent,
            contentDescription = stringResource(R.string.support_report_issue),
        )
    }

    if (sheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { sheetOpen = false }, sheetState = sheetState) {
            SupportTicketSheetContent(client = client, onDone = { sheetOpen = false })
        }
    }
}

@Composable
private fun SupportTicketSheetContent(client: SupportClient, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(emptyList<SupportLookupItem>()) }
    var priorities by remember { mutableStateOf(emptyList<SupportLookupItem>()) }
    var category by remember { mutableStateOf<SupportLookupItem?>(null) }
    var priority by remember { mutableStateOf<SupportLookupItem?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            categories = client.categories()
            priorities = client.priorities()
        }.onFailure { error = it.localizedMessage }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        if (done) {
            Text(stringResource(R.string.support_success), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.support_success_detail), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.support_close))
            }
            return@Column
        }

        Text(stringResource(R.string.support_report_issue), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.support_title)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.support_description)) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            minLines = 4,
        )

        if (categories.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            DropdownPicker(
                label = stringResource(R.string.support_category),
                options = categories,
                selected = category,
                onSelect = { category = it },
            )
        }
        if (priorities.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            DropdownPicker(
                label = stringResource(R.string.support_priority),
                options = priorities,
                selected = priority,
                onSelect = { priority = it },
            )
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.support_cancel))
            }
            Button(
                onClick = {
                    submitting = true
                    scope.launch {
                        runCatching {
                            client.createTicket(
                                SupportTicketDraft(
                                    title = title,
                                    description = description,
                                    categoryId = category?.id,
                                    priorityId = priority?.id,
                                ),
                            )
                        }.onSuccess { done = true }
                            .onFailure { error = it.localizedMessage }
                        submitting = false
                    }
                },
                enabled = !submitting && title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.support_submit))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownPicker(
    label: String,
    options: List<SupportLookupItem>,
    selected: SupportLookupItem?,
    onSelect: (SupportLookupItem?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    },
                )
            }
        }
    }
}
