package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mobileprism.fishing.ui.theme.Spacing
import com.mobileprism.fishing.ui.utils.enums.StringOperation

@Composable
fun <T> SortOptionsSheet(
    title: String,
    options: List<T>,
    current: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) where T : StringOperation {
    val currentState: State<T?> = remember(current) { mutableStateOf(current) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.screenH, vertical = Spacing.lg),
    ) {
        AppText(
            text = title,
            style = AppTextStyle.Heading,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )
        ItemsSelection(
            radioOptions = options,
            currentOption = currentState,
            onSelectedItem = onSelect,
        )
    }
}
