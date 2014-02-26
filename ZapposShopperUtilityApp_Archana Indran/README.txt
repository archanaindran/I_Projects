NAME: ZapposShoppersUtilityApp

DEVELOPED BY: Archana Indran + some help from Google.

LANGUAGE: Java (1.6x) using REST API.

HOW TO RUN: Double-click on the ZapposShoppersUtilityApp.jar file to run the program.

WHAT IS IT: A Java based utility application for shoppers at www.zappos.com, that provides various combinations of items that shoppers can conveniently purchase from the site. 

WHAT SHOULD THE USER INPUT: No, we are not asking for SSN, we need an input of total money the shopper wants to spend along with the number of items to be purchased.

HOW DOES IT WORK: The application takes a valid input from the user and calls the public Zappos Rest API and retrieves items and to display it back to the shopper. If items are not found for the shopper's input then a message is displayed.
If the shopper provides invalid input then application will request for a valid input.

EXTERNAL JARS: The application uses spring-web, json jars consuming the Zappos Rest API and Google Guava jar's "Sets" class for creating cartesian products for different combinations of items for displaying to the user.







