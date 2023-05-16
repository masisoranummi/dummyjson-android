package fi.organization.androidproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URL
import kotlin.concurrent.thread
import kotlin.math.exp


@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(var firstName: String? = null, var lastName: String? = null, var age: Int = 0,
                  var email: String? = null, var phone: String? = null,
                  var id: Int = 0, var image: String? = null, var isDeleted: Boolean = false)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DummyJsonObject(var users: MutableList<Person>? = null)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column{
                MyScreen()
            }
        }
    }

    @Composable
    fun MyScreen() {
        val (done, setDone) = remember { mutableStateOf(false) }
        val (userList, setUserList) = remember { mutableStateOf(emptyList<Person>()) }
        val (deletedUsers, setDeletedUsers) = remember { mutableStateOf(emptyList<Int>()) }
        val (addedUsers, setAddedUsers) = remember { mutableStateOf(emptyList<Person>()) }
        var firstNameToDelete by remember { mutableStateOf("") }
        var lastNameToDelete by remember { mutableStateOf("") }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                BottomAppBar(
                    backgroundColor = MaterialTheme.colors.primary,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add User") },
                            label = { Text("Add", maxLines = 1) },
                            onClick = { showAddDialog = true },
                            selected = true
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Delete, contentDescription = "Delete User") },
                            label = { Text("Delete", maxLines = 1) },
                            onClick = {
                                showDeleteDialog = true
                                      },
                            selected = true
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Get all users") },
                            label = { Text("Get all", maxLines = 1) },
                            onClick = {
                                setDone(false)
                                fetchAll(setDone, setUserList, deletedUsers, addedUsers)
                            },
                            selected = true
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if(done) {
                        SearchButton(onSearch = { searchTerm ->
                            searchUsers(searchTerm, setUserList, setDone, deletedUsers, addedUsers)
                        })
                    }
                    if (done) {
                        UserList(userList)
                    } else {
                        CircularProgressIndicator()
                    }
                }
                if (showDeleteDialog) {
                    DeleteUserDialog(
                        firstName = firstNameToDelete,
                        lastName = lastNameToDelete,
                        onFirstNameChange = { firstNameToDelete = it },
                        onLastNameChange = { lastNameToDelete = it },
                        onDeleteConfirmed = {
                            var updatedUsers = emptyList<Person>()
                            var toDelete: MutableList<Int> = mutableListOf()
                            val removed = addedUsers.toMutableList().removeIf { user ->
                                user.firstName.equals(firstNameToDelete, ignoreCase = true) && user.lastName.equals(lastNameToDelete, ignoreCase = true)
                            }
                            if (removed) {
                                println("removed from added")
                                setAddedUsers(addedUsers.toMutableList().apply {
                                    removeIf { user ->
                                        user.firstName.equals(firstNameToDelete, ignoreCase = true) && user.lastName.equals(lastNameToDelete, ignoreCase = true)
                                    }
                                })
                                updatedUsers = addedUsers.filter { user ->
                                    !(user.firstName.equals(firstNameToDelete, ignoreCase = true) && user.lastName.equals(lastNameToDelete, ignoreCase = true))
                                }
                            } else {
                                userList.forEach{
                                    if(it.firstName.equals(firstNameToDelete, ignoreCase = true) && it.lastName.equals(lastNameToDelete, ignoreCase = true)){
                                        val client = OkHttpClient()
                                        thread {
                                            val request = Request.Builder()
                                                .url("https://dummyjson.com/users/${it.id}")
                                                .delete()
                                                .build()
                                            val response = client.newCall(request).execute()
                                            val responseBody = response.body?.string()
                                            println(responseBody)
                                            val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                                            /* TODO: add toast */
                                            println("${newPerson.firstName} ${newPerson.lastName} deleted")
                                        }
                                        toDelete.add(it.id)
                                    }
                                }
                            }
                            setDone(false)
                            if(removed){
                                fetchAll(setDone, setUserList, deletedUsers + toDelete, updatedUsers)
                            } else {
                                fetchAll(setDone, setUserList, deletedUsers + toDelete, addedUsers)
                            }
                            setDeletedUsers(deletedUsers + toDelete)
                            firstNameToDelete = ""
                            lastNameToDelete = ""
                            showDeleteDialog = false
                        },
                        onDeleteCanceled = { showDeleteDialog = false }
                    )
                }
                if (showAddDialog) {
                    AddUserDialog(onAddCanceled = { showAddDialog = false }) { first, last, age, phone, email ->
                        thread {
                            setDone(false)
                            showAddDialog = false
                            val client = OkHttpClient()
                            val form = FormBody.Builder()
                                .add("firstName", first)
                                .add("lastName", last)
                                .add("age", age)
                                .add("phone", phone)
                                .add("email", email)
                                .add("image", "https://robohash.org/${first+last}.png?set=set1")
                                .build()
                            val request = Request.Builder()
                                .url("https://dummyjson.com/users/add")
                                .post(form)
                                .build()
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                            /* TODO: add toast */
                            println(newPerson)
                            fetchAll(setDone, setUserList, deletedUsers, addedUsers + newPerson)
                            setAddedUsers(addedUsers + newPerson)
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            fetchAll(setDone, setUserList, deletedUsers, addedUsers)
        }
    }

    @Composable
    fun DeleteUserDialog(
        firstName: String,
        lastName: String,
        onFirstNameChange: (String) -> Unit,
        onLastNameChange: (String) -> Unit,
        onDeleteConfirmed: () -> Unit,
        onDeleteCanceled: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDeleteCanceled,
            title = { Text("Delete user") },
            text = {
                Column {
                    Text("Enter the user's first and last name to delete", modifier = Modifier.padding(10.dp))
                    TextField(
                        value = firstName,
                        onValueChange = onFirstNameChange,
                        modifier = Modifier.padding(10.dp),
                        label = { Text(text = "First name") }
                    )
                    TextField(
                        value = lastName,
                        onValueChange = onLastNameChange,
                        modifier = Modifier.padding(10.dp),
                        label = { Text(text = "Last name") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                            onDeleteConfirmed()
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = onDeleteCanceled
                ) {
                    Text("Cancel")
                }
            }
        )
    }

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
                        if (firstNameToAdd.isNotEmpty() && lastNameToAdd.isNotEmpty()
                            && phoneToAdd.isNotEmpty() && emailToAdd.isNotEmpty()) {
                            onAddConfirmed(firstNameToAdd, lastNameToAdd, ageToAdd, phoneToAdd, emailToAdd)
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

    private fun fetchAll(setDone: (Boolean) -> Unit, setUserList: (List<Person>) -> Unit, deletedUsers: List<Int>, addedUsers: List<Person>) {
        thread {
            println(deletedUsers)
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://dummyjson.com/users")
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            val mp = ObjectMapper()
            val myObject: DummyJsonObject = mp.readValue(responseBody, DummyJsonObject::class.java)
            var persons: MutableList<Person>? = myObject.users
            if (persons != null) {
                persons.addAll(addedUsers)
                persons.removeIf { it.id in deletedUsers }
                runOnUiThread {
                    setUserList(persons)
                }
            }
            response.close()
            runOnUiThread {
                setDone(true)
            }

        }
    }


    @Composable
    fun UserList(userList: List<Person>) {
        LazyColumn() {
            items(userList) { user ->
                UserCard(person = user)
            }
        }
    }

    @Composable
    fun SearchButton(onSearch: (String) -> Unit) {
        var expanded by remember { mutableStateOf(false) }
        var searchTerm by remember { mutableStateOf("") }

        SimpleButton(buttonText = "Search users") {
            expanded = !expanded
        }
        if (expanded) {
            Row {
                TextField(value = searchTerm, onValueChange = {
                    searchTerm = it
                },
                label = { Text(text = "Search by name or email") })
                SimpleButton(buttonText = "Start Search") {
                    println("Searching for $searchTerm")
                    onSearch(searchTerm)
                    expanded = false
                }
            }
        }
    }



    fun searchUsers(searchTerm: String, setUsers: (List<Person>) -> Unit, setDone: (Boolean) -> Unit, deletedUsers: List<Int>, addedUsers: List<Person>){
        setDone(false)
        thread {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://dummyjson.com/users/search?q=$searchTerm")
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            val mp = ObjectMapper()
            val myObject: DummyJsonObject = mp.readValue(responseBody, DummyJsonObject::class.java)
            val persons: MutableList<Person>? = myObject.users
            if (persons != null) {
                addedUsers.forEach{
                    if(it.firstName!!.contains(searchTerm) || it.lastName!!.contains(searchTerm)
                        || it.email!!.contains(searchTerm)) {
                        persons.add(it)
                    }
                }
                persons.removeIf { it.id in deletedUsers }
                runOnUiThread {
                    setUsers(persons)
                }
                persons.forEach {
                    println("${it.firstName} ${it.lastName}")
                }
            }
            response.close()
            runOnUiThread {
                setDone(true)
            }

        }
    }
}




// Rip in peace. Trying to make this popup work took way too much out of me
// mentally and physically, I'll leave this here to remind myself that
// making things look nice isn't worth it, especially in android because
// nothing actually works.

/*
@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchPopUp(){

    var textFieldSize by remember { mutableStateOf(Size.Zero)}
    val (popup, setPopup) = remember { mutableStateOf(false) }
    val options = listOf("First name", "Last name", "Age", "Gender")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }
    var searchText by remember { mutableStateOf("") }
    var isTextFieldFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    SimpleButton(buttonText = "Search users") {
       setPopup(true)
    }

    if (popup) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = {
                setPopup(false)
            },
            properties = PopupProperties(dismissOnBackPress = true)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(225.dp)
                .background(color = Color.White)
                .border(width = 3.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))) {

                // Create an Outlined Text Field
                // with icon and not expanded
                OutlinedTextField(
                    value = selectedOptionText,
                    readOnly = true,
                    onValueChange = {  },
                    modifier = Modifier
                        .padding(PaddingValues(top = 10.dp))
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally)
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        },
                    label = {Text("Search options")},
                    trailingIcon = {
                        Icon(icon,"contentDescription",
                            Modifier.clickable { expanded = !expanded })
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(with(LocalDensity.current){textFieldSize.width.toDp()})
                ) {
                    options.forEach { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 50.dp)
                        ) {
                            DropdownMenuItem(onClick = {
                                selectedOptionText = label
                                expanded = false
                            }) {
                                Text(text = label)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
/*
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally),
                    label = {Text(selectedOptionText)},
                )
 */

                TextField(value = searchText, onValueChange = {searchText = it}, modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
                    .onFocusChanged {isFocused ->
                        if (isFocused.isFocused) {
                            println(isFocused.isFocused)
                            setPopup(true)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SimpleButton(buttonText = "Start search", offset = 0.9f) {
                    println("$searchText from $selectedOptionText")

                }
            }
        }
    }
}
*/

@Composable
fun UserCard(person: Person) {
    Row(modifier = Modifier
        .padding(all = 10.dp)
        .border(width = 3.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
        .padding(all = 10.dp)
        .fillMaxWidth()
        .height(80.dp)
    ) {
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
    }
}



@Composable
fun SimpleButton(buttonText: String, offset: Float = 1.0f, onButtonClick: () -> Unit) {
    Button(onClick = onButtonClick, modifier = Modifier
        .padding(start = 16.dp * offset, end = 16.dp)
        .fillMaxWidth())
    {
        Text(text = buttonText)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

}