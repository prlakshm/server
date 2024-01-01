package edu.brown.cs.student.main.server.main;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * ViewCSVHandler allows the user to view a pre-loaded csv file. This is through the handle method,
 * which handles the user's request. This class is dependency injected with the LoadCSVHandler
 * so that viewcsv can access the loaded csv data.
 */
public class ViewCSVHandler implements Route {

  private LoadCSVHandler loader;

  /**
   * Constructor for ViewCSVHandler, called in the server class when setting up
   * the viewcsv endpoint.
   *
   * @param loader - the LoadCSVHandler instantiated in Server
   */
  public ViewCSVHandler(LoadCSVHandler loader) {
    this.loader = loader;
  }

  /**
   * This method handles requests sent in by the user to view the csv.
   *
   * @param request - the request sent in by the user
   * @param response - allows us to respond to the user's request
   *
   * @return - a success response or failure response
   */
  public Object handle(Request request, Response response) {
    try {
      Map<String, Map<String, String>> objects = loader.viewMap(); // returns the data as a map from loader,
      Moshi moshi = new Moshi.Builder().build();         // since all the parsed data is stored in loader
      JsonAdapter<Map> jsonAdapter = moshi.adapter(Map.class);
      String jsonString = jsonAdapter.toJson(objects);
      Map<String, Object> jsonObject = jsonAdapter.fromJson(jsonString);
      return new ViewSuccessResponse(jsonObject).serialize();
    } catch (Exception e) {
      return new ViewFailureResponse().serialize();
    }
  }

  /**
   * Success response if the user is able to view the csv.
   *
   * @param response_type - in this case, "success"
   * @param data - the csv data as a Map of String (object number) to its data
   */
  public record ViewSuccessResponse(String response_type, Map<String, Object> data) {

    /**
     * Constructor for the success response.
     *
     * @param data - the csv data as a Map of String (object number) to its data
     */
    public ViewSuccessResponse(Map<String, Object> data) {
      this("success", data);
    }

    /**
     * Converts the success response to a json to be returned.
     * @return
     */
    public String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<ViewCSVHandler.ViewSuccessResponse> adapter = moshi.adapter(
          ViewCSVHandler.ViewSuccessResponse.class);
      return adapter.toJson(this);
    }
  }

  /**
   * Failure response if the user is unable to view the csv. This happens because
   * the file wasn't properly loaded.
   *
   * @param response_type - in this case, "error_datasource"
   */
  public record ViewFailureResponse(String response_type) {

    /**
     * Constructor for the failure response.
     */
    public ViewFailureResponse() {
      this("error_datasource: make sure your csv is loaded properly before viewing");
    }

    /**
     * Converts the failure response to a json to be returned.
     *
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(ViewCSVHandler.ViewFailureResponse.class).toJson(this);
    }
  }

}
