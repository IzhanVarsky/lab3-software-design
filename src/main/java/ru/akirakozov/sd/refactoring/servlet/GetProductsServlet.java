package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.DB.DBUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        DBUtils.executeQuery(response, "SELECT * FROM PRODUCT");

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
