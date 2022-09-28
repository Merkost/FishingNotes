package com.mobileprism.fishing.ui.custom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun FishingOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    errorString: String? = null,
) {
    Column(modifier = Modifier) {
        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(12.dp),
            maxLines = maxLines,
            label = {
                placeholder?.let {
                    Text(
                        text = it,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = maxLines,
                    )
                }
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            enabled = enabled,
            singleLine = singleLine,
            readOnly = readOnly,
            isError = isError,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            interactionSource = interactionSource,
            colors = colors,
            textStyle = textStyle,
        )
        AnimatedVisibility(visible = errorString.isNullOrBlank().not()) {
            Text(
                text = errorString ?: "",
                color = MaterialTheme.colors.error,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

