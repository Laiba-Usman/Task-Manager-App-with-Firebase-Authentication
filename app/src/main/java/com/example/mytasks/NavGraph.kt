package com.example.mytasks.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mytasks.AddEditTaskScreen
import com.example.mytasks.TaskListScreen
import com.example.mytasks.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object AddTask : Screen("add_task")
    object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: Int) = "edit_task/$taskId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route,
        modifier = modifier
    ) {
        composable(Screen.TaskList.route) {
            TaskListScreen(
                viewModel = hiltViewModel(),
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddTask.route)
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                }
            )
        }

        composable(Screen.AddTask.route) {
            AddEditTaskScreen(
                viewModel = hiltViewModel(),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            AddEditTaskScreen(
                viewModel = hiltViewModel(),
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}