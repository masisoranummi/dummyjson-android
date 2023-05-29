package fi.organization.androidproject

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

/**
 * Composable function for the search user dialog.
 *
 * @param onAddCanceled Callback when adding user is canceled.
 * @param onAddConfirmed Callback when adding user is confirmed with a non-empty fields.
 */
@Composable
fun AddUserDialog(
    onAddCanceled: () -> Unit,
    onAddConfirmed: (String, String, String, String, String) -> Unit
) {
    var firstNameToAdd by remember { mutableStateOf("") }
    var lastNameToAdd by remember { mutableStateOf("") }
    var phoneToAdd by remember { mutableStateOf("") }
    var emailToAdd by remember { mutableStateOf("") }
    var ageToAdd by remember { mutableStateOf("") }
    val context = LocalContext.current


    AlertDialog(
        onDismissRequest = onAddCanceled,
        title = { Text("Add user") },
        text = {
            Column {
                Text("Enter the users information", modifier = Modifier.padding(10.dp))
                TextField(
                    value = firstNameToAdd,
                    onValueChange = { firstNameToAdd = it },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "First name") }
                )
                TextField(
                    value = lastNameToAdd,
                    onValueChange = { lastNameToAdd = it },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "Last name") }
                )
                TextField(
                    value = ageToAdd,
                    onValueChange = {
                        // Doesn't let user put anything other than numbers to
                        // the age-field
                        if (it.isEmpty() || it.matches(Regex("^\\d+\$"))) {
                            ageToAdd = it
                        }
                    },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "Age") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword
                    )
                )
                TextField(
                    value = phoneToAdd,
                    onValueChange = {
                        if (it.matches(Regex("^[+\\d\\s]+\$")) || it.isEmpty()) { // Only digits, + or space
                            phoneToAdd = it
                        }
                    },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "Phone") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                )
                TextField(
                    value = emailToAdd,
                    onValueChange = { emailToAdd = it },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "Email") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstNameToAdd.isNotEmpty() && lastNameToAdd.isNotEmpty()
                        && phoneToAdd.isNotEmpty() && emailToAdd.isNotEmpty()) {
                        onAddConfirmed(firstNameToAdd, lastNameToAdd, ageToAdd, phoneToAdd, emailToAdd)
                    } else {
                        Toast.makeText(
                            context,
                            "One or more of the fields are empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = onAddCanceled
            ) {
                Text("Cancel")
            }
        }
    )
}



