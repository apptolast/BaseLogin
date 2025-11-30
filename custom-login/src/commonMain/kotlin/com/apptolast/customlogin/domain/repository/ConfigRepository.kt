package com.apptolast.customlogin.domain.repository

import com.apptolast.customlogin.domain.model.LoginConfig

interface ConfigRepository {

    /**
     * Retrieves the UI configuration for the login screen.
     * @return LoginConfig with branding details.
     */
    fun getLoginConfig(): LoginConfig
}