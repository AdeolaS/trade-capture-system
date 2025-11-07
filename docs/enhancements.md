# Implement Missing Functionality
Overview:
When implementing these enhancements, I made sure to:
- Continue on with the pre-existing coding patterns in the code. 
- I added Swagger annotations to every new endpoint I created in accordance with the established template. 
- I added logger messages at key moments in the program.
- For each enhancement, I wrote tests for successes and edge cases of methods.

## Enhancement 1: Advanced Trade Search System
I’ve added some more data into the data.sql file. This was for the purpose of my own manual testing. (I like to test in Postman)

### (“/rsql”)
With this endpoint, users can search for trades using RSQL.
- I got the base code from https://www.baeldung.com/rest-api-search-language-rsql-fiql
- This code was the basis for all the classes in the RSQL folder, with mostly minor tweaks made to make it work with the pre-existing code.
- I saw that there was already a dependency for cz.jirutka.rsql in the pom.xml so I felt this was the right way to go.
- The only class I had to make major changes to was GenericRsqlSpecification.
- I couldn’t rely on root.get(property) when specifying rooted properties e.g counterparty.name. So I created a method called getPath. If it's a simple property such as tradeID, then root.get(property) will be returned, just like in the source code. 
- Otherwise, the incoming String will be split at the full stops and the parts will have left joins performed on them. Then the resulting entity will be for parsing into a Predicate.
- Improvements for the future: At the moment, the error messages cna be very verbose and hard to understand. This could be improved by overriding the error messages from the library with custom ones.

### (“/search”)
With this endpoint, users can search for trades by trade status, book, counterparty, user and date.
- Wrote a custom query in TradeRepository.java which allows for all of the search parameters to be used at once. None of the search parameters are required, so the user can be as vague or detailed with their search as needed. This allows flexibility.
- I Validate the search parameters in TradeService.validateSearchParameters(). I check for things such as the earliest date being before the latest date, and certain parameters existing in the database.
- Any error messages from the validation method are collected in a String and thrown as a RuntimeException to the TradeController. I chose to implement it this way for ease of use on the user’s side. Collecting all the errors at the end of the checks and giving them all to the user at once means that there’ll be less back-and-forth fixing of errors if the user has input many invalid values.
- The list of trades (or error message) is returned as a response entity from the controller method.

### (“/filter”)
With this endpoint, users can search for all trades. The trades will be paginated according to the input parameters, with default values provided.
- The controller method paginateTrades() accepts page number and size as params
- Validation of these params occurs in TradeService.validatePaginationParams(). The validation method sends appropriate errors if requested page number or page size is invalid


## Enhancement 2: Comprehensive Trade Validation Engine

GENERAL NOTES:
- In order to implement the enhancements in this section, I wrote numerous new static methods in Validation.java, including validateTradeBusinessRules(), validateTradeLegConsistency() and validateReferenceData().
- I made them static so that the methods could be called without the need for instantiated objects of the class.
- These 3 validation methods return ValidationResult objects and are called by amendTrade() and createTrade() just before tradeRepository.save() is called.
- I choose to call the validation methods at this point because I wanted to call all the validation methods after the Trade object has been populated by the reference data. (For the sake of validateReferenceData())
- I could have alternatively called the other two validate methods at the beginning. However, I wanted all of the possible Validation error messages to be returned together.

### Created a Validation Result Object
- I made a validationResult Object 
- It has a Boolean to show if validations have passed and a list of ValidationErrors (starts off as an empty list.)
- ValidationError object contains three strings: field name that’s being validated, error message and severity: Error or Warning. A warning will not fail the validation.

### TradeService.getValidationResultErrorMessages() 
- method gets all the ValidationErrors in a validationResult and prints them.
- I created this method to keep repeated code to a minimum and so that the string could then be passed to the user.

### Date Validation Rules: public ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO)
This method ensures that the trade’s maturity date is not before start date or trade date, its start date is not before trade date and the trade date is not more than 30 days in the past
- Checks that none of the business rules have been violated.
- It is called by amendTrade() and createTrade().
- I deleted the pre-existing validateTradeCreation() method. It is now redundant, since the checks being done by it are now being done by this method and validateTradeLegConsistency().
- Pre-Existing Tests: After implementing this enhancement, I had to make some changes to the tests in TradeServiceTest.java
- The value for tradeDateDTO in the setup was 30 days before today’s date. So a lot of tests related to that were now failing. To solve this, I changed the variable to equal LocalDate.now() so that whenever the test is run, this variable will be valid. And then I applied this fix to maturityDate and tradeStartDate, with extra offsets to create a range in dates.
- After the enhancements, testCreateTrade_InvalidDates_ShouldFail() was now expecting the wrong error message. I updated the test method to expect the correct one.
In testCashflowGeneration_MonthlySchedule() there were also absolute date values that were invalid, so I replaced those dates with dates relative to LocalDate.now(), meaning that the test would be valid no matter when it’s run.
- New Tests: I wrote numerous tests for this validation method, including but not limited to:
A parameterised test with a range of input values to test valid dates.
Tests for when each date, or all dates are null
Test for when the dates are invalid
Used parameterised tests for the dates to test different scenarios
















