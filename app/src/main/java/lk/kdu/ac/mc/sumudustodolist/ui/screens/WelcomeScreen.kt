package lk.kdu.ac.mc.sumudustodolist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign
import lk.kdu.ac.mc.sumudustodolist.ui.navigation.Screen
import lk.kdu.ac.mc.sumudustodolist.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

//Composable function for the Welcome screen
//First screen users see
@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section for Image
        Image(
            painter = painterResource(id = R.drawable.ic_todo_welcome),
            contentDescription = "Welcome Illustration",
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to Sumudu's TO DO List!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "This App was developed by",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SIL Ratnayake (D/BCS/23/0012).",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        //Action button to navigate to the lists overview screen
        Button(
            onClick = { navController.navigate(Screen.ListsOverview.route) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                "Go to Lists",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}