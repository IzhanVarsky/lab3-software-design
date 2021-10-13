package ru.akirakozov.sd.refactoring.DB;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.*;
import java.util.function.Function;

import static ru.akirakozov.sd.refactoring.html.HTMLUtils.constructHTMLResponse;
import static ru.akirakozov.sd.refactoring.html.HTMLUtils.pairToHTMLString;

public class DBUtils {
    public static void executeUpdate(String sql) {
        try (Connection c = getConnection()) {
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeQuery(HttpServletResponse response, String sql) {
        executeQuery(response, sql, null);
    }

    public static void executeQuery(HttpServletResponse response, String sql, String prependContent) {
        executeQuery(response, sql, prependContent, null);
    }

    public static void executeQuery(HttpServletResponse response, String sql, String prependContent, Function<ResultSet, Object> fun) {
        try (Connection c = getConnection()) {
            Statement stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(sql);

            StringBuilder responseBuilder = new StringBuilder();

            if (prependContent != null) {
                responseBuilder.append(prependContent);
            }
            if (fun != null) {
                responseBuilder.append(fun.apply(rs));
            }

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                responseBuilder.append(pairToHTMLString(name, price));
            }

            response.getWriter().print(constructHTMLResponse(responseBuilder));
            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:test.db");
    }
}
