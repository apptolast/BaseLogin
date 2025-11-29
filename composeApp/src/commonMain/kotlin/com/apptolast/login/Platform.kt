package com.apptolast.login

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform