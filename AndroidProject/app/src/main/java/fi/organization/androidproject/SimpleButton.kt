package fi.organization.androidproject

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SimpleButton(buttonText: String, offset: Float = 1.0f, onButtonClick: () -> Unit) {
    Button(onClick = onButtonClick, modifier = Modifier
        .padding(start = 16.dp * offset, end = 16.dp)
        .fillMaxWidth())
    {
        Text(text = buttonText)
    }
}