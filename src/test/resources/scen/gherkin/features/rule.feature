Feature: Gherkin scenario

  Rule: Variable management

    Scenario: Init a variable

      Given a variable VAR

      When its value is VALUE

      Then its value should be VALUE

    Example: Concatenate a variable and a string

      Given a variable VAR
      And its value is VALUE

      When I concatenate SUITE to VAR

      Then its value should be VALUESUITE

  Rule: Variable management duplicate

    Scenario: Init a variable

      Given a variable VAR

      When its value is VALUE

      Then its value should be VALUE

    Example: Concatenate a variable and a string

      Given a variable VAR
      And its value is VALUE

      When I concatenate SUITE to VAR

      Then its value should be VALUESUITE
