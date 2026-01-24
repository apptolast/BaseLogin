package com.apptolast.login.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.apptolast.login.presentation.profile.ProfileScreen

fun NavGraphBuilder.mainRoutesFlow() {
    navigation<MainRoutesFlow>(
        startDestination = ProfileRoute
    ) {
        composable<ProfileRoute> {
            ProfileScreen()
        }
    }
}