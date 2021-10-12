import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class AddAndGetTest {
//    Run test with already running server!

    static class Pair {
        String a;
        String b;

        private Pair(String a, String b) {
            this.a = a;
            this.b = b;
        }

        static Pair of(String a, String b) {
            return new Pair(a, b);
        }
    }

    @Before
    public void setUp() {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                         "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                         " NAME           TEXT    NOT NULL, " +
                         " PRICE          INT     NOT NULL)";
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @After
    public void closeAndClearAll() {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            String sql = "DROP TABLE PRODUCT";
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddAndGetProduct() throws IOException {
        String allProducts = sendRequestAndGetResponse(getProductsURL());
        Assert.assertEquals(constructHTMLResponse(List.of()), allProducts);

        String url1 = addProductURL(Map.of("name", "x1", "price", "1000"));
        Assert.assertEquals("OK", sendRequestAndGetResponse(url1));

        String url2 = addProductURL(Map.of("name", "x2", "price", "10000"));
        Assert.assertEquals("OK", sendRequestAndGetResponse(url2));

        String url3 = addProductURL(Map.of("name", "x1", "price", "-100"));
        Assert.assertEquals("OK", sendRequestAndGetResponse(url3));

        allProducts = sendRequestAndGetResponse(getProductsURL());
        Assert.assertEquals(
                constructHTMLResponse(
                        List.of(
                                Pair.of("x1", "1000"),
                                Pair.of("x2", "10000"),
                                Pair.of("x1", "-100")
                        )
                ),
                allProducts
        );
    }

    private String constructHTMLResponse(List<Pair> pairs) {
        StringBuilder sj = new StringBuilder();
        pairs.forEach(p -> sj.append(p.a).append("\t").append(p.b).append("</br>"));
        return "<html><body>%s</body></html>".formatted(sj);
    }

    private String addProductURL(Map<String, String> args) {
        return getURLImpl("add-product", args);
    }

    private String getProductsURL() {
        return getURLImpl("get-products", Map.of());
    }

    private String getQueryURL(Map<String, String> args) {
        return getURLImpl("query", args);
    }

    private String getURLImpl(String method, Map<String, String> args) {
        StringJoiner sj = new StringJoiner("&");
        args.forEach((k, v) -> sj.add(k + "=" + v));
        return "http://localhost:8081/" + method + "?" + sj;
    }

    private String sendRequestAndGetResponse(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }
}
