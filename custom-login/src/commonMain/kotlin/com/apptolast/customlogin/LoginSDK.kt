package com.apptolast.customlogin

import com.apptolast.customlogin.domain.model.LoginConfig

object LoginSDK {

    private var _config: LoginConfig = LoginConfig()

    val config: LoginConfig
        get() = _config

    fun init(config: LoginConfig) {
        _config = config
    }
}
