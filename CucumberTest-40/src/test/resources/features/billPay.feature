Feature: Bill payment
  A customer can pay a bill from a bank account and receive validation for incomplete details.

  Background:
    Given the customer is logged into ParaBank
    And the customer has access to two bank accounts
    And the customer is on the Bill Pay page

  Scenario: Pay a bill with complete payee details
    When the customer submits a bill payment of "1.00" with payee name "Electricity Company"
    Then the bill payment should be confirmed for "Electricity Company" and "1.00"
    And the source account balance should decrease by "1.00"

  Scenario: Reject a bill payment without a payee name
    When the customer submits a bill payment of "1.00" with payee name ""
    Then the missing payee name should be reported
    And the source account balance should remain unchanged
