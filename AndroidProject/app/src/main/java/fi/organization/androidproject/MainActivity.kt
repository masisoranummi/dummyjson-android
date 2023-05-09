package fi.organization.androidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import kotlin.concurrent.thread


@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(var firstName: String? = null, var lastName: String? = null, var age: Int = 0,
                  var gender: String? = null, var email: String? = null, var phone: String? = null,
                  var id: Int = 0, var image: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DummyJsonObject(var users: MutableList<Person>? = null)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserList()
        }
    }


    private fun fetchAll(setDone: (Boolean) -> Unit, setUserList: (List<Person>) -> Unit) {
        thread {
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
                runOnUiThread {
                    setUserList(persons)
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


    @Composable
    fun UserList() {
        val (done, setDone) = remember { mutableStateOf(false) }
        val (userList, setUserList) = remember { mutableStateOf(emptyList<Person>()) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (done) {
                LazyColumn() {
                    items(userList) { user ->
                        MessageCard(person = user)
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }

        LaunchedEffect(Unit) {
            fetchAll(setDone, setUserList)
        }
    }
}



@Composable
fun MessageCard(person: Person) {
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
            modifier = Modifier.clip(CircleShape).size(70.dp)
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
fun SimpleButton(buttonText: String, onButtonClick: () -> Unit) {
    Button(onClick = onButtonClick) {
        Text(text = buttonText)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

}