package org.insbaixcamp.listadelacompra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.insbaixcamp.listadelacompra.data.repository.AuthRepository
import org.insbaixcamp.listadelacompra.ui.auth.LoginScreen
import org.insbaixcamp.listadelacompra.ui.auth.RegisterScreen
import org.insbaixcamp.listadelacompra.ui.lists.ListsScreen

@Composable
fun NavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val authRepository = remember { AuthRepository() }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.LISTS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.LISTS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.LISTS) {
            ListsScreen(
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LISTS) { inclusive = true }
                    }
                }
            )
        }
    }
}
