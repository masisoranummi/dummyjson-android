package fi.organization.androidproject

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

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