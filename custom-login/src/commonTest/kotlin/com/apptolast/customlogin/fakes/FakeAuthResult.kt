package com.apptolast.customlogin.fakes

import com.apptolast.customlogin.data.AuthResultWrapper
import com.apptolast.customlogin.data.UserWrapper

/**
 * A fake implementation of [AuthResultWrapper] for testing purposes.
 */
class FakeAuthResultWrapper(
    override val user: UserWrapper? = null
) : AuthResultWrapper
