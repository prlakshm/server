package edu.brown.cs.student.main.server.main;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.csv.FactoryFailureException;

import spark.Request;
import spark.Response;
import spark.Route;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SearchCSVHandler allows the user to search for a value in a pre-loaded csv file. This is through the handle method,
 * which handles the user's request, including query params for the search type, value to search for, and
 * column identifier (if applicable). This class is dependency injected with the LoadCSVHandler so that
 * our searcher can access the loaded csv data.
 */
public class SearchCSVHandler implements Route {

  private final LoadCSVHandler loader;

  /**
   * Constructor for the SearchCSVHandler.
   *
   * @param loader - the LoadCSVHandler instantiated in Server.
   */
  public SearchCSVHandler(LoadCSVHandler loader){
    this.loader = loader;
  }

  /**
   * Handles the user's request. Uses query params entered into the browser to call methods on the loader's
   * search methods, and then returns the results of the query as a success or failure response.
   *
   * @param request - the user's request
   * @param response - allows us to return a response to the user
   * @return a success or failure response as a json object, with the search results (if there are any).
   */
  public Object handle(Request request, Response response){
    try {
      String searchType = request.queryParams("searchType");
      String searchVal = request.queryParams("searchVal");
      Map<String, Map<String, String>> data;
      if (searchType.equalsIgnoreCase("index")) { // if the user wants to search by col index
        try {
          int columnIdentifier = Integer.parseInt(request.queryParams("columnIdentifier"));
          data = this.loader.searchByIndex(searchVal, columnIdentifier);
        } catch (NumberFormatException e) { // if the user doesn't enter a number
          return new ColIDSearchFailureResponse(searchType).serialize();
        }
      } else if (searchType.equalsIgnoreCase("name")) { // if the user wants to search by col name
          String columnIdentifier = request.queryParams("columnIdentifier");
          data = this.loader.searchByName(searchVal, columnIdentifier);
      } else if (searchType.equalsIgnoreCase("all")) { // if the user wants to search all cols
          data = this.loader.searchAll(searchVal);
      } else {
          return new ColIDSearchFailureResponse(searchType).serialize();
      }
      if (data.isEmpty()) { // if there are no search results but no error, return empty results
        return new SearchSuccessResponse(new HashMap<>()).serialize();
      }
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<Map> jsonAdapter = moshi.adapter(Map.class);
      String jsonString = jsonAdapter.toJson(data); // converts search results to a json,
      Map<String, Object> jsonObject = jsonAdapter.fromJson(jsonString); // then to a map

      return new SearchSuccessResponse(jsonObject).serialize();
    } catch (FileNotFoundException e) {
      return new FileNotFoundSearchFailureResponse().serialize();
    } catch (FactoryFailureException e) {
      return new FactoryFailureSearchFailureResponse().serialize();
    } catch (Exception e) {
      return new IOSearchFailureResponse().serialize();
    }
  }

  /**
   * Success response that gets returned if the user successfully receives search results
   * for their query.
   *
   * @param response_type - in this case, "success"
   * @param data - the search results of the user's query
   */
  public record SearchSuccessResponse(String response_type, Map<String, Object> data) {

    /**
     * Constructor for the success response. Called if the user searches without errors.
     *
     * @param data - the search results of the user's query
     */
    public SearchSuccessResponse(Map<String, Object> data) {
      this("success", data);
    }

    /**
     * Converts the success response to a json to be returned.
     *
     * @return the json-formatted success response, as a string
     */
    public String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<SearchSuccessResponse> adapter = moshi.adapter(
              SearchSuccessResponse.class);
      return adapter.toJson(this);
    }
  }

  /**
   * Failure response if the user didn't properly load a csv before trying to search.
   *
   * @param response_type - in this case, "error_datasource"
   */
  public record FileNotFoundSearchFailureResponse(String response_type) {

    /**
     * Constructor for the failure response.
     */
    public FileNotFoundSearchFailureResponse() {
      this("error_datasource: make sure you loaded your csv properly before trying to search");
    }

    /**
     * Converts the failure response to a json to be returned as a string
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotFoundSearchFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response if the user enters a value other than "index", "all", or "name" for their
   * searchType query param, or if they want to search using index but then input a string
   * for column identifier.
   *
   * @param response_type - in this case, "error_bad_request"
   */
  public record ColIDSearchFailureResponse(String response_type, String searchType) {

    /**
     * Constructor for the failure response.
     */
    public ColIDSearchFailureResponse(String searchType) {
      this("error_bad_request: enter 'index', 'all', or 'name' for your searchType, and a number" +
              " for columnIdentifier if searching by index", searchType);
    }

    /**
     * Returns the failure response as a json.
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(ColIDSearchFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response if there's an issue with the reader, stemming from the Searcher.
   *
   * @param response_type - in this case, "error_bad_request"
   */
  public record IOSearchFailureResponse(String response_type) {

    /**
     * Constructor for the failure response.
     */
    public IOSearchFailureResponse() {
      this("error_bad_request: make sure your data is" +
              " loaded properly before searching and that you include searchVal and searchType queryparams," +
              " and columnIdentifier if searching by column name or index!");
    }

    /**
     * Returns the response as a json string.
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(IOSearchFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response returned if the loader's search methods generate a factory failure exception.
   * @param response_type - in this case, "error_bad_request"
   */
  public record FactoryFailureSearchFailureResponse(String response_type) {

    /**
     * Constructor for the failure response.
     */
    public FactoryFailureSearchFailureResponse() {
      this("error_bad_request: make sure your data" +
              " is loaded properly before searching");
    }

    /**
     * Returns the response as a json string.
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FactoryFailureSearchFailureResponse.class).toJson(this);
    }
  }




}
