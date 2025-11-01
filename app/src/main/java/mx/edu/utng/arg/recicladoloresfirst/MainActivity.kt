package mx.edu.utng.arg.recicladoloresfirst

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.utng.arg.recicladoloresfirst.ui.navigation.NavGraph
import mx.edu.utng.arg.recicladoloresfirst.ui.theme.ReciclaDoloresFIRSTTheme
import javax.inject.Inject

/**
 * Activity principal de la aplicación.
 *
 * @AndroidEntryPoint permite que Hilt inyecte dependencias en esta Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReciclaDoloresFIRSTTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        auth = auth
                    )
                }
            }
        }
    }
}