package edu.brown.cs.student.main.csv;

import java.util.List;

public class StringCreator implements CreatorFromRow<String> {

  // empty constructor
  public StringCreator() {}

  /**
   * turn list of strings into one string object
   *
   * @param row list of strings to represent a row in csv
   * @return one long string of the row feilds
   */
  public String create(List<String> row) {
    return row.toString();
  }
}
