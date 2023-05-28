package fi.organization.androidproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

/**
 * The `MainActivity` class is the entry point of the application.
 * It represents the main activity of the app and is responsible for
 * handling user interactions and managing the app's UI.
 *
 * This is the only activity in the application
 */
class MainActivity : ComponentActivity() {

    /**
     * Initializes the activity when it is created.
     *
     * @param savedInstanceState The saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column{
                MyScreen()
            }
        }
    }

    /**
     * A composable fuction for the main UI for the screen
     */
    @Composable
    fun MyScreen() {
        // Defining and initializing state variables
        val (done, setDone) = remember { mutableStateOf(false) }
        val (userList, setUserList) = remember { mutableStateOf(emptyList<Person>()) }
        val (deletedUsers, setDeletedUsers) = remember { mutableStateOf(emptyList<Int>()) }
        val (addedUsers, setAddedUsers) = remember { mutableStateOf(emptyList<Person>()) }
        val (editedUsers, setEditedUsers) = remember { mutableStateOf(emptyList<Person>()) }
        var showSearchDialog by remember { mutableStateOf(false) }
        var showAddDialog by remember { mutableStateOf(false) }

        // Context variable for Toast-messages
        val context = LocalContext.current

        // Building UI using Jetpack Compose components
        Scaffold(
            bottomBar = {
                // Bottom bar with buttons
                BottomAppBar {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Button for showing the dialog for adding users
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add User") },
                            label = { Text("Add", maxLines = 1) },
                            onClick = { showAddDialog = true },
                            selected = true
                        )
                        // Button for showing the dialog for searching users
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = "Search User") },
                            label = { Text("Search", maxLines = 1) },
                            onClick = { showSearchDialog = true },
                            selected = true
                        )
                        // Button for getting all users
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
            // This was needed for the Scaffold to work
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                // Column that shows the UserCards or the loading icon
                // depending on if the fetch is done
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (done) {
                        UserList(userList, onDelete = { id ->
                            // Handle user deletion
                            setDone(false)
                            val addedUsersList = addedUsers.toMutableList()
                            // Look through the added users list, if an user with
                            // the matching id is found, delete that user from the list
                            // and return a boolean value based on if anything was removed
                            val userDeleted = addedUsersList.removeIf { addedUser ->
                                addedUser.id == id
                            }
                            if(userDeleted) {
                                // Find the deleted user in the list of users
                                val deletedUser = userList.find { it.id == id }
                                if (deletedUser != null) {
                                    Toast.makeText(
                                        context,
                                        "User ${deletedUser.firstName!!} ${deletedUser.lastName!!} deleted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                runOnUiThread {
                                    // Put the new list with the removed users to
                                    // the added users list
                                    setAddedUsers(addedUsersList)
                                }
                                // Fetch all users
                                fetchAll(setDone, setUserList, deletedUsers, addedUsersList, editedUsers)
                            } else {
                                val client = OkHttpClient()
                                // Thread for handling the delete request
                                thread {
                                    val request = Request.Builder()
                                        .url("https://dummyjson.com/users/${id}")
                                        .delete()
                                        .build()
                                    val response = client.newCall(request).execute()
                                    val responseBody = response.body?.string()
                                    println(responseBody)
                                    // Make the result into a new Person-object
                                    val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                                    // Fetch all users with the new user
                                    fetchAll(setDone, setUserList, deletedUsers + newPerson.id, addedUsers, editedUsers)
                                    // Put the new users id to the deleted users array
                                    // after fetching everything, because trying to put it before
                                    // creates issues, because setDeletedUsers is asynchronous
                                    setDeletedUsers(deletedUsers + newPerson.id)
                                    // Inform the user that an user was deleted
                                    runOnUiThread{
                                        Toast.makeText(
                                            context,
                                            "User ${newPerson.firstName!!} ${newPerson.lastName!!} deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }){ id, firstName, lastName, phone, email, age ->
                            // Handle user editing
                            setDone(false)
                            var edited = false

                            for (addedUser in addedUsers) {
                                // If user id matches any of the ones in
                                // added users, edit that user without
                                // making a http request
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

                            // if user being edited wasn't an added user, do a proper request
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
                                    // Make a new Person-object from the response
                                    val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                                    println(newPerson)
                                    // Fetch everything again with the new edited user
                                    fetchAll(setDone, setUserList, deletedUsers, addedUsers, editedUsers + newPerson)
                                    // Solves the same async issue that deleting users had
                                    setEditedUsers(editedUsers + newPerson)
                                }
                            }
                            // Inform user with a toast that an user was edited
                            Toast.makeText(
                                context,
                                "User $firstName $lastName edited",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Show loading icon if the app is fetching data
                        CircularProgressIndicator()
                    }
                }
                if (showSearchDialog){
                    // Show dialog for searching users
                    SearchUserDialog(onSearchCanceled = { showSearchDialog = false }) { searchTerm ->
                        setDone(false)
                        showSearchDialog = false
                        searchUsers(searchTerm, setUserList, setDone, deletedUsers, addedUsers, editedUsers)
                    }
                }
                // Show dialog for adding users
                if (showAddDialog) {
                    AddUserDialog(onAddCanceled = { showAddDialog = false }) { first, last, age, phone, email ->
                        thread {
                            // Make a request, put the user based on the returned json
                            // to the added users array and fetch all users again
                            setDone(false)
                            showAddDialog = false
                            val client = OkHttpClient()
                            val form = FormBody.Builder()
                                .add("firstName", first)
                                .add("lastName", last)
                                .add("age", age)
                                .add("phone", phone)
                                .add("email", email)
                                    // New users get a new generated image based on the
                                    // users first and last name
                                .add("image", "https://robohash.org/${first+last}.png?set=set1")
                                .build()
                            val request = Request.Builder()
                                .url("https://dummyjson.com/users/add")
                                .post(form)
                                .build()
                            val response = client.newCall(request).execute()
                            val responseBody = response.body?.string()
                            val newPerson : Person = ObjectMapper().readValue(responseBody, Person::class.java)
                            println(newPerson)
                            fetchAll(setDone, setUserList, deletedUsers, addedUsers + newPerson, editedUsers)
                            setAddedUsers(addedUsers + newPerson)
                        }
                        // Inform the user that user was added
                        Toast.makeText(
                            context,
                            "User $first $last added",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Fetch initial data
        LaunchedEffect(Unit) {
            fetchAll(setDone, setUserList, deletedUsers, addedUsers, editedUsers)
        }
    }

    /**
     * Fetches the list of users from the API and updates the UI.
     *
     * @param setDone Callback to set the loading status of the UI.
     * @param setUserList Callback to update the user list in the UI.
     * @param deletedUsers List of IDs of users that were deleted.
     * @param addedUsers List of users that were added.
     * @param editedUsers List of users that were edited.
     */
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
                // Add the addedUsers to the list
                persons.addAll(addedUsers)

                // Update the edited users in the list
                for (editedUser in editedUsers) {
                    val matchingPersonIndex = persons.indexOfFirst { it.id == editedUser.id }
                    if (matchingPersonIndex != -1) {
                        persons[matchingPersonIndex] = editedUser
                    }
                }

                // Remove the deleted users from the list
                persons.removeIf { it.id in deletedUsers }

                // Update the user list in the UI
                runOnUiThread {
                    setUserList(persons)
                }
            }
            response.close()

            // Set the loading status to done in the UI
            runOnUiThread {
                setDone(true)
            }

        }
    }

    /**
     * Searches for users based on the provided search term and updates the UI.
     *
     * @param searchTerm The search term to match against user attributes (e.g., firstName, lastName, email).
     * @param setUsers Callback to update the user list in the UI.
     * @param setDone Callback to set the loading status of the UI.
     * @param deletedUsers List of IDs of users that were deleted.
     * @param addedUsers List of users that were added.
     * @param editedUsers List of users that were edited.
     */
    private fun searchUsers(searchTerm: String, setUsers: (List<Person>) -> Unit, setDone: (Boolean) -> Unit,
                    deletedUsers: List<Int>, addedUsers: List<Person>, editedUsers: List<Person>){
        setDone(false)
        thread {
            // Searches users from the dummyjson-database,
            // NOT the local data
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
                // Add matching added users to the list
                addedUsers.forEach{
                    if(it.firstName!!.contains(searchTerm) || it.lastName!!.contains(searchTerm)
                        || it.email!!.contains(searchTerm)) {
                        persons.add(it)
                    }
                }

                // Remove all edited users from the list
                persons.removeIf { person ->
                    editedUsers.any { editedUser ->
                        editedUser.id == person.id
                    }
                }

                // Add back all edited users that match the search term
                editedUsers.forEach{
                    if(it.firstName!!.contains(searchTerm, ignoreCase = true) || it.lastName!!.contains(searchTerm, ignoreCase = true)
                        || it.email!!.contains(searchTerm, ignoreCase = true)) {
                        persons.add(it)
                    }
                }

                // Remove deleted users from the list
                persons.removeIf { it.id in deletedUsers }
                // Update the user list in the UI
                runOnUiThread {
                    setUsers(persons)
                }
                persons.forEach {
                    println("${it.firstName} ${it.lastName}")
                }
            }
            response.close()

            // Set the loading status to done in the UI
            runOnUiThread {
                setDone(true)
            }

        }
    }
}