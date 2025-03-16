package org.frauddetection.resource;

import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/fraud")
public class CorsOptionsHandler {

    @OPTIONS
    @Path("/check")
    public Response handleOptions() {
        return Response.ok().build();
    }
}