package edu.brown.cs.student.main.csv;

import java.io.*;
import java.util.*;

public class Searcher<T> {

  // instance variables
  Parser<T> p;
  private boolean h;

  /**
   * 2 argument constructor takes in parser object and if csv has headers
   *
   * @param parser  object to access parsed file to search in
   * @param headers if csv has headers or not
   */
  public Searcher(Parser<T> parser, boolean headers) {
    p = parser;
    h = headers;
  }

  /**
   * method to search csv based on column name -- can only use if have headers
   *
   * @param searchVal     value to search for in csv
   * @param colIdentifier name of column want to search in
   * @return list of T objects that match search
   * @throws IOException              from parseString() method parser class
   * @throws IllegalArgumentException thrown if column name not valid in file
   * @throws FactoryFailureException  from create() method creatorFromRow class
   */
  public List<T> searchByColName(String searchVal, String colIdentifier)
      throws IOException, IllegalArgumentException, FactoryFailureException {
    // follow only if have headers
    if (h) {
      List<List<String>> csvParsed = p.parseString();
      List<T> csvObjects = p.parseObjectsWithHeaders();
      List<String> colIndexList = csvParsed.get(0);

      // get index column name by counting index
      Integer colIndex = null;
      for (int j = 0; j < colIndexList.size(); j++) {
        // case-insensitive for colIdentifier
        if (colIndexList.get(j).equalsIgnoreCase(colIdentifier)) {
          colIndex = j;
          break; // breaks at first instance column name
        }
      }

      //searchByColumnIndex() on colIndex
      List<T> results = this.searchByColIndex(searchVal, colIndex);
      return Collections.unmodifiableList(results);
    } else {
      // throw my own error if doesn't have headers to control error message to be more specific
      throw new IllegalArgumentException(
          "Column name input is not valid because csv does not have headers!");
    }
  }

  /**
   * method to search csv based on column index
   *
   * @param searchVal value to search for in csv
   * @param index     of column want to search in
   * @return list of T objects that match search
   * @throws IOException               from parseString() method parser class
   * @throws IndexOutOfBoundsException thrown if column index not valid in file
   * @throws FactoryFailureException   from create() method creatorFromRow class
   */
  public List<T> searchByColIndex(String searchVal, int index)
      throws IOException, IndexOutOfBoundsException, FactoryFailureException {
    List<T> results = new ArrayList<>();
    List<List<String>> csvParsed = p.parseString();
    try {
      // follow if have headers
      if (h) {
        List<T> csvObjects = p.parseObjectsWithHeaders();
        // sees if any row (except for first) has index value that matches searchVal
        for (int i = 1; i < csvParsed.size(); i++) {
          String str = csvParsed.get(i).get(index);
          // if matches, adds row T object to results
          if (searchVal.equalsIgnoreCase(str)) {
            results.add(csvObjects.get(i - 1));
          }
        }
      }
      // follow if doesn't have headers
      else {
        // same as before, just account for no headers
        List<T> csvObjects = p.parseObjectsWithoutHeaders();
        // start for loop search on first row now no headers
        for (int i = 0; i < csvParsed.size(); i++) {
          String str = csvParsed.get(i).get(index);
          if (searchVal.equalsIgnoreCase(str)) {
            results.add(csvObjects.get(i));
          }
        }
      }
      return Collections.unmodifiableList(results);
    } catch (IndexOutOfBoundsException e) {
      // throw my own error to control error message to be more specific
      throw new IndexOutOfBoundsException("Column index input is not a valid csv row index!");
    }
  }

  /**
   * search all columns for value
   *
   * @param searchVal value to search for
   * @return list of T objects that match search
   * @throws IOException             from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  public List<T> searchAllCol(String searchVal) throws IOException, FactoryFailureException {
    List<T> results = new ArrayList<>();
    List<List<String>> csvParsed = p.parseString();
    // follow if csv has headers
    if (h) {
      List<T> csvObjects = p.parseObjectsWithHeaders();

      // start at first row search
      for (int i = 1; i < csvParsed.size(); i++) {
        for (String str : csvParsed.get(i)) {
          if (searchVal.equalsIgnoreCase(str)) {
            results.add(csvObjects.get(i - 1));
            break; // stop searching within row at first instance searchVal matches a row feild
          }
        }
      }
    }
    // follow if csv doesn't have headers
    else {
      List<T> csvObjects = p.parseObjectsWithoutHeaders();

      // same as before, just now start search at first row
      for (int i = 0; i < csvParsed.size(); i++) {
        for (String str : csvParsed.get(i)) {
          if (searchVal.equalsIgnoreCase(str)) {
            results.add(csvObjects.get(i));
            break;
          }
        }
      }
    }
    return Collections.unmodifiableList(results);
  }
}
