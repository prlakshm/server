package edu.brown.cs.student.main.server.main;


import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.csv.FactoryFailureException;
import edu.brown.cs.student.main.csv.RawCreator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.brown.cs.student.main.csv.Searcher;
import spark.Request;
import spark.Response;
import spark.Route;
import edu.brown.cs.student.main.csv.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * LoadCSVHandler is in charge of loading CSV files passed into the loadcsv endpoint by the user as a request.
 * The class stores the data in the CSV as parsed Strings. Since the data is stored in this class, LoadCSVHandler
 * also has control of calling Searcher depending on requests to searchcsv, and control over the map returned
 * if the user wants to view the CSV.
 */
public class LoadCSVHandler implements Route {
  private Boolean hasHeaders;
  private String filePath;
  private List<List<String>> objects;
  private Parser<List<String>> parser;
  private List<String> headers;

  /**
   * Constructor for LoadCSVHandler. Called in Server class and has no parameters.
   */
  public LoadCSVHandler() {}

  /**
   * This method is called when a user accesses our server's loadcsv endpoint. If formatted correctly,
   * the method should successfully load a csv file, which means the file will be parsed as a
   * list of list of Strings.
   *
   * @param request - the request made by the user.
   * @param response - allows the response returned to the user to be modified
   *
   * @return a success or failure message, along with the filepath
   */
  @Override
  public Object handle(Request request, Response response) {
    try {
      this.filePath = request.queryParams("filepath");
      String h = request.queryParams("hasHeaders");

      if (h.equalsIgnoreCase("true")) { // setting boolean value based on user's request
        this.hasHeaders = true;
      } else if (h.equalsIgnoreCase("false")) {
        this.hasHeaders = false;
      } else {
        return new HeaderLoadFailureResponse(this.filePath, h).serialize();
      }

      BufferedReader reader = new BufferedReader(new FileReader(
              "data/" + this.filePath)); // allows us to stay within our data folder, not our whole
      RawCreator creator = new RawCreator();      // directory
      this.parser = new Parser<>(creator, reader);

      this.objects = new ArrayList<>(parser.parseObjectsWithoutHeaders());
      if (this.hasHeaders) {
        this.headers = this.objects.get(0);
        this.objects.remove(0); // gets rid of the first row (headers) to return just the data
      }                               // to the user
      return new LoadSuccessResponse(this.filePath).serialize();
    } catch (FileNotFoundException e) {
      return new FileNotFoundLoadFailureResponse(this.filePath).serialize();
    } catch (FactoryFailureException e) {
      return new FactoryFailureLoadFailureResponse(this.filePath).serialize();
    } catch (JsonDataException e) {
      return new JsonDataLoadFailureResponse(this.filePath).serialize();
    } catch (Exception e) {
      return new IOLoadFailureResponse(this.filePath).serialize();
    }
  }


  /**
   * getMapWithHeaders is a private helper method that returns a list of lists of data as a hashmap.
   * This method is called when searching or viewing a csv with headers, so objects' elements will
   * align with their field names from the headers.
   *
   * @param data - a List<List<String>> of parsed data.
   * @return the data as a HashMap, with column headers matched with their values for each respective object
   */
  private Map<String, Map<String, String>> getMapWithHeaders(List<List<String>> data) {
    Map<String, Map<String, String>> map = new LinkedHashMap<>();
    if (data.isEmpty()) {
      return map;
    }
    int rowNum = data.size();
    int colNum = data.get(0).size();
    for (int i = 1; i <= rowNum; i++) { // start at 1 because field 1 is at index 0 but we want
      LinkedHashMap<String, String> rowObject = new LinkedHashMap<>();  // it to say "field 1"
      for (int j = 1; j <= colNum; j++) {                               // for the object
        rowObject.put(this.headers.get(j - 1), data.get(i - 1).get(j - 1)); // subtracting 1 accounts
      }                                                                   // for index discrepencies
      map.put("object" + i, rowObject);
    }
    return map;
  }

