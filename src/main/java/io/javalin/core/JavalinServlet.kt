/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin.core

import io.javalin.HaltException
import io.javalin.Response
import io.javalin.core.util.RequestUtil
import io.javalin.core.util.Util
import io.javalin.embeddedserver.StaticResourceHandler
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JavalinServlet(private val matcher: PathMatcher, private val exceptionMapper: ExceptionMapper, private val errorMapper: ErrorMapper, private val staticResourceHandler: StaticResourceHandler) : Servlet {

    @Throws(ServletException::class, IOException::class)
    override fun service(servletRequest: ServletRequest, servletResponse: ServletResponse) {

        val httpRequest = servletRequest as HttpServletRequest
        val httpResponse = servletResponse as HttpServletResponse
        val type = HandlerType.fromServletRequest(httpRequest)
        val requestUri = httpRequest.requestURI
        val request = RequestUtil.create(httpRequest)
        val response = Response(httpResponse)

        response.header("Server", "Javalin")

        try { // before-handlers, endpoint-handlers, static-files

            for (beforeEntry in matcher.findEntries(HandlerType.BEFORE, requestUri)) {
                beforeEntry.handler.handle(RequestUtil.create(httpRequest, beforeEntry, requestUri), response)
            }

            val entries = matcher.findEntries(type, requestUri)
            if (!entries.isEmpty()) {
                for (endpointEntry in entries) {
                    val currentRequest = RequestUtil.create(httpRequest, endpointEntry, requestUri)
                    endpointEntry.handler.handle(currentRequest, response)
                    if (!currentRequest.nexted()) {
                        break
                    }
                }
            } else if (type !== HandlerType.HEAD || type === HandlerType.HEAD && matcher.findEntries(HandlerType.GET, requestUri).isEmpty()) {
                if (staticResourceHandler.handle(httpRequest, httpResponse)) {
                    return
                }
                throw HaltException(404, "Not found")
            }

        } catch (e: Exception) {
            // both before-handlers and endpoint-handlers can throw Exception,
            // we need to handle those here in order to run after-filters even if an exception was thrown
            exceptionMapper.handle(e, request, response)
        }

        try { // after-handlers
            for (afterEntry in matcher.findEntries(HandlerType.AFTER, requestUri)) {
                afterEntry.handler.handle(RequestUtil.create(httpRequest, afterEntry, requestUri), response)
            }
        } catch (e: Exception) {
            // after filters can also throw exceptions
            exceptionMapper.handle(e, request, response)
        }

        try { // error mapping (turning status codes into standardized messages/pages)
            errorMapper.handle(response.status(), request, response)
        } catch (e: RuntimeException) {
            // depressingly, the error mapping itself could throw a runtime exception
            // we need to handle these last... but that's it.
            exceptionMapper.handle(e, request, response)
        }

        // javalin is done doing stuff, write result to servlet-response
        if (response.contentType() == null) {
            httpResponse.contentType = "text/plain"
        }
        if (response.encoding() == null) {
            httpResponse.characterEncoding = StandardCharsets.UTF_8.name()
        }
        if (response.body() != null) {
            httpResponse.writer.write(response.body())
            httpResponse.writer.flush()
            httpResponse.writer.close()
        } else if (response.bodyStream() != null) {
            Util.copyStream(response.bodyStream()!!, httpResponse.outputStream)
        }

    }

    @Throws(ServletException::class)
    override fun init(config: ServletConfig) {
    }

    override fun getServletConfig(): ServletConfig? {
        return null
    }

    override fun getServletInfo(): String? {
        return null
    }

    override fun destroy() {}

}
