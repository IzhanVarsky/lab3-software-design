import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static ru.akirakozov.sd.refactoring.DB.DBUtils.executeUpdate;
import static ru.akirakozov.sd.refactoring.html.HTMLUtils.*;


public class TestServlets {
    Server server;

    @Before
    public void setUp() throws Exception {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS PRODUCT(
                 ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                 NAME TEXT NOT NULL,
                 PRICE INT NOT NULL
                )
                 """);

        server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet()), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet()), "/get-products");
        context.addServlet(new ServletHolder(new QueryServlet()), "/query");

        server.start();
    }

    @After
    public void closeAndClearAll() throws Exception {
        executeUpdate("DROP TABLE PRODUCT");
        server.stop();
    }

    @Test
    public void testServlets() throws IOException {
        Assert.assertEquals(constructHTMLResponse(), getProducts());

        Assert.assertEquals("OK", addProduct("x1", 1000));
        Assert.assertEquals("OK", addProduct("x2", 10000));
        Assert.assertEquals("OK", addProduct("x1", -100));

        Assert.assertEquals(
                constructHTMLResponse(
                        pairToHTMLString("x1", 1000),
                        pairToHTMLString("x2", 10000),
                        pairToHTMLString("x1", -100)
                ),
                getProducts()
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        h1Wrap("Product with max price: "), pairToHTMLString("x2", 10000)
                ),
                getByQuery("max")
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        h1Wrap("Product with min price: "), pairToHTMLString("x1", -100)
                ),
                getByQuery("min")
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        "Summary price: ", 1000 + 10000 + (-100)
                ),
                getByQuery("sum")
        );

        Assert.assertEquals(
                constructHTMLResponse("Number of products: 3"),
                getByQuery("count")
        );
    }

    @Test
    public void randomTestAll() throws IOException {
        int productCNT = 300;
        Random random = new Random();

        List<Integer> prices = new ArrayList<>(productCNT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < productCNT; i++) {
            final int price = random.nextInt();
            prices.add(price);
            Assert.assertEquals("OK", addProduct("x" + i, price));
            sb.append(pairToHTMLString("x" + i, price));
        }
        Assert.assertEquals(
                constructHTMLResponse(sb),
                getProducts()
        );

        final int maximum = Collections.max(prices);
        Assert.assertEquals(
                constructHTMLResponse(
                        h1Wrap("Product with max price: "),
                        pairToHTMLString("x" + prices.indexOf(maximum), maximum)
                ),
                getByQuery("max")
        );

        final int minimum = Collections.min(prices);
        Assert.assertEquals(
                constructHTMLResponse(
                        h1Wrap("Product with min price: "),
                        pairToHTMLString("x" + prices.indexOf(minimum), minimum)
                ),
                getByQuery("min")
        );

        Assert.assertEquals(
                constructHTMLResponse(
                        "Summary price: ", prices.stream().mapToInt(i -> i).sum()
                ),
                getByQuery("sum")
        );

        Assert.assertEquals(
                constructHTMLResponse("Number of products: ", productCNT),
                getByQuery("count")
        );
    }

    private String addProduct(String name, Object price) throws IOException {
        return sendRequestAndGetResponse("add-product", Map.of("name", name, "price", price.toString()));
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