  /**
   * getMapWithHeaders is a private helper method that returns a list of lists of data as a hashmap.
   * This method is called when searching or viewing a csv without headers, so objects' elements will
   * align with its number field, starting with 1.
   *
   * @param data - a List<List<String>> of parsed data.
   * @return the data as a HashMap, with column indices/field numbers matched with their values for each respective object
   */
  private Map<String, Map<String, String>> getMapWithoutHeaders(List<List<String>> data) {
    Map<String, Map<String, String>> map = new LinkedHashMap<>();
    if (data.isEmpty()) {
      return map;
    }
    int rowNum = data.size();
    int colNum = data.get(0).size();
    for (int i = 1; i <= rowNum; i++) {
      LinkedHashMap<String, String> rowObject = new LinkedHashMap<>();
      for (int j = 1; j <= colNum; j++) {
        rowObject.put("field" + j, data.get(i - 1).get(j - 1)); // we use "field" + number because we
      }                                                         // don't have a header name
      map.put("object" + i, rowObject);
    }
    return map;
  }

  /**
   * viewMap returns the parsed list of data as an unmodifiable map. It is called in
   * the viewcsv handler.
   *
   * @return the CSV data as an unmodifiable map from headers/field number to header/field vaule.
   */
  public Map<String, Map<String, String>> viewMap() {
    if(this.hasHeaders) {
      return Collections.unmodifiableMap(this.getMapWithHeaders(this.objects));
    }
    return Collections.unmodifiableMap(this.getMapWithoutHeaders(this.objects));
  }

  /**
   * searchByIndex searches through loaded csv data and returns a Map with the user's search value as
   * the key and the number index they used as the value. This method is called in the SearchCSVHandler
   * class since this class has access to the parser, boolean, and objects.
   *
   * @param searchVal - the String value that the user is requesting a search for
   * @param columnIdentifier - the number of the column they want to search
   *
   * @return the data containing the searchVal, as a Map of the object(s) and its values.
   * @throws IOException
   * @throws IndexOutOfBoundsException
   * @throws FactoryFailureException
   */
  public Map<String, Map<String, String>> searchByIndex(String searchVal, int columnIdentifier)
      throws IOException, IndexOutOfBoundsException, FactoryFailureException {
    Searcher<List<String>> searcher = new Searcher<>(this.parser, this.hasHeaders);
    List<List<String>> data = searcher.searchByColIndex(searchVal, columnIdentifier);
    if (this.hasHeaders){
      return Collections.unmodifiableMap(this.getMapWithHeaders(data));
    }
    return Collections.unmodifiableMap(this.getMapWithoutHeaders(data));
  }

  /**
   * searchByName searches through loaded csv data and returns a Map with the user's search value as
   * the key and the column name they used as the value. This method is called in the SearchCSVHandler
   * class since this class has access to the parser, boolean, and objects.
   *
   * @param searchVal - the String value that the user is requesting a search for
   * @param columnIdentifier - the name of the column they want to search
   *
   * @return the data containing the searchVal, as a Map of the object(S) and its values.
   * @throws IOException
   * @throws IndexOutOfBoundsException
   * @throws FactoryFailureException
   */
  public Map<String, Map<String, String>> searchByName(String searchVal, String columnIdentifier)
      throws IOException, IndexOutOfBoundsException, FactoryFailureException {
    Searcher<List<String>> searcher = new Searcher<>(this.parser, this.hasHeaders);
    List<List<String>> data = searcher.searchByColName(searchVal, columnIdentifier);
    if (this.hasHeaders){
      return Collections.unmodifiableMap(this.getMapWithHeaders(data));
    }
    return Collections.unmodifiableMap(this.getMapWithoutHeaders(data));

  }

  /**
   * searchAll searches through loaded csv data and returns a Map with the user's search value as
   * the key and the object's properties as a value. This method is called in the SearchCSVHandler
   * class since this class has access to the parser, boolean, and objects.
   *
   * @param searchVal - the String value that the user is requesting a search for
   *
   * @return the data containing the searchVal, as a Map of the object(S) and its values.
   * @throws IOException if there's an error in the searcher's search methods
   * @throws FactoryFailureException if there's an error parsing the data within the searcher
   */
  public Map<String, Map<String, String>> searchAll(String searchVal)
      throws IOException, FactoryFailureException {
    Searcher<List<String>> searcher = new Searcher<>(this.parser, this.hasHeaders);
    List<List<String>> data = searcher.searchAllCol(searchVal);

    if (this.hasHeaders){
      return Collections.unmodifiableMap(this.getMapWithHeaders(data));
    }
    return Collections.unmodifiableMap(this.getMapWithoutHeaders(data));
  }

