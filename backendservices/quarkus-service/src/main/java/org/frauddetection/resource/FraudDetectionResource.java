package org.frauddetection.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.frauddetection.service.FraudDetectionHandler;
import org.json.JSONObject;

@Path("/api/fraud")
public class FraudDetectionResource {

    @Inject
    FraudDetectionHandler fraudDetectionHandler;

    @POST
    @Path("/check")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkTransaction(String jsonData) {
        try {
            JSONObject requestData = new JSONObject(jsonData);
            JSONObject result = fraudDetectionHandler.processTransaction(requestData);
            return Response.ok(result.toString()).build();
        } catch (Exception e) {
            return Response.status(500)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
