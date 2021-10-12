package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ru.akirakozov.sd.refactoring.DB.DBUtils.executeQuery;
import static ru.akirakozov.sd.refactoring.servlet.ResponseUtils.setContentTypeAndOKStatus;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        executeQuery(response, "SELECT * FROM PRODUCT");

        setContentTypeAndOKStatus(response);
    }
}
