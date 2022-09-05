package hntech.hntechserver.auth

import hntech.hntechserver.common.ADMIN
import hntech.hntechserver.common.AUTH_DENIED
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.security.auth.login.LoginException

@Aspect
@Component
class AuthAspect {
    @Before("@annotation(hntech.hntechserver.auth.Auth)")
    fun authCheck() {
        val request = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        request.request.session.getAttribute(ADMIN) ?: throw LoginException(AUTH_DENIED)
    }
}