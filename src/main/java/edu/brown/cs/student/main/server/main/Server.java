package edu.brown.cs.student.main.server.main;

import static spark.Spark.after;

import edu.brown.cs.student.main.server.datasource.ACSAPIDatasource;
import spark.Spark;

/**
 * Top-level class that holds our four handlers that allow the user to access the loadcsv,
 * viewcsv, searchcsv, and broadband endpoints.
 */
public class Server {
  public static void main(String[] args)  {
    int port = 3434;
    Spark.port(port);

    after((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "*");
    });

    LoadCSVHandler loader = new LoadCSVHandler();
    ACSAPIDatasource acsapi = new ACSAPIDatasource();
    Spark.get("loadcsv", loader);
    Spark.get("viewcsv", new ViewCSVHandler(loader));
    Spark.get("searchcsv", new SearchCSVHandler(loader));
    Spark.get("broadband", new BroadBandHandler(acsapi));

    Spark.init();
    Spark.awaitInitialization();

  }


}
