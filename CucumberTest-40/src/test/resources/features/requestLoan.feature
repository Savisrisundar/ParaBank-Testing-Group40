Feature: Request a loan
  A customer can receive a loan or encounter an unsuccessful request.

  Background:
    Given the customer is logged into ParaBank
    And the customer has access to two bank accounts
    And the customer is on the Request Loan page

  Scenario: Approve a modest loan with an adequate down payment
    When the customer requests a loan of "100.00" with down payment "20.00"
    Then the loan decision should be "Approved"

  # Observed behaviour: the server returns a generic internal error.
  # This is an unsuccessful request, NOT a business-rule denial.
  # Proper field-level validation is still a known application defect.
  Scenario: Loan request fails when the loan amount is missing
    When the customer requests a loan of "" with down payment "1.00"
    Then the loan request should fail without creating an account or changing balances
