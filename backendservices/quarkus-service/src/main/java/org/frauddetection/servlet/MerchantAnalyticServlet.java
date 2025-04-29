package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.json.JSONObject;
import org.frauddetection.model.TransactionData;
import org.frauddetection.service.MerchantAnalyticsHandler;

@WebServlet("/merchant-analytics")
public class MerchantAnalyticServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get transaction data and result from request attributes
        TransactionData transactionData = (TransactionData) request.getAttribute("transactionData");
        JSONObject result = (JSONObject) request.getAttribute("result");
        
        if (transactionData == null || result == null) {
            System.err.println("MerchantAnalyticServlet: Missing transaction data or result");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(new JSONObject()
                .put("error", "Missing transaction data or result")
                .toString());
            return;
        }
        
        try {
            // Process analytics using the MerchantAnalyticsHandler service
            MerchantAnalyticsHandler analyticsHandler = new MerchantAnalyticsHandler();
            JSONObject analyticsResult = analyticsHandler.processMerchantAnalytics(transactionData);
            
            // Add analytics to the result
            if (analyticsResult.has("analytics")) {
                result.put("analytics", analyticsResult.getJSONObject("analytics"));
            }
            
            // Return the combined result
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
            
        } catch (Exception e) {
            System.err.println("Error in merchant analytics: " + e.getMessage());
            e.printStackTrace();
            
            // Still return the original fraud detection result even if analytics failed
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
        }
    }
}
    //it will not take any data just emit the data that is required for the analytics as per customized request from customer
    //now this servlet is getting same state response as data-handler which is what i needed for my use case and as per experience its a design flaw of the current implementation using servlet but any way its desired for now
    //another mistake as request.getInputStream or request.getReader is one time use stream and its been already been read at data-handler servlet so caching is necessary instead rereading and attach it for this servlet...the part added for this part will be commented "cached and attached for analytics servlet"
    /*StringBuilder stringjsonbuilderobj = new StringBuilder();
    *String line;
    *try (BufferReader bufferreaderobj = request.getReader()) {
    * while ((line = reader.readLine()) != null) {
    *    stringjsonbuilderobj.append(line);
    *    }
    *  }
    */