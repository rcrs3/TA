Feature: Add Student
  As a professor
  I want to register students in the system
  So I can evaluate the students with respect to various criteria

#Cenário Controler
  Scenario: Register a new student
    Given the student "Roberto Alves" with login "ra" is not registered in the system
    When I register "Roberto Alves" with login "ra"
    Then the student "Roberto Alves" with login "ra" is saved in the system

#Cenário GUI
  Scenario: Message from the new student registration
    Given I am in the add student page
    When I add the "Rodrigo Calegario" with login "rc"
    Then I can see the name of "Rodrigo Calegario" and the login "rc" in the list of students

#Cenário Controler
  Scenario: Register a student twice
    Given the student "Josefo Alalcario" with login "ja" is registered in the system
    When I register "Josefo Alalcario" with login "ja"
    Then the system does not register "Josefo Alalcario" with login "ja"

#Cenário GUI
  Scenario: Error message when registering a student twice
    Given  I am in the add student page
    When I add the "Mocamba Marim" with login "mm"
    Then I can't see the name of "Mocamba Marim" and the login "mm" in the list of students