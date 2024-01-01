package edu.brown.cs.student.csv_handler_tests;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.MockAPIDatasource;
import edu.brown.cs.student.main.server.main.LoadCSVHandler;

import edu.brown.cs.student.main.server.main.SearchCSVHandler;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.after;

public class TestSearchCSVHandler {

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
        LoadCSVHandler loader = new LoadCSVHandler();
        Spark.get("loadcsv", loader);
        Spark.get("searchcsv", new SearchCSVHandler(loader));
        Spark.awaitInitialization();
    }

    @AfterTest
    public void teardown() {
        Spark.unmap("/searchcsv");
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
     * Tests SearchCSVHandler searching all cols if all query params are correct.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/kindergarten.csv&hasHeaders=false");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("searchcsv?searchType=all&searchVal=red");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");

        String results = "{object1={field1=red, field2=1, field3=sam}}";

        Assert.assertEquals(test.get("data").toString(), results);
        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests SearchCSVHandler if user doesn't load a csv first.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler2() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/kindergarten.csv&hasHeaders=false");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response
        HttpURLConnection clientConnection2 = tryRequest("searchcsv");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_bad_request: make sure your data is" +
                " loaded properly before searching and that you include searchVal and searchType queryparams, and " +
                "columnIdentifier if searching by column name or index!");

        clientConnection.disconnect();
        clientConnection2.disconnect();

    }

    /**
     * Tests SearchCSVHandler if user forgets any query params.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler3() throws IOException {
        HttpURLConnection clientConnection = tryRequest("searchcsv");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_bad_request: make sure your data is " +
                "loaded properly before searching and that you include searchVal and searchType queryparams, and " +
                "columnIdentifier if searching by column name or index!");

        clientConnection.disconnect();
    }

    /**
     * Tests SearchCSVHandler searching all by index if missing a queryparam.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler4() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/kindergarten.csv&hasHeaders=false");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("searchcsv?searchType=index&searchVal=1");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_bad_request: enter 'index', 'all', or 'name' " +
                "for your searchType, and a number for columnIdentifier if searching by index");

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests SearchCSVHandler searching all by index if everything is correct.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler5() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/kindergarten.csv&hasHeaders=false");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("searchcsv?searchType=index&searchVal=3&columnIdentifier=1");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");
        String results = "{object1={field1=yellow, field2=3, field3=beth}}";
        Assert.assertEquals(test.get("data").toString(), results);

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests SearchCSVHandler searching by col name if everything is correct.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler6() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=stars/ten-star.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("searchcsv?searchType=name&searchVal=Sol&columnIdentifier=ProperName");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");
        String results = "{object1={StarID=0, ProperName=Sol, X=0, Y=0, Z=0}}";
        Assert.assertEquals(test.get("data").toString(), results);

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests SearchCSVHandler searching for a value that doesn't exist in the data.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler7() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=stars/ten-star.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("searchcsv?searchType=all&searchVal=cheese");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");
        String results = "{}";
        Assert.assertEquals(test.get("data").toString(), results);

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests SearchCSVHandler searching with an invalid SearchType.
     *
     * @throws IOException
     */
    @Test
    public void testSearchCSVHandler8() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=stars/ten-star.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("searchcsv?searchType=slay&searchVal=sol");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_bad_request: enter 'index', 'all', or " +
                "'name' for your searchType, and a number for columnIdentifier if searching by index");

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }
}