package com.joesemper.fishing.compose.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joesemper.fishing.compose.ui.theme.primaryBlueLightColorTransparent
import com.joesemper.fishing.compose.ui.theme.secondaryFigmaTextColor

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
    trailingIcon: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    onClick: () -> Unit = { },
    helperText: String? = null
) {
    val darkTheme = isSystemInDarkTheme()
    Column(modifier = modifier.clickable { onClick() }) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, start = 16.dp),
            textAlign = TextAlign.Start,
            color = if (darkTheme) Color.LightGray else secondaryFigmaTextColor,
            style = MaterialTheme.typography.body2,
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
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
            singleLine = true,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon
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