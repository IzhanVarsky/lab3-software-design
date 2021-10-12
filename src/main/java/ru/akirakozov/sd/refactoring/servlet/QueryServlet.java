package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.akirakozov.sd.refactoring.DB.DBUtils.executeQuery;
import static ru.akirakozov.sd.refactoring.servlet.ResponseUtils.setContentTypeAndOKStatus;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        switch (command) {
            case "max" -> executeQuery(response,
                    "SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1",
                    "<h1>Product with max price: </h1>");
            case "min" -> executeQuery(response,
                    "SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1",
                    "<h1>Product with min price: </h1>");
            case "sum" -> executeQuery(response,
                    "SELECT SUM(price) FROM PRODUCT",
                    "Summary price: ",
                    (ResultSet rs, PrintWriter writer) -> {
                        try {
                            if (rs.next()) {
                                writer.println(rs.getInt(1));
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException();
                        }
                    }
            );
            case "count" -> executeQuery(response,
                    "SELECT COUNT(*) FROM PRODUCT",
                    "Number of products: ",
                    (ResultSet rs, PrintWriter writer) -> {
                        try {
                            if (rs.next()) {
                                writer.println(rs.getInt(1));
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException();
                        }
                    }
            );
            default -> response.getWriter().println("Unknown command: " + command);
        }

        setContentTypeAndOKStatus(response);
    }

}
