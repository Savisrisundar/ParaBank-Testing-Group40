@billpay @regression
Feature: Bill payment
  Customers should be able to pay valid bills and receive helpful validation for bad details.

  Background:
    Given the customer is logged into ParaBank
    And the customer has access to two bank accounts
    And the customer is on the Bill Pay page

  @smoke @positive
  Scenario: Pay a bill with complete payee details
    When the customer submits a bill payment of "1.00" with payee name "Electricity Company"
    Then the bill payment should be confirmed for "Electricity Company" and "1.00"
    And the source account balance should decrease by "1.00"

  @negative
  Scenario: Reject a bill payment without a payee name
    When the customer submits a bill payment of "1.00" with payee name ""
    Then the missing payee name should be reported
    And the source account balance should remain unchanged

  @positive @boundary
  Scenario Outline: Pay bills with different valid monetary amounts
    When the customer pays "<amount>" to "Electricity Company" with valid payee details
    Then the bill should be confirmed and the source balance debited by the exact amount
    Examples:
      | amount |
      | 0.01   |
      | 20.50  |
      | 99.99  |

  @negative @boundary
  Scenario Outline: Reject bill payments with invalid monetary amounts
    When the customer submits bill details with "amount" set to "<amount>"
    Then the bill payment should be rejected without debiting the customer
    Examples:
      | amount |
      | 0      |
      | -1.00  |
      | abc    |

  @negative
  Scenario Outline: Reject bill payments with missing details
    When the customer submits bill details with "<field>" set to ""
    Then the bill payment should be rejected without debiting the customer
    Examples:
      | field          |
      | street address |
      | city           |
      | state          |
      | postal code    |
      | payee account  |
      | verify account |
      | amount         |

  @negative
  Scenario Outline: Reject inconsistent or invalid payee account details
    When the customer submits bill details with "<field>" set to "<value>"
    Then the bill payment should be rejected without debiting the customer
    Examples:
      | field          | value    |
      | verify account | 987654   |
      | payee account  | abc      |
