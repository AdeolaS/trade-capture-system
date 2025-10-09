# Test Fixes

___

## fix(test): TradeControllerTest.testCreateTrade - Changed status expected from 200 to 201.

**Problem**: testCreateTrade() was getting a 201 status code instead of 200.

**Root Cause**: testCreateTrade() was expecting 200 (Ok), which is not the standard when creating a new resource. It should be testing for a status code of 201 (created) since a new resource is being created.

**Solution**: In TradeControllerTest, I replaced .andExpect(status().isOk()) with .andExpect(status().isCreated()).

**Impact**: The test method is now testing for the correct error code and is now passing.

___

## fix(test): TradeControllerTest.testDeleteTrade - Changed deleteTrade()’s returned status code from 200 to 204

**Problem**: testDeleteTrade() was expecting a status code of 204 but receiving 200.

**Root Cause**: The deleteTrade() method in TradeController.java was returning a 200 (Ok) code upon deletion, which is not the standard status code for the deletion of a resource. A 204 code is more appropriate.

**Solution**: I changed the returned code of deleteTrade() to 204 (No Content) in TradeController.java. I also added the message string, "Trade cancelled successfully", in the header of the response entity in order to make it clearer to the user that the trade has been deleted. Lastly, I changed the first swagger @ApiResponse annotation for the method to reflect the change in status code.

**Impact**: deleteTrade() is now returning 204 instead of 200 and the header of the response entity contains an informative message. The Swagger annotation for a successful deletion has also been updated.

___

## fix(test): TradeLegControllerTest.testCreateTradeLegValidationFailure_NegativeNotional - Expected error message is now showing up

**Problem**: testCreateTradeLegValidationFailure_NegativeNotional() had an empty response content instead of containing the string,‘Notional must be positive’.

**Root Cause**: The @Valid annotation on the TradeLegDTO parameter was triggered before the custom logic could be reached.

**Solution**: I removed @Positive(message = ‘Notional must be positive’) from TradeLegDTO.java.

**Impact**: Now, Spring’s validation framework doesn’t do a check for a positive notional, meaning that the program can now execute the custom check, and return the expected response message.

___

## fix(test): TradeServiceTest.testCreateTrade_InvalidDates_ShouldFail - Replaced wrong error message with the correct one

**Problem**: testCreateTrade_InvalidDates_ShouldFail() was getting the message “Start date cannot be before trade date” thrown, but it was expecting “Wrong error message”

**Root Cause**: testCreateTrade_InvalidDates_ShouldFail() was intentionally testing for the wrong error message.

**Solution**: I changed the expected error message to "Start date cannot be before trade date"

**Impact**: The test is now expecting the correct error message when the tradeStartDate is earlier than the tradeDate.

___

## fix(test): TradeControllerTest.testCreateTradeValidationFailure_MissingBook - Test is now getting the correct status code, and testing for the correct body string.

**Problem**: testCreateTradeValidationFailure_MissingBook() was expecting a 400 status code but was getting 201.

**Root Cause**: There were no checks in the tradeController or tradeService that would throw an exception if the book (or counterparty) was missing. And in the tradeControllerTest, there were no statements to stub the behaviour of those checks.

**Solution**: I added statements to throw a new RuntimeException with the message ‘Book and Counterparty are required’ in TradeService.populateReferenceDataByName() if the book or counterparty’s names and ids are null. I initially thought about adding a conditional to throw this exception in validateTradeCreation(), but ultimately decided to do this in populateReferenceDataByName() because this way, the program will execute less code before returning the exception. This means it is less expensive. Also, there was already an if-else-if conditional in validateTradeCreation() so it made sense to add an else onto the end. Lastly, the test has a statement to verify that saveTrade() is never called. Adding the check in validateTradeCreation(), which is called by saveTrade(), would mean that this verification would fail.
I also added a stubbing statement in the test method to return the RuntimeException when populateReferenceDataByName() is called in the test. I changed the test’s expected error message to ‘Error creating trade: Book and Counterparty are required’ in order to match the ResponseEntity body syntax returned by the TradeController.createTrade(). I could have alternatively removed the concatenation of “Error creating trade: ” in createTrade() but I felt that this extra information would be helpful for users of the application.

