package com.mobileprism.fishing.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileprism.fishing.ui.theme.primaryBlueLightColorTransparent
import com.mobileprism.fishing.ui.theme.secondaryFigmaTextColor

@Composable
fun SimpleOutlinedTextField(
    modifier: Modifier = Modifier,
    textState: MutableState<String>,
    label: String,
    singleLine: Boolean = true,
) {
    var text by rememberSaveable { textState }
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = text,
        onValueChange = { text = it },
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next
        ),
        singleLine = singleLine
    )
}

@Composable
fun SimpleUnderlineTextField(
    modifier: Modifier = Modifier,
    text: String,
    label: String = "",
    singleLine: Boolean = true,
    trailingIcon: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    onClick: () -> Unit = { },
    helperText: String? = null
) {
    val darkTheme = isSystemInDarkTheme()
    Column(modifier = modifier.clickable { onClick() }) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp, start = 16.dp),
                textAlign = TextAlign.Start,
                color = if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
                style = MaterialTheme.typography.body2,
            )
        }
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            readOnly = true,
            value = text,
            textStyle = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = primaryBlueLightColorTransparent,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            onValueChange = { },
            shape = RoundedCornerShape(24.dp),
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            interactionSource = remember { MutableInteractionSource() }
                .also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                onClick()
                            }
                        }
                    }
                }
        )
        helperText?.let {
            SecondaryTextColored(
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .align(Alignment.End),
                text = it
            )
        }
    }
}