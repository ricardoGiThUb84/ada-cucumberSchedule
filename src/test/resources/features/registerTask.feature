Feature: Register a task
  Scenario: Register new tasks with status OPEN
    Given A registered user
      | name  | username | password |
      | apolo | apolo    | 123456   |
    When Associate user inside payload
      | title          | status |
      | derrotar titas | OPEN  |
    And I record a new task
    Then A status code is 201.
    And apply contract validation

  Scenario: Update a description after the task creation
    Given Find a task by id 1
    When Update the task id 1 description to: "batalha no tártaro"
    Then Verify if status code is 200

  Scenario: Changing a user from a task
    Given A new user
      | name  | username | password |
      | athena | athena   | 654321   |
    And save a new user at dataBase
    When I replace a user from task with a user called "apolo"
    Then Verify if user name is "athena"


  Scenario: Register new tasks with status CLOSE
      Given The user already created "apolo"
      When Associate the task to the user
        | title          | status |
        |   tocar lira   | CLOSE  |
      Then A statusCode sould be 500.

  Scenario: Register a task without a user
        Given A payload for a new task information
          | title          | status |
          |   sem usuario  | OPEN  |
        When Register a task
        Then The status is 500

  Scenario: Register a task without a title
      Given record a task without a title
          | status   | description |
          |   OPEN   | task without title |
      When Register the task
      Then A status code is 400

  Scenario: When a task is finished the status changes to close and a date is created
    Given A task with id 2
    When The user set the status to "CLOSE"
    Then Update a modify task
    And Check if a date was created


    Scenario: It's not possible to change a closed task
      Given A recover one task that had been closed
      When Change the title and date
          |    title    |    date    |
          | change task | 2012-12-21 |
      And Change the user task
          | name | username | password
          | inca | warrior  | 123456
      Then Update the closed task
      And Recieve an status code 400




