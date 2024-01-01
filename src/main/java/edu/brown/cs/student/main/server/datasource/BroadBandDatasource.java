package edu.brown.cs.student.main.server.datasource;

import java.io.IOException;
import java.util.List;

/**
 * Interface that defines a broadband data source. Implemented by
 * the ACSAPIDatasource class.
 */
public interface BroadBandDatasource {

  /**
   * converts state name into state code according to specific api datasource
   * @param stateName name of state want to search
   * @return string of state code
   * @throws IOException error reading url
   * @throws DatasourceException error in connecting to client datasource
   */
  String getStateToCode(String stateName) throws IOException, DatasourceException;

  /**
   * converts county name into county code according to specific api datasource
   * @param state state name of county
   * @param county county you want to search for
   * @return county code as a string
   * @throws IOException error reading url
   * @throws DatasourceException error in connecting to client datasource
   */
  String getCountyToCode(String state, String county) throws IOException, DatasourceException;

  /**
   * find broadband access data from given api datasource
   * @param state string of state code
   * @param county string of county code
   * @return List of result data from the api with broadband percent for county, state location
   * @throws IOException error reading url
   * @throws DatasourceException error in connecting to client datasource
   */
  List<List<String>> getCurrentBroadband(String state, String county) throws DatasourceException, IOException;
}

