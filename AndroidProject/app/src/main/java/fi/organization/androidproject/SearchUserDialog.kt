package fi.organization.androidproject

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Composable function for the search user dialog.
 *
 * @param onSearchCanceled Callback when the search is canceled.
 * @param onSearchConfirmed Callback when the search is confirmed with a non-empty search term.
 */
@Composable
fun SearchUserDialog(onSearchCanceled: () -> Unit,onSearchConfirmed: (String) -> Unit){
    var searchTerm by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onSearchCanceled,
        title = { Text("Add user") },
        text = {
            Column {
                Text("Search for users by name or email", modifier = Modifier.padding(10.dp))
                TextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    modifier = Modifier.padding(10.dp),
                    label = { Text(text = "Search users") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (searchTerm.isNotEmpty()) {
                        onSearchConfirmed(searchTerm)
                    } else {
                        Toast.makeText(
                            context,
                            "Search field is empty",
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
                onClick = onSearchCanceled
            ) {
                Text("Cancel")
            }
        }
    )
}
