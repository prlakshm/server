package edu.brown.cs.student.main.server.datasource;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squareup.moshi.Types;
import okio.Buffer;

/**
 * A datasource for weather forecasts via NWS API. This class uses the _real_ API to return results.
 * It has no caching in itself, and is focused on working with the real API.
 */
public class ACSAPIDatasource implements BroadBandDatasource {

    /**
     * Constructor for the datasource.
     */
    public ACSAPIDatasource() {}

    /**
     * Returns the map of state names to their number codes to find them in the census data. Called
     * when handling the user's state name query param.
     *
     * @return the map of state name -> code.
     * @throws IOException if there's an issue reading in the URL request
     * @throws DatasourceException if there's an issue connecting to the client.
     */
      public String getStateToCode(String stateName) throws IOException, DatasourceException {
          URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state:*");
          HttpURLConnection clientConnection = connect(requestURL);
          Moshi moshi = new Moshi.Builder().build();
          Type listString = Types.newParameterizedType(List.class, String.class);
          Type listListString = Types.newParameterizedType(List.class, listString);
          JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);
          List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
          clientConnection.disconnect();

          Map<String, String> statesToCodes = new HashMap<>();
          for (List<String> stateRow: body) {
              statesToCodes.put(stateRow.get(0), stateRow.get(1)); // adds all the the states with name -> code
          }
          return statesToCodes.get(stateName);
      }

    /**
     * Returns the number code of a state's county to be used to get its broadband data. Called
     * when handling the user's county name query param.
     *
     * @param state - the state code we're searching within
     * @param county - the county we're looking for within the state.
     *
     * @return the number code of a state's county.
     *
     * @throws IOException if there's issues reading in the URL request.
     * @throws DatasourceException if there's issues connecting to the client.
     */
      public String getCountyToCode(String state, String county) throws IOException, DatasourceException {
          URL requestURL = new URL("https", "api.census.gov",
                  "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"+state);
          HttpURLConnection clientConnection = connect(requestURL);

          Moshi moshi = new Moshi.Builder().build();
          Type listString = Types.newParameterizedType(List.class, String.class);
          Type listListString = Types.newParameterizedType(List.class, listString);
          JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);
          List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
          clientConnection.disconnect();

          Map<String, String> countiesToCodes = new HashMap<>();
          for (List<String> countyRow: body) { // data takes the form of [countyname, state code, county code],
              countiesToCodes.put(countyRow.get(0), countyRow.get(2)); // so we just need the first and third element.
          }
          return countiesToCodes.get(county); // after adding all the counties, return the requested county's code.
      }

      /**
       * Private helper method; throws IOException so different callers can handle differently if
       * needed.
       */
      private static HttpURLConnection connect(URL requestURL) throws DatasourceException, IOException {
          URLConnection urlConnection = requestURL.openConnection();
          if (!(urlConnection instanceof HttpURLConnection)) {
              throw new DatasourceException("unexpected: result of connection wasn't HTTP");
          }
          HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
          clientConnection.connect();
          if (clientConnection.getResponseCode() != 200) {
              throw new DatasourceException("unexpected: API connection not success status " +
                      clientConnection.getResponseMessage());
          }
          return clientConnection;
      }

    /**
     * getCurrentBroadband returns the List<List<String>> obtained from the census data when searching
     * for a specific county's broadband data.
     *
     * @param state - the state we're searching
     * @param county - the county we're searching
     *
     * @return the list of list of strings of the state, county, broadband.
     *
     * @throws DatasourceException if there's issues connecting to the client.
     * @throws IOException if there's issues reading in the url request.
     */
      public List<List<String>> getCurrentBroadband(String state, String county) throws DatasourceException, IOException {
          URL requestURL = new URL("https", "api.census.gov", "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:" + county
                              + "&in=state:" + state);
          HttpURLConnection clientConnection = connect(requestURL);
          Moshi moshi = new Moshi.Builder().build();
          Type listString = Types.newParameterizedType(List.class, String.class);
          Type listListString = Types.newParameterizedType(List.class, listString);
          JsonAdapter<List<List<String>>> adapter = moshi.adapter(listListString);

          List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
          clientConnection.disconnect();
          return body;
      }

}