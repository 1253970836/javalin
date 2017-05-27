/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 *
 */

package io.javalin.core;

import io.javalin.Handler;

public class HandlerMatch {

    public Handler handler;
    public String handlerUri;
    public String requestUri;

    public HandlerMatch(Handler handler, String matchUri, String requestUri) {
        this.handler = handler;
        this.handlerUri = matchUri;
        this.requestUri = requestUri;
    }
}
