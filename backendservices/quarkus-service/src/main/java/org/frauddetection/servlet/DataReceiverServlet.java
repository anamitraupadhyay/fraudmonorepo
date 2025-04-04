package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import org.json.JSONObject;
import org.frauddetection.model.TransactionData;
import org.frauddetection.service.FraudDetectionHandler;
import jakarta.inject.Inject;

@WebServlet("/data-handler")
public class DataReceiverServlet extends HttpServlet {
    
    @Inject
    private FraudDetectionHandler handler;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Parse request body to get transaction data
        TransactionData txnData = parseRequestData(request);
        
        // Process with FraudDetectionHandler (remains untouched)
        JSONObject result = handler.processTransaction(txnData);
        
        // Store as request attributes
        request.setAttribute("transaction_data", txnData);
        request.setAttribute("transaction_result", result);
        
        // Forward to merchant analytics servlet
        RequestDispatcher dispatcher = request.getRequestDispatcher("/merchant-analytics-internal");
        dispatcher.include(request, response);
        
        // Return the result (possibly enhanced with analytics warnings)
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(result.toString());
    }
    
    private TransactionData parseRequestData(HttpServletRequest request) throws IOException {
        // Read JSON from request
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        // Parse JSON into TransactionData
        JSONObject json = new JSONObject(sb.toString());
        TransactionData data = new TransactionData();
        
        data.setCcNum(json.getLong("cc_num"));
        data.setAmt(json.getDouble("amt"));
        data.setZip(json.getString("zip"));
        data.setLat(json.getDouble("lat"));
        data.setLon(json.getDouble("long"));
        data.setCityPop(json.getInt("city_pop"));
        data.setUnixTime(json.getLong("unix_time"));
        data.setMerchLat(json.getDouble("merch_lat"));
        data.setMerchLon(json.getDouble("merch_long"));
        
        return data;
    }
}