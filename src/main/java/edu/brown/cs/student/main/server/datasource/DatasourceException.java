package edu.brown.cs.student.main.server.datasource;

/**
 * Exception thrown if there's issues with connecting to the web API's url. Used whenever trying
 * to start a http connection.
 */
public class DatasourceException extends Exception {

  /**
   * Constructor for the exception.
   *
   * @param message - the error message.
   */
  public DatasourceException(String message) {
    super(message);
  }

}
