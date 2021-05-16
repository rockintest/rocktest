Feature: Gherkin scenario

  Background:

    Given an empty variable

  Rule: Rule 1

    Background:

      Given a rule init step

    Scenario: scenario 1

      Given a given predicate

      When a when predicate

      Then a then predicate

    Example: scenario 2

      Given a given predicate

      When a when predicate

      Then a then predicate


  Rule: Rule 2

    Scenario: scenario 1

      Given a given predicate no init

      When a when predicate no init

      Then a then predicate no init

    Example: scenario 2

      Given a given predicate no init

      When a when predicate no init

      Then a then predicate no init
