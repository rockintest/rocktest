Feature: Outline test

  Scenario Outline: check concat

    Given 2 variables <var1> and <var2>
    When I concat them
    Then I get <result>

    Examples:
      | var1 | var2 | result |
      |    A |    B |     AB |
      |   DE |   FG |   DEFG |
