package edu.brown.cs.student.main.csv;

import java.io.*;
import java.util.*;

/** The Main class of our project. This is where execution begins. */
public final class Main {

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private Main(String[] args) {}

  private void run() {
    Scanner myScanner = new Scanner(System.in);

    System.out.println("Please input filepath to csv from the repository root " +
                      "\"server-prlakshm-emilywang188\\data\" folder:");
    String filepath = myScanner.nextLine(); // Read user input for csv filepath

    System.out.println("Please input (T/F) is csv has headers:");
    String headers = myScanner.nextLine(); // Read user input for if csv has headers
    Boolean h = null;
    if (headers.equalsIgnoreCase("T")) {
      h = true;
    } else if (headers.equalsIgnoreCase("F")) {
      h = false;
    } else {
      // terminate program if invalid input
      System.err.println("Invalid input");
      System.exit(1);
    }

    try {
      // parse can use user's own Reader object, the default is a BufferedReader object
      BufferedReader bf = new BufferedReader(new FileReader("C:\\Users\\prana\\Documents\\GitHub\\server-prlakshm-emilywang188\\data\\" + filepath));
      // parse can use user's own CreatorFromRow object, the default is a StringCreator object
      StringCreator strCreator = new StringCreator();
      Parser p = new Parser(strCreator, bf);
      Searcher s = new Searcher(p, h);

      System.out.println("Please input a value to search for:");
      String searchVal = myScanner.nextLine(); // Read user input for search value

      System.out.println(
          "Please input a column identifier option. If you would like to search by column name, "
              + "input \"name\". \nIf you would like to search by column index (with 0 being the leftmost "
              + "column), input \"index\". \nIf you would like to search all columns, input \"all\".");
      String colIdentifierOpt = myScanner.nextLine(); // Read user input for how to search

      if (colIdentifierOpt.equalsIgnoreCase("name")) {
        System.out.println("Please input a column name to search in:");
        try {
          String colName = myScanner.nextLine(); // Read user input for column name to search in
          List<String> results = s.searchByColName(searchVal, colName);
          System.out.println(results);
        } catch (Exception e) {
          // terminate program if invalid input and print appropriate message
          System.err.println(e.getMessage());
          System.exit(1);
        }

      } else if (colIdentifierOpt.equalsIgnoreCase("index")) {
        System.out.println("Please input a column index to search in (starts with 0):");
        try {
          int colIndex = myScanner.nextInt(); // Read user input for column index to search in
          List<String> results = s.searchByColIndex(searchVal, colIndex);
          System.out.println(results);
        } catch (Exception e) {
          // terminate program if invalid input and print appropriate message
          System.err.println(e.getMessage());
          System.exit(1);
        }

      } else if (colIdentifierOpt.equalsIgnoreCase("all")) {
        try {
          List<String> results = s.searchAllCol(searchVal); // searches all columns for value
          System.out.println(results);
        } catch (Exception e) {
          // terminate program if invalid input and print appropriate message
          System.err.println(e.getMessage());
          System.exit(1);
        }
      } else {
        // terminate program if invalid input
        System.err.println("Invalid input for column identifier.");
        System.exit(1);
      }

    }
    // catches error in filepath thrown by reader
    catch (FileNotFoundException e) {
      System.err.println("File not found error!");
      System.exit(1);
    }
    // catches error in reading file by parser
    catch (IOException e) {
      System.err.println("Error occurred in reading file!");
      System.exit(1);
    }
  }
}
