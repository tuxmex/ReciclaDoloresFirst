package mx.edu.utng.arg.recicladoloresfirst.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import mx.edu.utng.arg.recicladoloresfirst.ui.screens.auth.LoginScreen
import mx.edu.utng.arg.recicladoloresfirst.ui.screens.auth.RegisterScreen
import mx.edu.utng.arg.recicladoloresfirst.ui.screens.home.HomeScreen
import mx.edu.utng.arg.recicladoloresfirst.ui.screens.entrega.EntregaScreen
import mx.edu.utng.arg.recicladoloresfirst.ui.screens.recompensas.RecompensasScreen


/**
 * Grafo de navegación principal de la aplicación.
 *
 * Define todas las pantallas y las transiciones entre ellas.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    auth: FirebaseAuth
) {
    // Determinar pantalla inicial según estado de autenticación
    val startDestination = if (auth.currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ========== AUTENTICACIÓN ==========

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ========== HOME ==========

        composable(Screen.Home.route) {
            // Verificar autenticación
            LaunchedEffect(Unit) {
                if (auth.currentUser == null) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            HomeScreen(
                onNavigateToPerfil = {
                    navController.navigate(Screen.Perfil.route)
                },
                onNavigateToEntregas = {
                    navController.navigate(Screen.MisEntregas.route)
                },
                onNavigateToRecompensas = {
                    navController.navigate(Screen.Recompensas.route)
                },
                onNavigateToNuevaEntrega = {
                    navController.navigate(Screen.NuevaEntrega.route)
                },
                onNavigateToHistorial = {
                    navController.navigate(Screen.Historial.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ========== ENTREGAS ==========

        composable(Screen.NuevaEntrega.route) {
            EntregaScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MisEntregas.route) {
            // TODO: Implementar MisEntregasScreen
            // Por ahora, pantalla placeholder
            Text("Mis Entregas - En construcción")
        }

        composable(
            route = Screen.DetalleEntrega.route,
            arguments = listOf(
                navArgument("entregaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entregaId = backStackEntry.arguments?.getString("entregaId") ?: ""
            // TODO: Implementar DetalleEntregaScreen
            Text("Detalle Entrega: $entregaId")
        }

        composable(Screen.Recompensas.route) {
            RecompensasScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetalle = { recompensaId ->
                    navController.navigate(Screen.DetalleRecompensa.createRoute(recompensaId))
                }
            )
        }

        composable(
            route = Screen.DetalleRecompensa.route,
            arguments = listOf(
                navArgument("recompensaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recompensaId = backStackEntry.arguments?.getString("recompensaId") ?: ""
            // TODO: Implementar DetalleRecompensaScreen
            Text("Detalle Recompensa: $recompensaId")
        }

        // ========== PERFIL ==========

        composable(Screen.Perfil.route) {
            // TODO: Implementar PerfilScreen
            Text("Perfil - En construcción")
        }

        // ========== HISTORIAL Y CANJES ==========

        composable(Screen.Historial.route) {
            // TODO: Implementar HistorialScreen
            Text("Historial - En construcción")
        }

        composable(Screen.MisCanjes.route) {
            // TODO: Implementar MisCanjesScreen
            Text("Mis Canjes - En construcción")
        }
    }

}




