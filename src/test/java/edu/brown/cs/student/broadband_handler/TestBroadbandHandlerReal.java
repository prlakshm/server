package edu.brown.cs.student.broadband_handler;

import static spark.Spark.after;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.ACSAPIDatasource;
import edu.brown.cs.student.main.server.datasource.DatasourceException;
import edu.brown.cs.student.main.server.main.BroadBandHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import spark.Spark;

public class TestBroadbandHandlerReal {

  //instance variables to access through program
  private ACSAPIDatasource real;

  //constructor instantiates ASC API object
  public TestBroadbandHandlerReal() {
    this.real = new ACSAPIDatasource();
  }

  /**
   * sets up new port
   */
  @BeforeSuite
  public static void setupOnce() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);

    after((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "*");


    });
  }

  /**
   * for every test, send a new broadband request
   */
  @BeforeTest
  public void setup() {
    Spark.get("broadband", new BroadBandHandler(this.real));
    Spark.awaitInitialization();


  }

  /**
   * shut down broadband request
   */
  @AfterTest
  public void teardown() {
    Spark.unmap("/broadband");

    Spark.awaitStop();
  }

  /**
   * host a fake url connection
   * @param apiCall url endpoint of fake connection
   * @return clientConnection with response
   * @throws IOException throws if error processing url
   */
  static private HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * tests for successful data retrieval
   * @throws IOException throws if error processing url
   * @throws DatasourceException error in connecting to client datasource
   */
  @Test
  public void testBroadBandHandler() throws IOException, DatasourceException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=California&county=Orange%20County,%20California");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    String stateCode = this.real.getStateToCode("California");
    String countyCode = this.real.getCountyToCode(stateCode, "Orange County, California");
    List<List<String>> data = this.real.getCurrentBroadband(stateCode,
        countyCode);
    Moshi moshi = new Moshi.Builder().build();
    Type listOfStrings = Types.newParameterizedType(List.class, String.class);
    Type listListString = Types.newParameterizedType(List.class, listOfStrings);
    JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listListString);
    String jsonString = jsonAdapter.toJson(data); // preps data for json conversion
    List<List<String>> jsonList = jsonAdapter.fromJson(jsonString);

    //expected results
    List<List<String>> listOfLists = new ArrayList<>();
    // Create the inner lists
    List<String> list1 = new ArrayList<>();
    list1.add("NAME");
    list1.add("S2802_C03_022E");
    list1.add("state");
    list1.add("county");

    List<String> list2 = new ArrayList<>();
    list2.add("Orange County, California");
    list2.add("93.0");
    list2.add("06");
    list2.add("059");

    // Add the inner lists to the outer list
    listOfLists.add(list1);
    listOfLists.add(list2);

    Assert.assertEquals(jsonList, listOfLists);

    clientConnection.disconnect();
  }

  /**
   * tests unsuccessful data retrieval with error code if given invalid county
   * @throws IOException throws if error processing url
   * @throws DatasourceException error in connecting to client datasource
   */
  @Test
  public void testBroadBandHandlerInvalid() throws IOException, DatasourceException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=California&county=Orange%20County");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter(mapStringObject);
    Map<String, Object> jsonObject = jsonAdapter.fromJson(
        new Buffer().readFrom(clientConnection.getInputStream()));

    Assert.assertEquals(jsonObject.get("response_type"),
        "error_bad_request: make sure your state name and county are correct! e.g. state: Illinois; county: Cook County, Illinois.");

    clientConnection.disconnect();
  }

  /**
   * tests unsuccessful data retrieval with error code if given invalid state
   * @throws IOException throws if error processing url
   * @throws DatasourceException error in connecting to client datasource
   */
  @Test
  public void testBroadBandHandlerInvalid2() throws IOException, DatasourceException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=Spain&county=Orange%20County,%20California");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter(mapStringObject);
    Map<String, Object> jsonObject = jsonAdapter.fromJson(
        new Buffer().readFrom(clientConnection.getInputStream()));

    Assert.assertEquals(jsonObject.get("response_type"),
        "error_bad_request: make sure your state name and county are correct! e.g. state: Illinois; county: Cook County, Illinois.");

    clientConnection.disconnect();
  }

  /**
   * tests for unsuccessful input if forget param state or county
   * @throws IOException throws if error processing url
   * @throws DatasourceException error in connecting to client datasource
   */
  @Test
  public void testBroadBandHandlerInvalid3() throws IOException, DatasourceException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=California");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter(mapStringObject);
    Map<String, Object> jsonObject = jsonAdapter.fromJson(
        new Buffer().readFrom(clientConnection.getInputStream()));

    Assert.assertEquals(jsonObject.get("response_type"),
        "error_bad_request: make sure your state name and county are correct! e.g. state: Illinois; county: Cook County, Illinois.");

    clientConnection.disconnect();
  }


}
