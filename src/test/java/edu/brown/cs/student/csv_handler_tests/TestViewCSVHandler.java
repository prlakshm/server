package edu.brown.cs.student.csv_handler_tests;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.MockAPIDatasource;
import edu.brown.cs.student.main.server.main.LoadCSVHandler;

import edu.brown.cs.student.main.server.main.ViewCSVHandler;
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

public class TestViewCSVHandler {

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
        LoadCSVHandler loader = new LoadCSVHandler();
        Spark.get("loadcsv", loader);
        Spark.get("viewcsv", new ViewCSVHandler(loader));
        Spark.awaitInitialization();
    }

    @AfterTest
    public void teardown() {
        Spark.unmap("/viewcsv");
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
     * Tests ViewCSVHandler if all query params are correct.
     *
     * @throws IOException
     */
    @Test
    public void testViewCSVHandler() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/kindergarten.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("viewcsv");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "success");

        String resultMap = "{object1={red=orange, 1=2, sam=jill}, object2={red=yellow, 1=3, sam=beth}, " +
                "object3={red=green, 1=4, sam=beth}, object4={red=blue, 1=5, sam=jeremy}, " +
                "object5={red=indigo, 1=6, sam=ellie}, object6={red=violet, 1=7, sam=sam}}";

        Assert.assertEquals(test.get("data").toString(), resultMap);

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests ViewCSVHandler if user doesn't load a csv first.
     *
     * @throws IOException
     */
    @Test
    public void testViewCSVHandler2() throws IOException {
        HttpURLConnection clientConnection = tryRequest("viewcsv");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject); // Update to handle JSON objects
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_datasource: make sure your csv is loaded " +
                "properly before viewing");

        clientConnection.disconnect();
    }

    /**
     * Tests ViewCSVHandler if user tries to load a csv first, fails, then tries to view the csv.
     *
     * @throws IOException
     */
    @Test
    public void testViewCSVHandler3() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/yes.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection2 = tryRequest("viewcsv");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));

        Assert.assertEquals(test.get("response_type"), "error_datasource: make sure your csv is loaded " +
                "properly before viewing");

        clientConnection.disconnect();
        clientConnection2.disconnect();
    }

    /**
     * Tests ViewCSVHandler if user loads a csv successfully, then loads a second csv successfully. The
     * most recent csv should be viewable.
     *
     * @throws IOException
     */
    @Test
    public void testViewCSVHandler4() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=sample/shakespeare.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response
        HttpURLConnection clientConnection2 = tryRequest("loadcsv?filepath=sample/kindergarten.csv&hasHeaders=true");
        Assert.assertEquals(clientConnection2.getResponseCode(), 200); // tests success response

        HttpURLConnection clientConnection3 = tryRequest("viewcsv");
        Assert.assertEquals(clientConnection3.getResponseCode(), 200); // tests success response

        Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
        Map<String, Object> test = adapter.fromJson(new Buffer().readFrom(clientConnection3.getInputStream()));
        Assert.assertEquals(test.get("response_type"), "success");
        String resultMap = "{object1={red=orange, 1=2, sam=jill}, object2={red=yellow, 1=3, sam=beth}, " +
                "object3={red=green, 1=4, sam=beth}, object4={red=blue, 1=5, sam=jeremy}, " +
                "object5={red=indigo, 1=6, sam=ellie}, object6={red=violet, 1=7, sam=sam}}";

        Assert.assertEquals(test.get("data").toString(), resultMap);
        clientConnection.disconnect();
        clientConnection2.disconnect();;
        clientConnection3.disconnect();
    }
}