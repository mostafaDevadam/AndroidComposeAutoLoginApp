package com.example.autologinapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Nullable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.autologinapp.ui.theme.AutoLoginAppTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AutoLoginAppTheme() {
                App()
            }
            //App()

        }
    }
}

// App
@Composable
fun App(){
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        token = AuthManager.getToken(context)
        loading = false
    }

    if(loading){
        LoadingScreen()

    }else {
        if(token == null){
            LoginScreen(
                onLogin= {
                    scope.launch {
                        val fakeToken = "fake_token_123123"

                        AuthManager.saveToken(context, fakeToken)

                        token = fakeToken

                        Toast.makeText(
                            context,
                            "Logged-in successfully!",
                            Toast.LENGTH_SHORT
                        ).show()


                    }
                }
            )

        }else {
            HomeScreen(
                context = context,
                token = token!!,
                onLogout={
                    scope.launch {
                        AuthManager.logout(context)
                        token = null
                        Toast.makeText(
                            context,
                            "Logout successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

    }


}

//
@Composable
fun LoadingScreen(){

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator()
    }

}
// Home Screen
@Composable
fun HomeScreen(
    context: Context,
    token: String,
    onLogout: () -> Unit
) {
    val vm: MessageViewModel = viewModel()
    val state by vm.uiState.collectAsState()
    val listState by vm.uiListState.collectAsState()

    val vvm: MsgVolleyViewModel = viewModel()
    val vState by vvm.uiVState.collectAsState()
    val vlistState by vvm.uiVListState.collectAsState()

    LaunchedEffect(Unit) {
        Toast.makeText(context, "Welcome Back!", Toast.LENGTH_LONG).show()
    }

    // Entire screen is now a single LazyColumn
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Section
        item {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(text = "Token: $token")
        }

        item {
            HorizontalDivider()
            Text(text = "Volley!", style = MaterialTheme.typography.headlineSmall)
        }

        // fetch using Volley!

        // Single Message using Volley
        item {
            when (val uivState = vState) {
                is UiVState.Loading -> {
                    CircularProgressIndicator()
                }
                is UiVState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        /*Text(
                            text = "Success: ${uivState.success.toString()}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Status: ${uivState.status}",
                            style = MaterialTheme.typography.headlineSmall
                        )

                         */
                        Text(
                            text = "Volley Message: ${uivState.message}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Button(onClick = { vm.fetchMessage() }) {
                            Text("Refresh Single Message")
                        }
                    }
                }
                is UiVState.Error -> {
                    Column {
                        Text(
                            text = "Volley Error: ${uivState.errorMsg}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { vvm.fetchMessage() }) {
                            Text("Volley Retry Single Message")
                        }
                    }
                }
            }
        }


        // MessagesList using Volley

        when (val lState = vlistState) {
            is UiListState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiListState.Success -> {
                // items() adds each item directly into the parent LazyColumn scope
                items(lState.messages) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Time: ${item.timestamp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            is UiListState.Error -> {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${lState.errorMsg}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { vvm.fetchMessagesArray() }) {
                            Text("Retry List")
                        }
                    }
                }
            }
        }



        item {
            HorizontalDivider()
            Text(text = "Retrofit!", style = MaterialTheme.typography.headlineSmall)
        }


        // 2. Single Message Section (uiState)
        item {
            when (val uiState = state) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }
                is UiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Retrofit Success: ${uiState.success.toString()}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Retrofit Status: ${uiState.status}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Retrofit Message: ${uiState.message}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Button(onClick = { vm.fetchMessage() }) {
                            Text("Refresh Single Message")
                        }
                    }
                }
                is UiState.Error -> {
                    Column {
                        Text(
                            text = "Retrofit Error: ${uiState.errorMsg}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { vm.fetchMessage() }) {
                            Text("Retry Single Message")
                        }
                    }
                }
            }
        }

        // 3. List Section Header / Divider
        item {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Text(
                text = "Message List:",
                style = MaterialTheme.typography.titleLarge
            )
        }

        // 4. Dynamic List Section (listState)
        when (val lState = listState) {
            is UiListState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiListState.Success -> {
                // items() adds each item directly into the parent LazyColumn scope
                items(lState.messages) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.message,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Time: ${item.timestamp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
            is UiListState.Error -> {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${lState.errorMsg}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { vm.fetchMessageList() }) {
                            Text("Retry List")
                        }
                    }
                }
            }
        }

        // 5. Logout Button at the bottom
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}


// Login Screen
@Composable
fun LoginScreen(
    onLogin: () -> Unit
){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text="Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        /*TextField(
            value = username,
            onValueChange = { it ->
                username = it
            },
            label = { Text(text = "Username") },


        )*/

        //Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { it ->
                username = it
            },
            label = { Text(text = "Username") },
            //modifier = Modifier.height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedSupportingTextColor = Color.Black,
                focusedTextColor = Color.Black,      // Color when the field is active
                unfocusedTextColor = Color.Gray,    // Color when the field is inactive
                disabledTextColor = Color.LightGray, // Color when the field is disabled
                errorTextColor = Color.Red           // Color when validation fails
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { it ->
                password = it
            },
            label = { Text("Password") },
            //modifier = Modifier.height(12.dp),
            colors = TextFieldDefaults.colors(
                /*focusedTextColor = Color.Red,
                disabledTextColor = Color.Gray*/
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                //onLogin()
                if(username == "admin" && password == "123"){
                    onLogin()
                }else {
                    Toast.makeText(
                        context,
                        "Login failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }



            }
        ) {
            Text("Login")
        }


    }



}




@Composable
fun TopToastScreen() {
    var isVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Automatically hide the top toast after 3 seconds
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000)
            isVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content
        Button(
            onClick = {
                toastMessage = "Success! Saved your changes."
                isVisible = true
            },
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("Show Top Toast")
        }

        // Custom Top Toast Animation Container
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = toastMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AutoLoginAppTheme {
        Greeting("Android")
    }
}