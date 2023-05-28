package fi.organization.androidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

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
        val (editedUsers, setEditedUsers) = remember { mutableStateOf(emptyList<Person>()) }
        var showSearchDialog by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                BottomAppBar {
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
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search User") },
                            label = { Text("Search", maxLines = 1) },
                            onClick = { showSearchDialog = true },
                            selected = true
                        )
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Get all users") },
                            label = { Text("Get all", maxLines = 1) },
                            onClick = {
                                setDone(false)
                                fetchAll(setDone, setUserList, deletedUsers, addedUsers, editedUsers)
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
                    if (done) {
                        UserList(userList, onDelete = {id ->
                            setDone(false)
                            val addedUsersList = addedUsers.toMutableList()
                            val userDeleted = addedUsersList.removeIf { addedUser ->
                                addedUser.id == id
                            }
                            if(userDeleted) {
                                runOnUiThread {
                                    setAddedUsers(addedUsersList)
                                }
                                fetchAll(setDone, setUserList, deletedUsers, addedUsersList, editedUsers)
                            } else {
                                val client = OkHttpClient()
                                thread {
                                    val request = Request.Builder()
                                        .url("https://dummyjson.com/users/${id}")
                                        .delete()
                                        .build()
                                    val response = client.newCall(request).execute()
                                    val responseBody = response.body?.string()
                                    println(responseBody)
                                    val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                                    /* TODO: add toast */
                                    println("${newPerson.firstName} ${newPerson.lastName} deleted")
                                    fetchAll(setDone, setUserList, deletedUsers + newPerson.id, addedUsers, editedUsers)
                                    setDeletedUsers(deletedUsers + newPerson.id)
                                }
                            }
                        }){ id, firstName, lastName, phone, email, age ->
                            setDone(false)
                            var edited = false

                            for (addedUser in addedUsers) {
                                if(addedUser.id == id){
                                    addedUser.firstName = firstName
                                    addedUser.lastName = lastName
                                    addedUser.phone = phone
                                    addedUser.email = email
                                    addedUser.age = age
                                    edited = true
                                    println(addedUser)
                                    fetchAll(setDone, setUserList, deletedUsers, addedUsers, editedUsers)
                                }
                            }

                            if(!edited){
                                thread {
                                    val client = OkHttpClient()
                                    val form = FormBody.Builder()
                                        .add("firstName", firstName)
                                        .add("lastName", lastName)
                                        .add("age", age.toString())
                                        .add("phone", phone)
                                        .add("email", email)
                                        .build()
                                    val request = Request.Builder()
                                        .url("https://dummyjson.com/users/${id}")
                                        .put(form)
                                        .build()
                                    val response = client.newCall(request).execute()
                                    val responseBody = response.body?.string()
                                    val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                                    println(newPerson)
                                    fetchAll(setDone, setUserList, deletedUsers, addedUsers, editedUsers + newPerson)
                                    setEditedUsers(editedUsers + newPerson)
                                }
                            }
                        }
                    } else {
                        CircularProgressIndicator()
                    }
                }
                if (showSearchDialog){
                    SearchUserDialog(onSearchCanceled = { showSearchDialog = false }) { searchTerm ->
                        setDone(false)
                        showSearchDialog = false
                        searchUsers(searchTerm, setUserList, setDone, deletedUsers, addedUsers, editedUsers)
                    }
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
                            fetchAll(setDone, setUserList, deletedUsers, addedUsers + newPerson, editedUsers)
                            setAddedUsers(addedUsers + newPerson)
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            fetchAll(setDone, setUserList, deletedUsers, addedUsers, editedUsers)
        }
    }

    @Composable
    fun SearchUserDialog(onSearchCanceled: () -> Unit,onSearchConfirmed: (String) -> Unit){
        var searchTerm by remember { mutableStateOf("") }

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

    private fun fetchAll(setDone: (Boolean) -> Unit, setUserList: (List<Person>) -> Unit,
                         deletedUsers: List<Int>, addedUsers: List<Person>, editedUsers: List<Person>) {
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
            val persons: MutableList<Person>? = myObject.users
            if (persons != null) {
                persons.addAll(addedUsers)

                for (editedUser in editedUsers) {
                    val matchingPersonIndex = persons.indexOfFirst { it.id == editedUser.id }
                    if (matchingPersonIndex != -1) {
                        persons[matchingPersonIndex] = editedUser
                    }
                }

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

    private fun searchUsers(searchTerm: String, setUsers: (List<Person>) -> Unit, setDone: (Boolean) -> Unit,
                    deletedUsers: List<Int>, addedUsers: List<Person>, editedUsers: List<Person>){
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

                persons.removeIf { person ->
                    editedUsers.any { editedUser ->
                        editedUser.id == person.id
                    }
                }

                editedUsers.forEach{
                    if(it.firstName!!.contains(searchTerm, ignoreCase = true) || it.lastName!!.contains(searchTerm, ignoreCase = true)
                        || it.email!!.contains(searchTerm, ignoreCase = true)) {
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