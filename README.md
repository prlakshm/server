# Server-prlakshm-emilywand188

Project name: Server

Team Members: Emily Wang and Pranavi Lakshminarayanan 

Link to Github Repo: https://github.com/prlakshm/server

How To Use:

To run the program, run the "src\main\java\edu\brown\cs\student\main\server\main\Server.java" file. Go to the localhost url printed in the terminal. Use the endpoints:

    /loadcsv?filepath=[filepath]&hadHeaders=["true"/"false"] -- to load csv file

    /viewcsv -- to view csv file

    /searchcsv?searchType=["index"/"name"]&columnIdentifier=[columnIndex/columnName]&searchVal=[searchValues] -- to search by a specific column index or column name in loaded csv

    /searchcsv?searchType=["all"]&searchVal=[searchValues] -- to search all columns in loaded csv

    /broadband?state=[state]&county=[county] -- to get broadband access percent for a specific county

Design Choices:

We chose to host our server using the Java Spark framework. We created handlers for each of our different endpoints in the "src\main\java\edu\brown\cs\student\main\server" directory. There is a handler for the "broadband: endpoint, the "loadcsv" endpoint, "viewcsv" endpoint, and "searchcsv" endpoint. We are essentially creating our own API server which can return broadband access data for a specific county and search a loaded csv file. 

We chose to represent handler success and failure responses as records so that the responses are immutable. These are more efficient than classes because we do not have to write getters and setters for every feild. 

The "src\main\java\edu\brown\cs\student\main\csv" directory has all classes that deal with parsing and searching a csv. These are the classes we use in the csv server handlers.

Errors/Bugs:
To our knowledge, there are no bugs in our code. However, we acknowledge that you can only search for one keyword using the "searchcsv" emdpoint. The search does not handle spaces. We also have not implemented cacheing to store query results. We plan to implement this in future sprints. 

Tests:
In our "src\test" directory, we have tests for the "loadcsv" handler, "viewcsv" handler, "searchcsv" handler, and "broadband" handler. These are all integration tests testing if a success response is returned in a tester spark port. To test "broadband" handler, we created a small test quite to test the actual American Community Survey (ACS) API. We also mocked the ACS API and tested the handler using the mock as the API connection. This mocked API returns the same broadband access percent response regardless of the query params. 
