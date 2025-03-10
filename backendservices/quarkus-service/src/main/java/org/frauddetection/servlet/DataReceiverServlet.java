package org.frauddetection.servlet;

import org.frauddetection.model.FormData;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that receives form data and forwards it for processing
 */
@WebServlet("/data-handler")
public class DataReceiverServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Create a FormData object from request parameters to store the data in variables
        FormData formData = new FormData(
                request.getParameter("field1"),
                request.getParameter("field2"),
                request.getParameter("field3"),
                request.getParameter("field4"),
                request.getParameter("field5"),
                request.getParameter("field6"),
                request.getParameter("field7"),
                request.getParameter("field8"),
                request.getParameter("field9"));

        // Store the data object as a request attribute
        request.setAttribute("formData", formData);

        // Forward to the processing servlet
        request.getRequestDispatcher("/process-data").forward(request, response);
    }
}