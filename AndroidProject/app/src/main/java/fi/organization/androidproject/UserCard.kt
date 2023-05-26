package fi.organization.androidproject

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun UserCard(person: Person, onDelete: (Int) -> Unit ,onEdit: (Int, String, String, String, String, Int) -> Unit) {
    var opened by remember { mutableStateOf(false) }
    if(opened){
        EditUserDialog(onEditCanceled = {
            opened = !opened
        },
            person = person, onEditConfirmed = onEdit, onDelete = { id ->
                onDelete(id)
            })
    }
    Box(modifier = Modifier
        .padding(all = 10.dp)
        .border(width = 3.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
        .padding(all = 10.dp)
        .fillMaxWidth()
        .height(80.dp)
    ) {
        Row {
            AsyncImage(
                model = person.image,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(70.dp)
            )
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.height(80.dp)) {
                person.firstName?.let { Text(text = it) }
                Spacer(modifier = Modifier.height(4.dp))
                person.lastName?.let { Text(text = it) }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.height(80.dp)) {
                person.phone?.let { Text(text = "Phone: $it") }
                Spacer(modifier = Modifier.height(4.dp))
                person.email?.let { Text(text = "Email: $it") }
            }
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.placeRelative(constraints.maxWidth - placeable.width, 0)
                        }
                    }
                    .size(24.dp)
            ) {
                IconButton(
                    onClick = {
                        opened = !opened
                        println(opened)
                    }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
            }
        }
    }

}