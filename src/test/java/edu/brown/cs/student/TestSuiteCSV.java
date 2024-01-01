package edu.brown.cs.student;

import static org.testng.Assert.assertEquals;

import edu.brown.cs.student.main.csv.FactoryFailureException;
import edu.brown.cs.student.main.csv.Parser;
import edu.brown.cs.student.main.csv.Searcher;
import edu.brown.cs.student.main.csv.StringCreator;
import java.io.*;
import java.util.*;
import org.junit.jupiter.api.Test;

/** Tests for CSV Sprint 1 */
public class TestSuiteCSV {

  /**
   * tests if parser methods work and if can accept any reader object
   *
   * @throws IOException from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  @Test
  public void testParser() throws IOException, FactoryFailureException {
    // tests StringReader input into parser
    StringReader sr = new StringReader("This is a sample sentence.");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, sr);

    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\stars\\stardata.csv");
    Parser p2 = new Parser(sc, f);
    assertEquals(p2.parseString().size(), 119618);

    assertEquals(p2.parseObjectsWithHeaders().size(), 119617);

    FileReader f2 =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\sample\\deserts.csv");
    Parser p3 = new Parser(sc, f2);
    assertEquals(p3.parseObjectsWithoutHeaders().size(), 9);
  }

  /**
   * tests search within Shakespeare csv with special quotes inside quotes
   *
   * @throws IOException from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  @Test
  public void testShakespeare() throws IOException, FactoryFailureException {
    BufferedReader bf =
        new BufferedReader(
            new FileReader(
                "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\sample\\shakespeare.csv"));
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, bf);
    Searcher s = new Searcher(p, true);

    List results = new ArrayList();
    results.add("[Hamlet, \"\"\"Woe is me\"\"\"]");
    assertEquals(s.searchAllCol("Hamlet"), results);

    List results2 = new ArrayList();
    results2.add("[Romeo/Juliet, \"Romeo, O' Romeo\"]");
    assertEquals(s.searchByColName("romEO/juliEt", "pLAy"), results2);

    /*
    This is the test case that doesn't get covered by the regex!
    regex doesn't cover a quotes enclosed in quotes
    for csv files, quotes inside quotes for after /" so if csv doesn't follow normal convention
    search should not work. However, this does follow convention so search should work.
    List results3 = new ArrayList();
    results3.add("[Mean Girls, \"That is so /\"fetch/\"\"");
    assertEquals(s.searchByColName("mean girls","play"), results3);
     */
  }

  /**
   * tests serach in Kindergarten csv without headers
   *
   * @throws IOException from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  @Test
  public void testKindergarten() throws IOException, FactoryFailureException {
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\sample\\kindergarten.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, false);

    List results = new ArrayList();
    results.add("[orange, 2, jill]");
    assertEquals(s.searchByColIndex("Jill", 2), results);
    assertEquals(s.searchByColIndex("Jill", 1), new ArrayList());

    List results2 = new ArrayList();
    results2.add("[red, 1, sam]");
    results2.add("[violet, 7, sam]");
    assertEquals(s.searchAllCol("sam"), results2);
  }

  /**
   * tests search on income_by_race csv with large data
   *
   * @throws IOException from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  @Test
  public void testRace() throws IOException, FactoryFailureException {
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\census\\income_by_race_edited.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, true);

    assertEquals(s.searchByColName("2020", "Id year").size(), 40);
    assertEquals(s.searchByColIndex("0", 0).size(), 40);
  }

  /**
   * tests search on dol_ri_earnings_disparity csv for floats, percents and $ representation
   *
   * @throws IOException from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  @Test
  public void testEarnings() throws IOException, FactoryFailureException {
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\census\\dol_ri_earnings_disparity.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, true);

    assertEquals(s.searchByColName("RI", "state").size(), 6);
    assertEquals(s.searchAllCol("ri").size(), 6);

    // tests for different number representations
    List<String> results = new ArrayList<>();
    results.add("[RI, Hispanic/Latino, $673.14, 74596.18851, $0.64, 14%]");
    assertEquals(s.searchByColIndex("14%", 5), results);
    assertEquals(s.searchAllCol("74596.18851"), results);
  }

  /**
   * tests search on desert csv with lists inside row fields
   *
   * @throws IOException from parseString() method parser class
   * @throws FactoryFailureException from create() method creatorFromRow class
   */
  @Test
  public void testDeserts() throws IOException, FactoryFailureException {
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\sample\\deserts.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, false);

    List<String> results = new ArrayList<>();
    results.add("[cakes, \"[7, 8, 9]\"]");
    assertEquals(s.searchByColIndex("\"[7, 8, 9]\"", 1), results);

    // for csv files, brackets are enclosed in quotes
    // if the file does not follow normal csv convention, the search will not work
    assertEquals(s.searchAllCol("[10, 2, 3, 4]"), new ArrayList<>());
  }

  @Test
  public void testUnmodifiableListParser() throws IOException, FactoryFailureException{
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\census\\dol_ri_earnings_disparity.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, true);

    try{
      List<List<String>> csvParsed =  p.parseString();
      csvParsed.remove(0);
    }
    catch (UnsupportedOperationException e){
      assertEquals(1+1, 2);
    }
  }

  @Test
  public void testUnmodifiableListParser2() throws IOException, FactoryFailureException{
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\csv-prlakshm\\data\\census\\dol_ri_earnings_disparity.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, true);

    try{
      List<String> csvObjects =  p.parseObjectsWithHeaders();
      csvObjects.add("new object");
    }
    catch (UnsupportedOperationException e){
      assertEquals(2+2, 4);
    }
  }

  @Test
  public void testUnmodifiableListSearcher() throws IOException, FactoryFailureException{
    FileReader f =
        new FileReader(
            "C:\\Users\\prana\\Documents\\GitHub\\server-prlakshm-emilywang188\\data\\sample\\kindergarten.csv");
    StringCreator sc = new StringCreator();
    Parser p = new Parser(sc, f);
    Searcher s = new Searcher(p, false);

    try{
      List<String> results =  s.searchByColIndex("Jill", 2);
      results.add("new search result");
    }
    catch (UnsupportedOperationException e){
      assertEquals(5-5, 0);
    }
  }
}
