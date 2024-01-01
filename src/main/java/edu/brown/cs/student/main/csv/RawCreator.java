package edu.brown.cs.student.main.csv;

import java.util.List;

public class RawCreator implements CreatorFromRow<List<String>> {
  // empty constructor
  public RawCreator() {}

  /**
   * turn list of strings into one string object
   *
   * @param row list of strings to represent a row in csv
   * @return one long string of the row feilds
   */
  public List<String> create(List<String> row) {
    return row;
  }
}
