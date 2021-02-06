Coding Assessment for Boris Ashman
mobile 646-244-6653

GITHUB REPOSITORY : https://github.com/boriska-spb/TrueAccordAssessment_Scala

RUN IN SBT SHELL :
Command lines in sbt shell :
run Main.scala
run Main.scala src//main//scala//debt_config.json


TASKS
======
1. Consume the HTTP API endpoints described below to create a script that will output debts, one JSON object per line, to stdout.
Each line should contain
 - all of the debt's fields returned by the API
 - an additional boolean field, "is_in_payment_plan" set to true, if the debt is associated with a payment plan

2. Provide a test suite that validates the output being produced, along with any other operations performed internally

3 Add a new fields to the debts in the output:
 - remaining_amount, containing the calculated amount remaining to be paid on the debt. Output the value as a JSON number
 - next_payment_due_date", containing the ISO 8601 UTC date of when the next payment is due or
   null if there is no payment plan or if the debt has been paid off


DESIGN
=======

API requests
------------
All queries to API are encapsulated in APIAccess object.
API responses are
- fetched via io.Source.fromURL
- parsed by liftweb.lift-json parser
- extracted into data objects (case classes Debt, PaymentPlan and Payment)

Reporting logic
-----------------
Is implemented in Main.
All debt records are fetched, then for each debt record :
- find payment plan via parametrized request to API
- for remaining amount,fetch payments associated with the plan
- all required report fields are calculated for available data according to the business rules

Each debt record ultimately yields list of report fields (List[Any]) - a report row, associated with this debt record.
The complete report data is thus represented by list of such rows (List[List[Any]]), produced off the list of debt record
Finally report is printed, row by row, using format string, corresponding ot reprot type (Basic Info vs Extended Info)

Unit Test
---------
I wrote a simple mock utility, which supplied user-defined JSON responses per API requests (url + REST parameters).
mock object exposes mock "fromURL" function of type String => Option[String], tasking API request and returning response
if found, or none otherwise.
For unit test, mock object is attached to APIAccess object as a dynamic Scala type - DynamicVariable.
If mock object is present and mock query to API returns a user-defined JSON response, this response is used for
further processing. Otherwise, APIAccess uses query to actual API.


IMPLEMENTATION
===============

Config
----------
Implements configuration settings - API URLs and today's date
Config settings are implemented via dynamic variables, which can be overridden by the user of Config object.
E.g. today's date is overridden by unit test to provide compatibility with back-dated test data.
Main can also override config setting if path to user-specified config file is passed via command line.
Default config file (debt_config.json) is also provided in solution

APIAccess
----------
Encapsulates all internal API request processing, json parsing etc
End user interface is comprised of
 - user data classes - Debt, PaymentPlan and Payment, each reflecting corresponding table schema
 - user queries each table - fetchDebts, fetchPaymentPlans and fetchPayments, taking optional parameter (id)

HTTP request is handled by internal HTTPRequest method, which also implements the choice between mocked API and web API
It returns JSON response to the caller (fetch data methods), where a specific data object is extracted from JSON.

APIAccess also defines utility parse methods for installment frequency and dates, used by reporting logic in Main.

MockAPI
--------
Mock object, which stores user-defined JSON responses to API calls.
Exposes mock 'fromURL' method, which is used by APIAccess for unit test.

Main
--------
Implements reporting logic for both reports - with basic and extended info
Pretty prints output to the console
Takes a single program argument - path to config file.

DEPENDENCIES
==============
Solution uses following packages
net.liftweb.lift-json
org.scalatest

These packages must be installed on your system in order to run this solution

==================================================================================================================
==================================================================================================================

DEVELOPMENT PROCESS OVERVIEW

My initial choice for this assessment was Python - becasue it is often used to process HTTP requests.
however, I completed my work with Python 3 days earlier than deadline and decided to try and write Scala solution as well.
This solution takes a bit different approach than Python implementation, and in my view, is cleaner than my solution in Python.

Because I was busy the whole Tuesday, January 26 - I could get my hands on this solution only yesterday, Wednesday, January 27.
I completed program code in one day, and spent next day (today) to design unit test approach and implement unit tests.

Because of the time constraints (I have to submit both solutions by the end of the day), some parts, especially
related to unit test, may appear a bit crude. I had to design ad-hoc approach to HTTP request mocking because did
not have enough time to investigate and try different scala mock libraries.

If I had more time, I would probably provide better implementation HTTP request mocking and look at
lift-json parse options to provide conversion to custom types and parsing error processing.

You can also look at the history of my commits at https://github.com/boriska-spb/TrueAccordAssessment_Scala
