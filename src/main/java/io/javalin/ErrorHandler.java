/*
 * Javalin - https://javalin.io
 * Copyright 2017 David Åse
 * Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE
 */

package io.javalin;

@FunctionalInterface
public interface ErrorHandler {
    void handle(Request request, Response response);  // similar to handler, can't throw exception
}
