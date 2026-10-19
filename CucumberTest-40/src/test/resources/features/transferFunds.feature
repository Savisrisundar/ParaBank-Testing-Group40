
Feature: Transfer Funds

  Background:
    Given the customer is logged into ParaBank
    And the customer has access to two bank accounts

  Scenario: Successfully transfer funds between accounts
    Given the customer is on the Transfer Funds page
    When the customer enters the amount "1"
    And the customer selects an account with sufficient funds
    And the customer selects a different destination account
    And clicks the Transfer button
    Then the message "Transfer Complete!" should be displayed
    And the source account balance should decrease by "1"
    And the destination account balance should increase by "1"

  Scenario: Transfer funds with an empty amount
    Given the customer is on the Transfer Funds page
    When the customer leaves the transfer amount empty
    And the customer selects an account with sufficient funds
    And the customer selects a different destination account
    And clicks the Transfer button
    Then the message "Error!" should be displayed
    And the transfer confirmation should not be displayed
