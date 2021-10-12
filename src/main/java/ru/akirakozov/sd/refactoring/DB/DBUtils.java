package ru.akirakozov.sd.refactoring.DB;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.sql.*;
import java.util.function.BiConsumer;

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

    public static void executeQuery(HttpServletResponse response, String sql, String prependContent, BiConsumer<ResultSet, PrintWriter> fun) {
        try (Connection c = getConnection()) {
            Statement stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            final PrintWriter writer = response.getWriter();

            writer.println("<html><body>");
            if (prependContent != null) {
                writer.println(prependContent);
            }
            if (fun != null) {
                fun.accept(rs, writer);
            }

            while (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                writer.println(pairToHTMLString(name, price));
            }
            writer.println("</body></html>");

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