**Impact**: The test is now receiving the 400 status code, and is expecting the correct string content. The method tradeService.saveTrade is never called because the checks for book ID and Name are executed beforehand.

___

## fix(test): TradeControllerTest.testCreateTradeValidationFailure_MissingTradeDate - The test is now testing for the correct response content and receiving the correct status code.

**Problem**: testCreateTradeValidationFailure_MissingTradeDate() was expecting the response content \<Trade date is required\> but was receiving empty response content.

**Root Cause**: Firstly, there were no custom checks for whether the tradeDate was null or not. Secondly, in TradeDTO.java, there was a @NotNull annotation on the tradeDate that was executing before the custom checks could be reached.

**Solution**: I removed the @NotNull annotation from tradeDate in TradeDTO.java. I added a check in TradeMapper.toEntity() that returns a RuntimeException if the trade date is null. I chose to add it here because toEntity is the first method called in createTrade(). This means that the exception can be thrown early. 
I then added a stubbing statement in the test to mock the exception being thrown. I changed the test’s expected string to ‘Error creating trade: Trade date is required’ to match the concatenated syntax returned from TradeController.createTrade.java.

**Impact**: There is now a check to see if the TradeDTO has a tradeDate. If it doesn’t, an exception with the appropriate message is thrown and caught by the TradeController, which then returns a response entity with the correct status code and body containing the correct concatenated error string. The test now has a stubbing statement to mock the Exception being thrown. tradeService.saveTrade() is never invoked in the test.

___

## fix(test): TradeControllerTest.testUpdateTradeIdMismatch - Added an if statement to check for mismatching IDs, and to throw an exception if not matching. Changed expected string in test.

**Problem**: testUpdateTradeIdMismatch() is getting a status code of 200 instead of 400.

**Root Cause**: In TradeController.updateTrade(), the statement tradeDTO.setTradeId(id) immediately sets the given tradeDTO’s ID to the given ID. This means that when the method is given parameters with mismatching IDs, the DTO is immediately overwritten, so the mismatch no longer exists by the time amendTrade() is called.

**Solution**: I added an if statement in TradeController.updateTrade(), before the assignment, that returns a RuntimeException if there are mismatching IDs. I changed the test’s expected string to ‘Error updating trade: Trade ID in path must match Trade ID in request body’ to match the concatenated syntax returned from TradeController.updateTrade.java

**Impact**: The test is now passing. The tradeDTO ID is no longer getting overwritten before a check for mismatching IDs can be done. And the test is now checking for the correct string in the ResponseEntity body.

___


## fix(test): TradeControllerTest.testUpdateTrade - Updated test method to have stubbing statements for the correct controller method. 

**Problem**: TradeControllerTest.testUpdateTrade() was getting no value at JSON path ‘\$.tradeId’

**Root Cause**: The test was calling TradeController.updateTrade(), which makes a call to tradeService.amendTrade(). But in the test method, there were stubbing statements for tradeService.saveTrade() and populateReferenceDataByName(), neither of which are called in TradeController.updateTrade(). The test had stubbing statements and verification for the wrong methods.

**Solution**: In testUpdateTrade(), I added stubbing statements for the methods that are called in the method being tested: amendTrade() and toDto(). I added a verification statement to test if amendTrade() was called with the tradeId and any tradeDTO as parameters.

**Impact**: The value at the JSON path $.tradeId is now 1001, as expected. The test method now contains stubbing statements for the correct method.

___

## fix(test): BookServiceTest.testFindBookByNonExistentId - Added a Mock for the BookMapper, which stopped NullPointerExceptions occurring for this test and two others.

**Problem**: testFindBookByNonExistentId() was getting a NullPointerException.

**Root Cause**: There was no BookMapper mocked for the test. So when BookService.getBookById tried to call bookMapper::toDto, the Exception was being thrown. 

**Solution**: I added a mock for the BookMapper at class level. I gave the variable this scope because I could see that the other tests in BookServiceTest were also getting NullPointerExceptions.

**Impact**: The test is no longer throwing an exception and is now passing. After fixing this issue, BookServiceTest.testFindBookById(), which was also getting a NullPointerException, was no longer getting the error. (It was now failing for a different reason). BookServiceTest.testSaveBook() was also no longer getting the NullPointerException, but was now getting an error for a potential stubbing problem.

