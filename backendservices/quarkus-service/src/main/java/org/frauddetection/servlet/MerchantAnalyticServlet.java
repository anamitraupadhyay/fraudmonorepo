package org.frauddetection.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpResponse;
import jakarta.servlet.http.HttpRequest;
import java.io.IOException;

@WebServlet("/merchant-analytics")
public class MerchantAnalyticsHandler extends HttpServlet {

  @Override
  public static void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
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

    response.setContentType("application/json");
    response.getWriter().write. (.toString());
    }
  }