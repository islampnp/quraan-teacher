package com.quraan.teacher.app.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quraan.teacher.app.presentation.screens.dashboard.DashboardScreen
import com.quraan.teacher.app.presentation.screens.dashboard.DashboardViewModel
import com.quraan.teacher.app.presentation.screens.students.StudentsScreen
import com.quraan.teacher.app.presentation.screens.students.StudentsViewModel
import com.quraan.teacher.app.presentation.screens.student_detail.StudentDetailScreen
import com.quraan.teacher.app.presentation.screens.student_detail.StudentDetailViewModel
import com.quraan.teacher.app.presentation.screens.learning_path.LearningPathGeneratorScreen
import com.quraan.teacher.app.presentation.screens.learning_path.LearningPathViewModel
import com.quraan.teacher.app.presentation.screens.schedule.ScheduleScreen
import com.quraan.teacher.app.presentation.screens.schedule.ScheduleViewModel
import com.quraan.teacher.app.presentation.screens.quran_viewer.QuranViewerScreen
import com.quraan.teacher.app.presentation.screens.quran_viewer.QuranViewerViewModel
import com.quraan.teacher.app.presentation.screens.audio.AudioScreen
import com.quraan.teacher.app.presentation.screens.audio.AudioViewModel
import com.quraan.teacher.app.presentation.screens.quizzes.QuizzesScreen
import com.quraan.teacher.app.presentation.screens.quizzes.QuizzesViewModel
import com.quraan.teacher.app.presentation.theme.Background
import com.quraan.teacher.app.presentation.theme.Primary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "لوحة التحكم", Icons.Default.Dashboard)
    data object Students : Screen("students", "الطلاب", Icons.Default.People)
    data object Schedule : Screen("schedule", "الجدول", Icons.Default.CalendarMonth)
    data object QuranViewer : Screen("quran_viewer", "المصحف", Icons.Default.MenuBook)
    data object Audio : Screen("audio", "التسجيلات", Icons.Default.Mic)
    data object Quizzes : Screen("quizzes", "الاختبارات", Icons.Default.Quiz)

    data object StudentDetail : Screen("student_detail/{studentId}", "تفاصيل الطالب", Icons.Default.Person) {
        fun createRoute(studentId: Long) = "student_detail/$studentId"
    }
    data object LearningPathGenerator : Screen("learning_path/generate/{studentId}", "إنشاء مسار", Icons.Default.Route) {
        fun createRoute(studentId: Long) = "learning_path/generate/$studentId"
    }
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Students,
    Screen.Schedule,
    Screen.QuranViewer,
    Screen.Audio
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = Primary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300)) }
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToStudent = { id ->
                        navController.navigate(Screen.StudentDetail.createRoute(id))
                    },
                    onNavigateToAddSession = {
                        navController.navigate(Screen.Students.route)
                    }
                )
            }

            composable(Screen.Students.route) {
                val vm: StudentsViewModel = hiltViewModel()
                StudentsScreen(
                    viewModel = vm,
                    onStudentClick = { id ->
                        navController.navigate(Screen.StudentDetail.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.StudentDetail.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
                val vm: StudentDetailViewModel = hiltViewModel()
                StudentDetailScreen(
                    viewModel = vm,
                    studentId = studentId,
                    onNavigateToPathGenerator = {
                        navController.navigate(Screen.LearningPathGenerator.createRoute(studentId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.LearningPathGenerator.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: return@composable
                val vm: LearningPathViewModel = hiltViewModel()
                LearningPathGeneratorScreen(
                    viewModel = vm,
                    studentId = studentId,
                    onBack = { navController.popBackStack() },
                    onPathSaved = { navController.popBackStack() }
                )
            }

            composable(Screen.Schedule.route) {
                val vm: ScheduleViewModel = hiltViewModel()
                ScheduleScreen(
                    viewModel = vm,
                    onStudentClick = { id ->
                        navController.navigate(Screen.StudentDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.QuranViewer.route) {
                val vm: QuranViewerViewModel = hiltViewModel()
                QuranViewerScreen(
                    viewModel = vm,
                    onStudentClick = { id ->
                        navController.navigate(Screen.StudentDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Audio.route) {
                val vm: AudioViewModel = hiltViewModel()
                AudioScreen(viewModel = vm)
            }

            composable(Screen.Quizzes.route) {
                val vm: QuizzesViewModel = hiltViewModel()
                QuizzesScreen(viewModel = vm)
            }
        }
    }
}
