Feature: Add a new user

  Rule: Rocker management

    Scenario: Add a new rocker

      Given a username newRocker
      And it does not exist

      When I add this rocker

      Then the API returns a success
      And the rocker is actually created