  /**
   * This record stores the response type and filepath returned to the user when we want a success response.
   *
   * @param response_type - in this case, "success"
   * @param filepath - the file path the user requested in loadcsv.
   */
  public record LoadSuccessResponse(String response_type, String filepath) {

    /**
     * Called in handle if the code executes without any errors or unexpected results.
     *
     * @param filepath - the file path the user requested when loading the csv.
     */
    public LoadSuccessResponse(String filepath) {
      this("success", filepath);
    }

    /**
     * Converts the object to a json.
     *
     * @return - the success message as a json.
     */
    public String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<LoadSuccessResponse> adapter = moshi.adapter(LoadSuccessResponse.class);
      return adapter.toJson(this);
    }
  }

  /**
   * Failure response called in the handle if the user sends in something other than "true" or "false" for the
   * hasHeaders boolean
   *
   * @param response_type - in this case, "error_bad_request"
   * @param filepath - the filepath of the csv
   * @param hasHeaders - string that should be either "true" or "false -- in this case something else
   */
  public record HeaderLoadFailureResponse(String response_type, String filepath, String hasHeaders) {

    /**
     * Constructor for the failure response record
     *
     * @param filepath - the filepath of the csv
     * @param hasHeaders - string that should be either "true" or "false" -- in this case something else
     */
    public HeaderLoadFailureResponse(String filepath, String hasHeaders) {
      this("error_bad_request: enter true or false for hasHeaders query param", filepath, hasHeaders);
    }

    /**
     * Converts the failure response to a Json, called when the failure response is
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(HeaderLoadFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response called in the handle if the file requested to be loaded by the user couldn't
   * be found in the data/ folder
   *
   * @param response_type - in this case, "error_datasource"
   * @param filepath - the filepath the user requested
   */
  public record FileNotFoundLoadFailureResponse(String response_type, String filepath) {

    /**
     * Constructor for the failure response record
     *
     * @param filepath - the filepath of the csv
     */
    public FileNotFoundLoadFailureResponse(String filepath) {
      this("error_datasource: make sure your csv's filepath is correct and within the data/ folder", filepath);
    }

    /**
     * Converts the failure response to a Json, called when the failure response is
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FileNotFoundLoadFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response called in the handle if there is an error parsing data into lists of lists
   * of strings while loading the data
   *
   * @param response_type - in this case, "error_bad_request"
   * @param filepath - the filepath of the csv
   */
  public record FactoryFailureLoadFailureResponse(String response_type, String filepath) {

    /**
     * Constructor for the failure response record
     *
     * @param filepath - the filepath of the csv
     */
    public FactoryFailureLoadFailureResponse(String filepath) {
      this("error_bad_request: make sure there's no issues with your csv data!!", filepath);
    }

    /**
     * Converts the failure response to a Json, called when the failure response is
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(FactoryFailureLoadFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response called in the handler if there is an error converting data to/from json
   *
   * @param response_type - in this case, "error_bad_json"
   * @param filepath - the filepath of the csv
   */
  public record JsonDataLoadFailureResponse(String response_type, String filepath) {

    /**
     * Constructor for the failure response, called when the failure response is
     *
     * @param filepath - the filepath of the csv
     */
    public JsonDataLoadFailureResponse(String filepath) {
      this("error_bad_json: make sure there's no issues with your csv data",
              filepath);
    }

    /**
     * Converts the failure response to a Json, called when the failure response is
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(JsonDataLoadFailureResponse.class).toJson(this);
    }
  }

  /**
   * Failure response called in the handler if there is an error with the reader passed into the
   * parser while loading the csv
   *
   * @param response_type - in this case, "error_bad_request"
   * @param filepath - the filepath of the csv
   */
  public record IOLoadFailureResponse(String response_type, String filepath) {

    /**
     * Constructor for the failure response, called when the failure response is
     *
     * @param filepath - the filepath of the csv
     */
    public IOLoadFailureResponse(String filepath) {
      this("error_bad_request: make sure you include 'true' or 'false' for your hasHeaders query param!" , filepath);
    }

    /**
     * Converts the failure response to a Json, called when the failure response is
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(IOLoadFailureResponse.class).toJson(this);
    }
  }


}
