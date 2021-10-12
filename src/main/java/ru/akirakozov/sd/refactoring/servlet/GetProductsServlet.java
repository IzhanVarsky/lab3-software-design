package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.html.HTMLUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

import static ru.akirakozov.sd.refactoring.html.HTMLUtils.pairToHTMLString;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
                Statement stmt = c.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT");
                response.getWriter().println("<html><body>");

                kek(response, stmt, rs);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    static void kek(HttpServletResponse response, Statement stmt, ResultSet rs) throws SQLException, IOException {
        while (rs.next()) {
            String name = rs.getString("name");
            int price = rs.getInt("price");
            response.getWriter().println(pairToHTMLString(name, price));
        }
        response.getWriter().println("</body></html>");

        rs.close();
        stmt.close();
    }
}
