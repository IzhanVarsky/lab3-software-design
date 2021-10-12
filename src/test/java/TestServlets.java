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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class TestServlets {
//    Run test with already running server!

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
    public void testServlets() throws IOException {
        Assert.assertEquals(constructHTMLResponse(List.of()), getProducts());

        Assert.assertEquals("OK",
                addProduct(Map.of("name", "x1", "price", "1000")));

        Assert.assertEquals("OK",
                addProduct(Map.of("name", "x2", "price", "10000")));

        Assert.assertEquals("OK",
                addProduct(Map.of("name", "x1", "price", "-100")));

        Assert.assertEquals(
                constructHTMLResponse(
                        List.of(
                                pairToHTMLString("x1", "1000"),
                                pairToHTMLString("x2", "10000"),
                                pairToHTMLString("x1", "-100")
                        )
                ),
                getProducts()
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        List.of(h1Wrap("Product with max price: "), pairToHTMLString("x2", "10000"))
                ),
                getByQuery(Map.of("command", "max"))
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        List.of(h1Wrap("Product with min price: "), pairToHTMLString("x1", "-100"))
                ),
                getByQuery(Map.of("command", "min"))
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        List.of("Summary price: ", String.valueOf(1000 + 10000 + (-100)))
                ),
                getByQuery(Map.of("command", "sum"))
        );

        Assert.assertEquals(
                constructHTMLResponse(List.of("Number of products: 3")),
                getByQuery(Map.of("command", "count"))
        );
    }

    private String wrapWithTag(String tag, String content) {
        return "<%s>%s</%s>".formatted(tag, content, tag);
    }

    private String h1Wrap(String content) {
        return wrapWithTag("h1", content);
    }

    private String pairToHTMLString(String a, String b) {
        return a + "\t" + b + "</br>";
    }

    private String constructHTMLResponse(List<String> pairs) {
        StringBuilder sj = new StringBuilder();
        pairs.forEach(sj::append);
        return wrapWithTag("html", wrapWithTag("body", sj.toString()));
    }

    private String addProduct(Map<String, String> args) throws IOException {
        return sendRequestAndGetResponse("add-product", args);
    }

    private String getProducts() throws IOException {
        return sendRequestAndGetResponse("get-products", Collections.emptyMap());
    }

    private String getByQuery(Map<String, String> args) throws IOException {
        return sendRequestAndGetResponse("query", args);
    }

    private String makeURL(String method, Map<String, String> args) {
        StringJoiner sj = new StringJoiner("&");
        args.forEach((k, v) -> sj.add(k + "=" + v));
        return "http://localhost:8081/" + method + "?" + sj;
    }

    private String sendRequestAndGetResponse(String method, Map<String, String> args) throws IOException {
        URL url = new URL(makeURL(method, args));
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
