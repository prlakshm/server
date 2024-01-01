package edu.brown.cs.student.main.server.main;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.server.datasource.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * BroadBandHandler allows the user to search for a specific state & county's broadband percentage.
 * This is through the handle method which handles the user's request, including query params for
 * the state and county.
 */
public class BroadBandHandler implements Route {

  private String stateName;
  private String countyName;

  private BroadBandDatasource datasource;

  /**
   * Constructor for the BroadBandHandler. Called in Server when setting up the broadband endpoint.
   */
  public BroadBandHandler(BroadBandDatasource datasource) {
    this.datasource = datasource;
  }

  /**
   * Handles the user's request to search for a state + county's broadband percent.
   *
   * @param request  - the user's request.
   * @param response - allows us to respond to the user's request.
   * @return a success or failure response as a json string.
   */
  public Object handle(Request request, Response response) {
    try {
      LocalDateTime myDateObj = LocalDateTime.now();
      DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

      String dateTime = myDateObj.format(myFormatObj);

      this.stateName = request.queryParams("state");
      String stateCode = this.datasource.getStateToCode(this.stateName); // getStatesToCodes returns a
      // hashmap of state name -> code
      this.countyName = request.queryParams("county");
      String countyCode = this.datasource.getCountyToCode(stateCode, this.countyName); //same
      // idea with getCountyToCode
      List<List<String>> data = this.datasource.getCurrentBroadband(stateCode, countyCode);

      if (data.isEmpty()) { // if there are no results for the given county, return empty list
        return new BroadbandSuccessResponse("success", new ArrayList<>(), dateTime);
      }
      Moshi moshi = new Moshi.Builder().build();
      Type listOfStrings = Types.newParameterizedType(List.class, String.class);
      Type listListString = Types.newParameterizedType(List.class, listOfStrings);
      JsonAdapter<List<List<String>>> jsonAdapter = moshi.adapter(listListString);
      String jsonString = jsonAdapter.toJson(
          data); // preps json for a list of list of strings, then
      List<List<String>> jsonObject = jsonAdapter.fromJson(
          jsonString); // converts data to a json string,
      // and then finally to a json object
      return new BroadbandSuccessResponse(jsonObject, dateTime).serialize();
    } catch (Exception e) {
      return new BroadbandFailureResponse(this.stateName, this.countyName,
          new ArrayList<>()).serialize();
    }
  }

  /**
   * Success response for Broadband if the user is able to successfully view the state/county's
   * data.
   *
   * @param response_type - in this case, "success"
   * @param data          - the list of list of strings, containing state, county, broadband, and
   *                      timestamp
   */
  public record BroadbandSuccessResponse(String response_type, List<List<String>> data,
                                         String dateTime) {

    /**
     * Constructor for the success response.
     *
     * @param data - the state, county, broadband, and timestamp as a list of lists.
     */
    public BroadbandSuccessResponse(List<List<String>> data, String dateTime) {
      this("success", data, dateTime);
    }

    /**
     * Returns the success response as a json string.
     *
     * @return the response as a json.
     */
    public String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<BroadbandSuccessResponse> adapter = moshi.adapter(
          BroadbandSuccessResponse.class);
      return adapter.toJson(this);
    }
  }

  /**
   * Failure response for broadband, if the user makes a mistake in what they are searching for
   *
   * @param response_type - in this case, "error_bad_request"
   * @param data          - an empty array list.
   */
  public record BroadbandFailureResponse(String response_type, String state, String county,
                                         List<List<String>> data) {

    /**
     * Constructor for the failure response.
     *
     * @param data - in this case, an empty array list
     */
    public BroadbandFailureResponse(String state, String county, List<List<String>> data) {
      this("error_bad_request: make sure your state name and county are correct!" +
          " e.g. state: Illinois; county: Cook County, Illinois.", state, county, data);
    }

    /**
     * Returns the failure response as a json string.
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(BroadbandFailureResponse.class).toJson(this);
    }
  }

}
