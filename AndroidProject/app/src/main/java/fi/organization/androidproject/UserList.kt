package fi.organization.androidproject

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

/**
 *
 * Composable function that displays a user card with information about a person.
 *
 * @param userList The person-list containing all the user information.
 * @param onDelete Callback function invoked when the user card is deleted.
 * @param onEdit Callback function invoked when the user card is edited.
 */
@Composable
fun UserList(userList: List<Person>, onDelete: (Int) -> Unit, onEdit: (Int, String, String, String, String, Int) -> Unit) {
    LazyColumn() {
        items(userList) { user ->
            UserCard(person = user, onEdit = onEdit, onDelete = { id ->
                onDelete(id)
            })
        }
    }
}