package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpResponse;
import jakarta.servlet.http.HttpRequest;
import java.io.IOException;

@WebServlet("/merchant-analytics")
public class MerchantAnalyticsHandler extends HttpServlet{

  @Override
  public static void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    //it will not take any data just emit the data that is required for the analytics as per customized request from customer
    
    response.setContentType("application/json");
    response.getWriter().write.(.toString());
  }
}
