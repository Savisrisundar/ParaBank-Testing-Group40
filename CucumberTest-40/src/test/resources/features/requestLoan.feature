@loan @regression
Feature: Request a loan
  Customers should receive a lending decision without unauthorised account changes.

  Background:
    Given the customer is logged into ParaBank
    And the customer has access to two bank accounts
    And the customer is on the Request Loan page

  @smoke @positive
  Scenario: Approve a modest loan with an adequate down payment
    When the customer requests a loan of "100.00" with down payment "20.00"
    Then the loan decision should be "Approved"

  @negative @boundary
  Scenario: Reject a missing loan amount with zero down payment
    When the customer requests a loan of "" with a down payment of "0"
    Then the loan application should not create an account or change existing balances

  @positive
  Scenario Outline: Approve realistic loan requests and create a new account
    When the customer requests a loan of "<amount>" with a down payment of "<down>"
    Then the loan application should be approved and a new account should exist
    Examples:
      | amount | down   |
      | 50.00  | 20.00  |
      | 100.00 | 25.00  |
      | 200.00 | 100.00 |

  @negative @boundary
  Scenario Outline: Reject invalid loan and down-payment values
    When the customer requests a loan of "<amount>" with a down payment of "<down>"
    Then the loan application should not create an account or change existing balances
    Examples:
      | amount | down   |
      | 100.00 |        |
      | 0      | 0      |
      | -1.00  | 1.00   |
      | abc    | 1.00   |
      | 100.00 | xyz    |
