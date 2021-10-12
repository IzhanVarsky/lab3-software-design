package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ru.akirakozov.sd.refactoring.DB.DBUtils.executeUpdate;
import static ru.akirakozov.sd.refactoring.servlet.ResponseUtils.setContentTypeAndOKStatus;

/**
 * @author akirakozov
 */
public class AddProductServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        long price = Long.parseLong(request.getParameter("price"));

        executeUpdate("""
                INSERT INTO PRODUCT
                (NAME, PRICE) VALUES ("%s", "%s")
                """.formatted(name, price));

        setContentTypeAndOKStatus(response);
        response.getWriter().println("OK");
    }
}
