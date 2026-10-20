@transfer @regression
Feature: Transfer funds between a customer's bank accounts
  Customers should be able to transfer valid amounts without losing or creating money.

  Background:
    Given the customer is logged into ParaBank
    And the customer has access to two bank accounts

  @smoke @positive
  Scenario: Successfully transfer funds between accounts
    Given the customer is on the Transfer Funds page
    When the customer enters the amount "1"
    And the customer selects an account with sufficient funds
    And the customer selects a different destination account
    And clicks the Transfer button
    Then the message "Transfer Complete!" should be displayed
    And the source account balance should decrease by "1"
    And the destination account balance should increase by "1"

  @negative
  Scenario: Transfer funds with an empty amount
    Given the customer is on the Transfer Funds page
    When the customer leaves the transfer amount empty
    And the customer selects an account with sufficient funds
    And the customer selects a different destination account
    And clicks the Transfer button
    Then the message "Error!" should be displayed
    And the transfer confirmation should not be displayed

  @positive @boundary
  Scenario Outline: Transfer valid monetary amounts
    When the customer transfers "<amount>" between two different accounts
    Then the transfer is confirmed and both account balances change by the exact amount
    Examples:
      | amount |
      | 0.01   |
      | 10.50  |
      | 25     |
      | 99.99  |

  @positive @boundary
  Scenario: Transfer the complete available balance
    When the customer transfers the entire available balance to another account
    Then the transfer is confirmed and both account balances change by the exact amount

  @negative @boundary
  Scenario Outline: Reject invalid transfer amounts without moving money
    When the customer transfers "<amount>" between two different accounts
    Then the transfer should be rejected without modifying any account balance
    Examples:
      | amount |
      | 0      |
      | -1.00  |
      | abc    |
