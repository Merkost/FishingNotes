package com.mobileprism.fishing.ui.home.views

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import fishing.shared.generated.resources.Res
import fishing.shared.generated.resources.more_options
import org.jetbrains.compose.resources.stringResource

data class OverflowMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val leadingIcon: ImageVector? = null,
    val tint: Color = Color.Unspecified,
)

@Composable
fun AppBarOverflowMenu(
    items: List<OverflowMenuItem>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        modifier = modifier,
        onClick = { expanded = true }
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            contentDescription = stringResource(Res.string.more_options)
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        color = if (item.tint == Color.Unspecified) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            item.tint
                        }
                    )
                },
                leadingIcon = item.leadingIcon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (item.tint == Color.Unspecified) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                item.tint
                            }
                        )
                    }
                },
                onClick = {
                    expanded = false
                    item.onClick()
                }
            )
        }
    }
}
