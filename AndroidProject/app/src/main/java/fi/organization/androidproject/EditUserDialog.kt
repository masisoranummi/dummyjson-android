package fi.organization.androidproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditUserDialog(onEditCanceled: () -> Unit, onDelete: (Int) -> Unit, person: Person, onEditConfirmed: (Int, String, String, String, String, Int) -> Unit){

    var firstNameToAdd by remember { mutableStateOf(person.firstName!!) }
    var lastNameToAdd by remember { mutableStateOf(person.lastName!!) }
    var phoneToAdd by remember { mutableStateOf(person.phone!!) }
    var emailToAdd by remember { mutableStateOf(person.email!!) }
    var ageToAdd by remember { mutableStateOf(person.age) }

    var ageInput by remember { mutableStateOf(person.age.toString()) }

    AlertDialog(
        onDismissRequest = onEditCanceled,
        title = { Text("Edit user") },
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
                    value = ageInput,
                    onValueChange = {
                        ageInput = it
                        if (it.isEmpty()) {
                            ageToAdd = 0 // Default value when empty
                        } else if (it.matches(Regex("^\\d+\$"))) {
                            ageToAdd = it.toInt()
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
                    onValueChange = { phoneToAdd = it },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "Phone") }
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
                    onEditConfirmed(person.id,firstNameToAdd, lastNameToAdd, phoneToAdd, emailToAdd, ageToAdd)
                    onEditCanceled()
                }
            ) {
                Text("Edit user")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDelete(person.id)
                }
            ) {
                Text("Delete user")
            }
        }
    )
}