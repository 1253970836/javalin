// Javalin - https://javalin.io
// Copyright 2017 David Åse
// Licensed under Apache 2.0: https://github.com/tipsy/javalin/blob/master/LICENSE

package javalin.security;

import java.util.List;

import javalin.Handler;
import javalin.Request;
import javalin.Response;

@FunctionalInterface
public interface AccessManager {
    void manage(Handler handler, Request request, Response response, List<Role> permittedRoles) throws Exception;
}