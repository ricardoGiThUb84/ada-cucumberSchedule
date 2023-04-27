package org.example;

import com.google.gson.Gson;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.model.Task;
import org.example.model.TaskContract;
import org.example.model.TaskStatus;
import org.example.model.User;
import org.example.util.DataBaseUtil;
import org.junit.Assert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterTaskDefinition {

    private Task taskGlobal;
    private User user;
    private String json;
    private RequestSpecification request = RestAssured.given()
            .baseUri("http://localhost:8080/api")
            .contentType(ContentType.JSON);
    private Response response;

    private JsonPath jsonPath;

    private Gson gson = new Gson();

    @Given("A registered user")
    public void isAnUserRegister(DataTable dataTable) {

        User userTable = createUserFromDataTable(dataTable);

        DataBaseUtil.insertUser(userTable);

        user = DataBaseUtil.findUserByUsername(userTable.getUsername());
    }

    @When("Associate user inside payload")
    public void insertAnUserAtPayload(DataTable dataTable) {

        json = taskPayLoad(createTaskFromDataTable(dataTable, user));
    }

    @And("I record a new task")
    public void recordTask() {

        response = request.body(json).when().post("/tasks");
    }

    @Then("A status code is {int}.")
    public void statusCodeafterCreated(Integer statusCode) {

        Assert.assertEquals((int) statusCode, response.getStatusCode());

    }

    @And("apply contract validation")
    public void verifyingContract() throws FileNotFoundException {

        contractValidation(response);
    }

    @Given("Find a task by id {int}")
    public void findATaskByUser(Integer idTask) {

        jsonPath = request.when().get("/tasks/" + idTask).jsonPath();

    }

    @When("Update the task id {int} description to: {string}")
    public void updateDescriptionTask(Integer id, String description) {

        User usuario = jsonToUser(jsonPath.getMap("user").toString());


        Task taskUpdate = new Task();
        taskUpdate.setId(Long.valueOf(id));
        taskUpdate.setTitle(jsonPath.getString("title"));
        taskUpdate.setDescription(description);
        taskUpdate.setUser(usuario);
        taskUpdate.setClosedAt(null);
        taskUpdate.setStatus(Enum.valueOf(TaskStatus.class, jsonPath.getString("status")));

        System.out.println("update");
        System.out.println(taskUpdate);

        response = request.body(taskUpdate).when().put("/tasks/" + id);
    }

    @Then("Verify if status code is {int}")
    public void verifyIfUpdateWasSuccessfull(int satatus) throws FileNotFoundException {

        Assert.assertEquals(response.statusCode(), satatus);
        contractValidation(response);
    }

    @Given("A new user")
    public void createAnewUser(DataTable dataTable) {
        user = createUserFromDataTable(dataTable);
    }

    @And("save a new user at dataBase")
    public void savingTheNewUserInDataTable() {
        DataBaseUtil.insertUser(user);
    }

    @When("I replace a user from task with a user called {string}")
    public void replaceAnUserFromATask(String userName) throws SQLException {
        Long id = DataBaseUtil.findUserByUsername(userName).getId();
        List<Task> taskByUser = DataBaseUtil.findTaskByUser(id);

        taskByUser.stream().forEach(task -> {

            task.setUser(user);
            try {
                DataBaseUtil.insertTask(task);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @And("Verify if user name is {string}")
    public void verifyingIfUserWasChanged(String username) {

        boolean isUserNamePresent = request.when().get("/tasks").jsonPath()
                .getString("user").contains(username);

        Assert.assertTrue(isUserNamePresent);
    }


    @Given("The user already created {string}")
    public void checkIfIsTheSameUser(String userName) {
        user = DataBaseUtil.findUserByUsername(userName);
        Assert.assertEquals(user.getUsername(), userName);
    }

    @When("Associate the task to the user")
    public void createSecondTask(DataTable dataTable) {
        json = taskPayLoad(createTaskFromDataTable(dataTable, user));
        response = request.body(json).when().post("/tasks");
    }

    @Then("A statusCode sould be {int}.")
    public void statusCodeInvalidCreate(Integer statusCode) {

        response.prettyPrint();
        Assert.assertEquals((int) statusCode, response.getStatusCode());
    }


    @Given("A payload for a new task information")
    public void createAnewTaskWithoutAnUser(DataTable dataTable) {

        json = taskPayLoad(createTaskFromDataTable(dataTable, null));

    }

    @When("Register a task")
    public void registerATaskWithoutAUser() {

        response = request.body(json).when().post("/tasks");
    }

    @Then("The status is {int}")
    public void checkTheResponseForRecordATaskWithoutAUser(Integer status) {

        response.prettyPrint();
        Assert.assertEquals((int) status, response.getStatusCode());
    }


    @Given("record a task without a title")
    public void createTaskWithoutTitle(DataTable dataTable) {
        TaskContract tarefa = createTaskFromDataTable(dataTable, user);
        json = taskPayLoad(tarefa);
        System.out.println(json);

    }

    @When("Register the task")
    public void registerTaskWithoutTitle() {

        response = request.body(json).when().post("/tasks");
        response.prettyPrint();

    }

    @Then("A status code is {int}")
    public void checkResponseTaskWithoutTitle(Integer status) {

        response.prettyPrint();

        Assert.assertEquals((int) status, response.getStatusCode());
    }

    @Given("A task with id {int}")
    public void recoverATaskById(int id) throws SQLException {

        taskGlobal = DataBaseUtil.findTaskById((long) id);


    }

    @When("The user set the status to {string}")
    public void closingATask(String status) {

        taskGlobal.setStatus(Enum.valueOf(TaskStatus.class, status));
    }

    @Then("Update a modify task")
    public void updateTaskToClose() {

        request.body(taskGlobal).put("/tasks/" + taskGlobal.getId());

    }

    @And("Check if a date was created")
    public void checkAPatternDate() throws SQLException {

        Pattern dateRegex = Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$");

        long taskID = taskGlobal.getId();

        String closeAt = request.when()
                .get("/tasks/" + taskID).jsonPath().getString("closedAt");

        Matcher matcher = dateRegex.matcher(closeAt);

        Assert.assertTrue(matcher.matches());


    }

    @Given("A recover one task that had been closed")
    public void recoverClosedTask() throws SQLException {

        List<Integer> list = request.when().get("/tasks").jsonPath()
                .getList("findAll{it.status == 'CLOSE'}.id");

        taskGlobal = DataBaseUtil.findTaskById(Long.valueOf(list.get(0)));
    }

    @When("Change the title and date")
    public void changeTheTitleAndDate(DataTable dataTable) {

        taskGlobal.setTitle(dataTable.column(0).get(1));
        taskGlobal.setClosedAt(Date.valueOf(dataTable.column(1).get(1)));
    }

    @And("Change the user task")
    public void changeUserAtClosedTask(DataTable dataTable) {

        user = createUserFromDataTable(dataTable);
        taskGlobal.setUser(user);

    }

    @Then("Update the closed task")
    public void updateAclosedTask() {

        response = request.body(taskGlobal).put("/tasks/" + taskGlobal.getId());

    }


    @And("Recieve an status code {int}")
    public void verifyStatusFromClosedTasks(int status) throws FileNotFoundException {


        response.prettyPrint();
        Assert.assertEquals(status, response.getStatusCode());

    }

    private User createUserFromDataTable(DataTable dataTable) {

        User user = new User();

        dataTable.asMaps().forEach(it -> {
                    String name = it.get("name");
                    if (name == null) {
                        name = RandomStringUtils.randomAlphabetic(10);
                    }
                    String username = it.get("username");
                    if (username == null) {
                        username = RandomStringUtils.randomAlphabetic(10);
                    }
                    String password = it.get("password");

                    if (password != null) {
                        password = RandomStringUtils.randomAlphabetic(8);
                    }
                    user.setName(name);
                    user.setUsername(username);
                    user.setPassword(password);
                }
        );

        return user;
    }

    private TaskContract createTaskFromDataTable(DataTable data, User user) {

        TaskContract task = new TaskContract();
        task.setUser(user);
        data.asMaps().forEach(it -> {


            String title = it.get("title");

            TaskStatus status = Arrays.stream(TaskStatus.values())
                    .filter(s -> s.toString().equalsIgnoreCase(it.get("status")))
                    .findFirst().get();

            task.setStatus(status);


            task.setTitle(title);
            task.setUser(user);


        });
        return task;
    }


    private String taskPayLoad(TaskContract task) {
        Gson gson = new Gson();
        return gson.toJson(task);
    }

    private User jsonToUser(String user) {
        Gson gson = new Gson();
        return gson.fromJson(user, User.class);
    }

    private void contractValidation(Response response) throws FileNotFoundException {
        InputStream file = new FileInputStream("src/test/resources/taskSchema.json");
        response.then()
                .body(JsonSchemaValidator.matchesJsonSchema(file));
        response.prettyPrint();
    }

    private Task createTaskFromJson(String task, User user) {
        Gson gson = new Gson();
        Task taskUpdate = gson.fromJson(task, Task.class);

        taskUpdate.setUser(user);
        return taskUpdate;
    }

}
