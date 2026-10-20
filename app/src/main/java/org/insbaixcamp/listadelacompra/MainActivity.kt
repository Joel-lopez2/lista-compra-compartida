package org.insbaixcamp.listadelacompra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import org.insbaixcamp.listadelacompra.navigation.NavGraph
import org.insbaixcamp.listadelacompra.navigation.Routes
import org.insbaixcamp.listadelacompra.ui.theme.ListaDeLaCompraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Si ya hay sesion iniciada vamos directos a "Mis listas"
        val startDestination =
            if (FirebaseAuth.getInstance().currentUser != null) Routes.LISTS else Routes.LOGIN

        setContent {
            ListaDeLaCompraTheme {
                NavGraph(startDestination = startDestination)
            }
        }
    }
}
