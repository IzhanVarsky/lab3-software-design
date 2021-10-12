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
import java.util.*;

public class TestServlets {
//    Run test with already running server!

    @Before
    public void setUp() {
        runSQL("CREATE TABLE IF NOT EXISTS PRODUCT" +
               "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
               " NAME           TEXT    NOT NULL, " +
               " PRICE          INT     NOT NULL)");
    }

    @After
    public void closeAndClearAll() {
        runSQL("DROP TABLE PRODUCT");
    }

    private void runSQL(String sql) {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServlets() throws IOException {
        Assert.assertEquals(constructHTMLResponse(), getProducts());

        Assert.assertEquals("OK", addProduct("x1", "1000"));
        Assert.assertEquals("OK", addProduct("x2", "10000"));
        Assert.assertEquals("OK", addProduct("x1", "-100"));

        Assert.assertEquals(
                constructHTMLResponse(
                        pairToHTMLString("x1", "1000"),
                        pairToHTMLString("x2", "10000"),
                        pairToHTMLString("x1", "-100")
                ),
                getProducts()
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        h1Wrap("Product with max price: "), pairToHTMLString("x2", "10000")
                ),
                getByQuery("max")
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        h1Wrap("Product with min price: "), pairToHTMLString("x1", "-100")
                ),
                getByQuery("min")
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        "Summary price: ", String.valueOf(1000 + 10000 + (-100))
                ),
                getByQuery("sum")
        );

        Assert.assertEquals(
                constructHTMLResponse("Number of products: 3"),
                getByQuery("count")
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

    private String constructHTMLResponse(String... strings) {
        StringBuilder sj = new StringBuilder();
        Arrays.stream(strings).forEach(sj::append);
        return wrapWithTag("html", wrapWithTag("body", sj.toString()));
    }

    private String addProduct(String name, String price) throws IOException {
        return sendRequestAndGetResponse("add-product", Map.of("name", name, "price", price));
    }

    private String getProducts() throws IOException {
        return sendRequestAndGetResponse("get-products", Collections.emptyMap());
    }

    private String getByQuery(String command) throws IOException {
        return sendRequestAndGetResponse("query", Map.of("command", command));
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
