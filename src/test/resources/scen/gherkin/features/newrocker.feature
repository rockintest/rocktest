Feature: Add a new user

  Scenario: Add a non existing rocker

    Given a username newRocker
    And it does not exist

    When I add this rocker

    Then the API returns a success
    And the rocker is actually created

