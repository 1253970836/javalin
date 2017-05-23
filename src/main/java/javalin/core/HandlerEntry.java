// Javalin - https://javalin.io
// Copyright 2017 David Åse
// Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE

package javalin.core;

import javalin.Handler;

public class HandlerEntry {

    public Handler.Type type;
    public String path;
    public Handler handler;

    public HandlerEntry(Handler.Type type, String path, Handler handler) {
        this.type = type;
        this.path = path;
        this.handler = handler;
    }

}
