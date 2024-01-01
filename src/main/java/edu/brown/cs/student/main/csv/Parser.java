package edu.brown.cs.student.main.csv;

import java.io.*;
import java.util.*;

public class Parser<T> {

  // instance variables
  private CreatorFromRow<T> c;
  private BufferedReader br;

  private List<List<String>> csvParsed;

  /**
   * constructor takes in 3 arguments and parses file into 2D array of strings
   *
   * @param convertor creatorFromRow object that converts rows csv to T objects
   * @param reader any type of reader object from Reader abstract class
   * @throws IOException throws if error in reading file and handled in main
   */
  public Parser(CreatorFromRow<T> convertor, Reader reader) throws IOException {
    c = convertor;
    br = new BufferedReader(reader);
    csvParsed = new ArrayList<>();

    // file read in constructor so all methods can access and reader doesn't have to keep rereading
    String line = br.readLine();
    while (line != null) {
      // split row into list of strings from regex
      List<String> row =
          new ArrayList<>(
              Arrays.asList(line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))")));
      // remove white spaces
      List<String> trimmedRow = new ArrayList<>();
      for (String str : row) {
        trimmedRow.add(str.trim());
      }
      csvParsed.add(trimmedRow);
      line = br.readLine();
    }
    br.close();
  }

  /**
   * method returns parsed csv
   *
   * @return parsed csv represented as 2D array of list<list<string>>
   */
  public List<List<String>> parseString() {
    return Collections.unmodifiableList(csvParsed);
  }

  /**
   * parses each row in csv as objects
   *
   * @return list of objects represented by each csv row
   * @throws FactoryFailureException throws if error in converting into objects by creatorFromRow
   *     object
   */
  public List<T> parseObjectsWithoutHeaders() throws FactoryFailureException {
    List<List<String>> csvStrings = this.parseString();
    List<T> csvObjects = new ArrayList<>();

    // converts each row into a T object based on creatorFromRow object used
    for (List<String> r : csvStrings) {
      try {
        csvObjects.add(c.create(r));
      } catch (FactoryFailureException e) {
        // use own error message so can print specific reason why error occurred
        throw new FactoryFailureException("Could not parse row into object!", r);
      }
    }
    return Collections.unmodifiableList(csvObjects);
  }

  /**
   * parses each row in csv as objects but skips first row
   *
   * @return list of objects represented by each csv row
   * @throws FactoryFailureException throws if error in converting into objects by creatorFromRow
   *     object
   */
  public List<T> parseObjectsWithHeaders() throws FactoryFailureException {
    List<List<String>> csvStrings = this.parseString();
    List<T> csvObjects = new ArrayList<>();

    // use sublist to account for header row -- skip first row
    for (List<String> r : csvStrings.subList(1, csvStrings.size())) {
      try {
        csvObjects.add(c.create(r));
      } catch (FactoryFailureException e) {
        // use own error message so can print specific reason why error occurred
        throw new FactoryFailureException("Could not parse row into object!", r);
      }
    }
    return Collections.unmodifiableList(csvObjects);
  }
}
