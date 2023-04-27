package com.mobileprism.fishing.ui.custom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobileprism.fishing.R

@Composable
fun FishingOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textFieldModifier: Modifier = Modifier,
    mainModifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorString: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
) {
    Column(modifier = mainModifier) {
        OutlinedTextField(
            modifier = textFieldModifier,
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
                style = MaterialTheme.typography.body2,
                text = errorString ?: "",
                color = MaterialTheme.colors.error,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}


@Composable
fun FishingPasswordTextField(
    modifier: Modifier = Modifier,
    password: String,
    onValueChange: (String) -> Unit,
    placeholder: String = stringResource(R.string.password),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Password
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorString: String? = null,
    showPassword: Boolean = true,
    onShowPasswordChanged: (() -> Unit)? = null,
) {

    FishingOutlinedTextField(
        textFieldModifier = modifier,
        value = password,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        enabled = enabled,
        isError = isError,
        errorString = errorString,
        singleLine = true,
        visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        leadingIcon = { Icon(Icons.Default.Password, Icons.Default.Password.name) },
        trailingIcon = {
            if (onShowPasswordChanged != null && password.isNotEmpty())
                Crossfade(targetState = showPassword) { show ->
                    when (show) {
                        true -> IconButton(onClick = onShowPasswordChanged) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                Icons.Default.VisibilityOff.name
                            )
                        }
                        else -> IconButton(onClick = onShowPasswordChanged) {
                            Icon(
                                Icons.Default.Visibility,
                                Icons.Default.Visibility.name
                            )
                        }
                    }
                }
        }
    )
}
