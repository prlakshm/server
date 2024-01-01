package edu.brown.cs.student.main.server.datasource;

import java.util.ArrayList;
import java.util.List;

public class MockAPIDatasource implements BroadBandDatasource{

  /**
   * mock state code
   * @param stateName any state name
   * @return the same state code "06" for any state input
   */
  public String getStateToCode(String stateName) {
    return "06";
  }

  /**
   * mock county code
   * @param state state name can be any
   * @param county county name can be any
   * @return same county code "059" for any inputs
   */
  public String getCountyToCode(String state, String county) {
   return "059";
  }

  /**
   * mock broadband data
   * @param state any state name
   * @param county any county name
   * @return returns same broadband data for Orange County, California regardless of inputs
   */
  public List<List<String>> getCurrentBroadband(String state, String county) {
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

    return listOfLists;
  }

}
