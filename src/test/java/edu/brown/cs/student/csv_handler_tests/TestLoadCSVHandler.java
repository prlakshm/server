package edu.brown.cs.student.csv_handler_tests;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.MockAPIDatasource;
import edu.brown.cs.student.main.server.main.LoadCSVHandler;

import okio.Buffer;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.after;

public class TestLoadCSVHandler {

    @BeforeSuite
    public static void setupOnce() {
        Spark.port(0);
        Logger.getLogger("").setLevel(Level.WARNING);

        after((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "*");
        });
    }


    @BeforeTest
    public void setup() {

        MockAPIDatasource mock = new MockAPIDatasource();

        Spark.get("loadcsv", new LoadCSVHandler());
        Spark.awaitInitialization();


    }

    @AfterTest
    public void teardown() {
        Spark.unmap("/loadcsv");

        Spark.awaitStop();
    }


    static private HttpURLConnection tryRequest(String apiCall) throws IOException {
        URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
        HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

        clientConnection.connect();
        return clientConnection;
    }

    /**
     * Tests LoadCSVHandler if all query params are correct.
     * @throws IOException
     */
    @Test
    public void testLoadCSVHandler() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=stars/ten-star.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");
        Assert.assertEquals(test.get("filepath"), "stars/ten-star.csv");

        clientConnection.disconnect();
    }

    /**
     * Tests LoadCSVHandler if a file does not exist in the data folder.
     * @throws IOException
     */
    @Test
    public void testLoadCSVHandler2() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=stars/ten-stars.csv&hasHeaders=true");

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_datasource: make sure your csv's filepath is correct" +
                " and within the data/ folder");
        Assert.assertEquals(test.get("filepath"), "stars/ten-stars.csv");

        clientConnection.disconnect();
    }

    /**
     * Tests LoadCSVHandler with a csv that doesn't contain headers
     * @throws IOException
     */
    @Test
    public void testLoadCSVHandler3() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/shakespeare.csv&hasHeaders=false");

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");
        Assert.assertEquals(test.get("filepath"), "sample/shakespeare.csv");

        clientConnection.disconnect();
    }

    /**
     * Tests LoadCSVHandler when the user doesn't include the hasHeaders boolean. Also tests two
     * connections in a row.
     * @throws IOException
     */
    @Test
    public void testLoadCSVHandler4() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=yes");

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_bad_request: make sure you include 'true' " +
                "or 'false' for your hasHeaders query param!");
        Assert.assertEquals(test.get("filepath"), "yes");


        HttpURLConnection clientConnection2 = tryRequest("loadcsv?filepath=sample/shakespeare.csv");
        Map<String, Object> test2 = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));
        Assert.assertEquals(test2.get("response_type"), "error_bad_request: make sure you include 'true' " +
                "or 'false' for your hasHeaders query param!");
        Assert.assertEquals(test2.get("filepath"), "sample/shakespeare.csv");

        clientConnection.disconnect();
    }

    /**
     * Testing LoadCSVHandler when the user includes only a parameter for hasHeaders.
     * @throws IOException
     */
    @Test
    public void testLoadCSVHandler5() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?hasHeaders=true");

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_datasource: make sure your csv's filepath is correct" +
                " and within the data/ folder");
        Assert.assertNull(test.get("filepath"));

    }

    /**
     * Tests LoadCSVHandler when there is neither search param included.
     * @throws IOException
     */
    @Test
    public void testLoadCSVHandler6() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv");

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_bad_request: make sure you include 'true'"
                + " or 'false' for your hasHeaders query param!");
        Assert.assertNull(test.get("filepath"));
    }




}