### Cross-Leg Business Rules: public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs)
This method ensures that both legs have identical maturity dates, Legs have opposite pay/receive flags, floating legs have an index specified and that fixed legs have a valid rate. This method also ensures that any leg-related reference data exists and is valid. 
- Checks the required leg consistency rules.
- Checks if the reference data belonging to the legDTO is non-null and non-empty.
- It is called by amendTrade() and createTrade().
- I have not done a check for both legs having the same maturity date, as that property is set at Trade Object level, so the legs will always have the same maturity date.
- Pre-Existing Tests: A lot of tests were now failing so had to make some changes in TradeServiceTest.java:
    - In setUp() I set the legTypes - one floating, one fixed
    - I set the flags - one pay, one receive
    - I gave the floating leg and index id and raised the rate of the other leg to be above zero.
    - I also had to create mocks and stubbing statements for LegType, Index and PayRec and their repositories. I then had to add reference data to the mocks as well.

New Tests: I wrote many tests for this method, testing the success case, as well as many edge cases.
- Success when all data is valid.
- Testing the error message when the TradeLeg list is null
- Tests for the PayRec flag rules
- Tests for when each field is null
- Tests for when the reference data are empty strings
- Tests for when the notional and rate are invalid

### Entity Status Validation
- I created TradeService.validateReferenceData() to validate the reference data that isn’t held by the TradeLegs.
- I used a series of nested if loops to validate the existence of some of the data (to check if they’re not null).
- I did it this way due to the structure of the classes. 
- E.g Each Book has a CostCenter, so if the Book is null, the if statement to check for the presence of a CostCenter is not executed. And each CostCenter has a Subdesk. So if the CostCenter is null, the check for a Subdesk is never reached, and so on.
- This new validate method replaces the old one of the same name. 

Tests:
- Success when all data is present and active
- Tests for cases where each piece of reference data is not present or inactive.

## Enhancement 3: Trader Dashboard and Blotter System
GENERAL NOTES:
- I created a new controller class, TradeDashboardController. RequestMapping(“/api/dashboard”)
And a new TradeDashboardService

### @GetMapping("/my-trades") 
- Created a new query in TradeRepository that finds trades by traderUserId.
- Added corresponding methods in TradeDashboardService and TradeDashboardController.
- Accepts the userId as a string and checks that the corresponding user is found in the database, and is active, before retrieving the list of trades

### @GetMapping("/book/{id}/trades") 
- Created a new query in TradeRepository that finds trades by traderUserId and bookId.
- Added corresponding methods in TradeDashboardService and TradeDashboardController.
- Accepts the parameters as strings and checks if the book and user are both present and active in the database before retrieving the list of trades.

### @GetMapping("/summary")    
- Created a TradeSummary Entity class, as well as corresponding DTO, mapper and repo classes.
buildTradeSummary() method builds the trade summary, populating the object with: 
    - Time and date stamps, 
    - Total number of trades by status
    - Total notional amounts by currency
    - Breakdown by trade type and counterparty
    - Risk exposure summaries (To calculate this, I grouped the trades by book name. For each trade in the group, I summed all its legs’ notionals (if any) and then summed those totals across all trades in the same book)
- The method saves the built tradeSummary into the tradeSummaryRepository
- The buildTradeSummary() is called by getTradeSummaryForUser(). It maps the returned object to a TradeSummaryDTO.
- I also added a getHistoricalTradeSummaries() so that the user can look up historical trade summaries.
- I wrote plenty of tests to test successes and edge cases.
  
### @GetMapping("/daily-summary")
- Created a DailySummary Entity class, as well as corresponding DTO, mapper and repo classes.
buildDailySummary() method builds the daily summary, populating the object with:
    - Application User
    - Date stamp
    - Today’s Trade Count
    - Today’s total notional
    - Trade count and notional count by book.
    - Previous day’s notional and trade counts
    - And the percentage changes from the previous days
    - Average of the last 30 day’s notional and trade counts
    - And the percentage changes from those previous days
- The method saves the created daily summary in the repo before returning the trade summary
buildDailySummary() is called by getDailySummaryForUser(), which then maps the summary into a DTO.