___

## fix(test): BookServiceTest.testSaveBook - Got rid of the strict stubbing argument mismatch.

**Problem**: There was a strict stubbing argument mismatch between bookRepository.save() in the test method and in BookService.java. This problem became evident after the NullPointerException was fixed previously (see previous fix(test) for reference).

**Root Cause**: Not all the required stubbing statements had been written in the test. bookMapper.toEntity(dto) had not been mocked, so was returning null instead of a book, which was causing the mismatch.

**Solution**: I added the missing stubbing statements to mock the behaviour of the saveBook() method.

**Impact**: The test method is now mocking the behaviour of BookService.saveBook() correctly and there is no longer a mismatch. 

___

## fix(test): BookServiceTest.testFindBookById - Added more mocks to the test method.

**Problem**: assertTrue(found.isPresent()) was expecting true but was getting false. This problem became apparent after the NullPointerException was fixed in a previous commit.

**Root Cause**: Not all the required mocks had been set up for the test. 

**Solution**: I added a stubbing statement to mock the bookMapper’s behaviour and initialised a BookDto with a matching Id to the mock Book object already in the test.

**Impact**: The test is now mocking the behaviour of getBookById() correctly and is now passing.

___

## fix(test): TradeServiceTest.testCreateTrade_Success - Added missing variables and mocks needed to avoid errors being thrown, and to accurately mock the behaviour of a new trade object being created.

**Problem**: Was getting the runtime exception ‘Book not found or not set’

**Root Cause**: The createTrade() method being called in the test was calling populateReferenceDataByName(), which was then throwing the exception because the tradeDTO object in the test setup did not have a counterparty or book with a name or ID assigned to it. The set up was also missing a TradeStatus and a TradeLeg, which are problems that became apparent after I fixed the initial exception.

**Solution**: I created mocks for the bookRepository and counterpartyRepository. I declared a Book, Counterparty, TradeStatus and TradeLeg object at class level. I declared them at this scope as I suspected that some of these variables would be needed by TradeServiceTest.testCashflowGeneration_MonthlySchedule(). I assigned values to the objects in setUp(). And I created stubbing statements in the test method to mimic the behaviour of the relevant methods in TradeService.java.

**Impact**: The test is now passing. Now, exceptions are being thrown and the mocks are mimicking the behaviour of the program when a trade is created.

___

## fix(test): TradeServiceTest.testAmendTrade_Success - Assigned a value to trade.version and added a stubbing statement to get rid of the NullPointerExceptions.

**Problem**: Test was throwing a NullPointerException, Cannot invoke ‘Java.lang.Integer.intValue()’ because the return value of ‘com.technicalchallenge.model.Trade.getVersion()’ is null

**Root Cause**: The Trade object in the setup didn’t have a version assigned to it so amendedTrade.setVersion(existingTrade.getVersion() + 1) was throwing a NullPointerException. Additionally, there was no stubbing statement for tradeLegRepository.save(tradeLeg), so leg was null and TradeLeg.getLegId() could not be invoked. This became apparent after I solved the first issue.

**Solution**: In the test method, I assigned a version integer to the trade object, and I added a stubbing statement for saving the tradeLeg.

**Impact**: The test is now passing. The NullPointer exceptions are no longer occurring.

___

## fix(test): TradeServiceTest.testCashflowGeneration_MonthlySchedule - Added a parameterised test which calls generateCashflows() indirectly via createTrade().

**Problem**: This test was a deliberate bug for candidates to find and fix. Candidates were supposed to implement proper cashflow testing

**Root Cause**: This test method was incomplete and had logical errors. The assertion was written in a way that would always fail.

**Solution**: I created a schedule with the string “1M” (monthly) and assigned it to the tradeLeg. I made the test parameterised in order to test the method’s effectiveness with multiple date scenarios. I added stubbing statements to mock the behaviour needed for the generateCashflows() method to execute. generateCashflows() is a private method, so has to be called indirectly through createTrade() or amendTrade(). In this case I used createTrade. And then for each test case, I verified that cashflowRepository.save() was invoked the correct number of times. 

**Impact**: The method generateCashflows() is now being tested correctly for multiple date scenarios.

___