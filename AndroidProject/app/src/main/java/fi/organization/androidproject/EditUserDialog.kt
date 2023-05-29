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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


/**
 * Displays a dialog for editing a user's information.
 *
 * @param onEditCanceled Callback triggered when the editing is canceled.
 * @param onDelete Callback triggered when the delete-button is pressed.
 * @param person The Person object representing the user to be edited.
 * @param onEditConfirmed Callback triggered when the editing is confirmed with the updated user information.
 */
@Composable
fun EditUserDialog(onEditCanceled: () -> Unit, onDelete: (Int) -> Unit, person: Person, onEditConfirmed: (Int, String, String, String, String, Int) -> Unit){

    // State variables for capturing user input
    var firstNameToAdd by remember { mutableStateOf(person.firstName!!) }
    var lastNameToAdd by remember { mutableStateOf(person.lastName!!) }
    var phoneToAdd by remember { mutableStateOf(person.phone!!) }
    var emailToAdd by remember { mutableStateOf(person.email!!) }
    var ageToAdd by remember { mutableStateOf(person.age) }

    var ageInput by remember { mutableStateOf(person.age.toString()) }
    val context = LocalContext.current

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
                        } else if (it.matches(Regex("^\\d+\$"))) { // Only accepts numbers
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
                    onValueChange = {
                        if (it.matches(Regex("^[+\\d\\s]+\$"))) { // Only digits, + or space
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
                    // Only calls the callback if none of the fields are empty
                    if(firstNameToAdd.isNotEmpty() && lastNameToAdd.isNotEmpty()
                        && phoneToAdd.isNotEmpty() && emailToAdd.isNotEmpty()){
                        onEditConfirmed(person.id,firstNameToAdd, lastNameToAdd, phoneToAdd, emailToAdd, ageToAdd)
                        onEditCanceled()
                    } else {
                        Toast.makeText(
                            context,
                            "One or more of the fields are empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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