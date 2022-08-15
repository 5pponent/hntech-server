package hntech.hntechserver.utils

import hntech.hntechserver.utils.function.logger
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.ContentCachingResponseWrapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LoggingInterceptor : HandlerInterceptor {
    val log = logger()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        log.info(
            "-------------- User Request {} -------------- {}: {}",
            request.remoteAddr,
            request.method,
            request.requestURL
        )
        return super.preHandle(request, response, handler)
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        log.info("------------ Server Response ------------- result : {}", response.status)

        val bytes = ContentCachingResponseWrapper(response).contentAsByteArray
        print(String(bytes, Charsets.UTF_8))
        super.postHandle(request, response, handler, modelAndView)
    }
}