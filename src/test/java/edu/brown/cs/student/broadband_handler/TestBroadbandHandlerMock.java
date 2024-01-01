package edu.brown.cs.student.broadband_handler;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.DatasourceException;
import edu.brown.cs.student.main.server.datasource.MockAPIDatasource;
import edu.brown.cs.student.main.server.main.BroadBandHandler;
import edu.brown.cs.student.main.server.main.LoadCSVHandler;

import java.util.ArrayList;
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

public class TestBroadbandHandlerMock {

  //instance variables to access throughout class
  private MockAPIDatasource mock;
  private List<List<String>> mockData;

  //constructor sets up mockAPI and mock list value
  public TestBroadbandHandlerMock() {
    this.mock = new MockAPIDatasource();
    //same list for every request in mock
    this.mockData = new ArrayList<>();
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
    mockData.add(list1);
    mockData.add(list2);
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
    Spark.get("broadband", new BroadBandHandler(this.mock));
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
   * tests for broadband request for mocked county and state
   * @throws IOException throws if error processing url
   */
  @Test
  public void testBroadBandHandler() throws IOException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=California&county=Orange%20County,%20California");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    List<List<String>> data = this.mock.getCurrentBroadband("California",
        "Orange County, California");
    Moshi moshi = new Moshi.Builder().build();
    Type listOfStrings = Types.newParameterizedType(List.class, String.class);
    Type listListString = Types.newParameterizedType(List.class, listOfStrings);
    JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listListString);
    String jsonString = jsonAdapter.toJson(data); // preps data for json conversion
    List<List<String>> jsonList = jsonAdapter.fromJson(jsonString);

    Assert.assertEquals(jsonList, this.mockData);

    clientConnection.disconnect();
  }

  /**
   * tests for county code and state code request for mocked county and state
   * @throws IOException throws if error processing url
   */
  @Test
  public void testBroadBandCodes() throws IOException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=California&county=Orange%20County,%20California");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    String countyCode = this.mock.getCountyToCode("California", "Orange County, California");
    Assert.assertEquals(countyCode, "059");

    String stateCode = this.mock.getStateToCode("California");
    Assert.assertEquals(stateCode, "06");
  }


  /**
   * tests for broadband request for random county and state
   * @throws IOException throws if error processing url
   */
  @Test
  public void testBroadBandHandler2() throws IOException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=Virginia&county=Loudoun%20County,%20Virginia");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    List<List<String>> data = this.mock.getCurrentBroadband("Virginia",
        "Loudoun County, Virginia");
    Moshi moshi = new Moshi.Builder().build();
    Type listOfStrings = Types.newParameterizedType(List.class, String.class);
    Type listListString = Types.newParameterizedType(List.class, listOfStrings);
    JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listListString);
    String jsonString = jsonAdapter.toJson(data); // preps data for json conversion
    List<List<String>> jsonList = jsonAdapter.fromJson(jsonString);

    Assert.assertEquals(jsonList, this.mockData);

    clientConnection.disconnect();
  }

  /**
   * tests for county code and state code request for random county and state
   * @throws IOException throws if error processing url
   */
  @Test
  public void testBroadBandCodes2() throws IOException {
    HttpURLConnection clientConnection = tryRequest(
        "broadband?state=Virginia&county=Loudoun%20County,%20Virginia");
    Assert.assertEquals(clientConnection.getResponseCode(), 200); // tests success response

    String countyCode = this.mock.getCountyToCode("Virginia",
        "Loudoun County, Virginia");
    Assert.assertEquals(countyCode, "059");

    String stateCode = this.mock.getStateToCode("Virginia");
    Assert.assertEquals(stateCode, "06");
  }
}
