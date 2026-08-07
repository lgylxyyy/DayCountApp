package com.daycountapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.daycountapp.DayCountApp
import com.daycountapp.ui.screens.EventFormScreen
import com.daycountapp.ui.screens.EventManagementScreen
import com.daycountapp.ui.screens.HiddenEventsScreen
import com.daycountapp.ui.screens.PasswordSetupScreen
import com.daycountapp.ui.screens.PersonalizationScreen
import com.daycountapp.ui.screens.SettingsScreen
import com.daycountapp.ui.viewmodel.EventViewModel

object Routes {
    const val MANAGE = "manage"
    const val EVENT_FORM = "event_form/{eventId}"
    const val PERSONALIZE = "personalize"
    const val SETTINGS = "settings"
    const val HIDDEN_EVENTS = "hidden_events"
    const val PASSWORD_SETUP = "password_setup"

    fun eventForm(eventId: Long? = null): String = "event_form/${eventId ?: -1}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val app = DayCountApp.instance
    val eventViewModel: EventViewModel =
        viewModel(
            factory = EventViewModel.Factory(app.eventRepository),
        )

    NavHost(
        navController = navController,
        startDestination = Routes.MANAGE,
        modifier = modifier,
    ) {
        composable(Routes.MANAGE) {
            EventManagementScreen(
                viewModel = eventViewModel,
                onNavigateToForm = { navController.navigate(Routes.eventForm()) },
                onEventClick = { eventId ->
                    navController.navigate(Routes.eventForm(eventId))
                },
                onNavigateToHidden = { navController.navigate(Routes.HIDDEN_EVENTS) },
            )
        }

        composable(
            route = Routes.EVENT_FORM,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId")
            EventFormScreen(
                viewModel = eventViewModel,
                eventId = if (eventId == -1L) null else eventId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PERSONALIZE) {
            PersonalizationScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHidden = { navController.navigate(Routes.HIDDEN_EVENTS) },
                onNavigateToPasswordSetup = { navController.navigate(Routes.PASSWORD_SETUP) },
            )
        }

        composable(Routes.HIDDEN_EVENTS) {
            HiddenEventsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditEvent = { eventId ->
                    navController.navigate(Routes.eventForm(eventId))
                },
            )
        }

        composable(Routes.PASSWORD_SETUP) {
            PasswordSetupScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
