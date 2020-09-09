package goRest;

import goRest.model.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GoRestUsersTests {

    private int userId;

    @Test()
    public void getUsers() {
        List<User> userList = RestAssured.given()
                .when()
                .get("https://gorest.co.in/public-api/users")
                .then()
                .log().body()
                //assertions
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.not(Matchers.empty()))
                //extracting users list
                .extract().jsonPath().getList("data", User.class);

        for (User user : userList) {
            System.out.println(user);
        }
    }

    @Test(enabled = false)
    public void getUsersExtactingMultipleTimes() {
        ExtractableResponse<Response> extract = RestAssured.given()
                .when()
                .get("https://gorest.co.in/public-api/users")
                .then()
                //assertions
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", Matchers.equalTo(200))
                .body("data", Matchers.not(Matchers.empty()))
                //extracting users list
                .extract();

        int code = extract.jsonPath().getInt("code");
        System.out.println("Code: " + code);

//        List<goRest.model.User> userList = extract.jsonPath().getList("data", goRest.model.User.class);
//        for (goRest.model.User user : userList) {
//            System.out.println(user);
//        }

//        goRest.model.User[] data = extract.jsonPath().getObject("data", goRest.model.User[].class);
//        for (int i = 0; i < data.length; i++) {
//            System.out.println(data[i]);
//        }

        // extracting certain user only
        User user = extract.jsonPath().getObject("data[4]", User.class);
        System.out.println(user);
    }

    @Test()
    public void createUser() {
        userId = RestAssured.given()
                // prerequisite data
                .header("Authorization", "Bearer 55b19d86844d95532f80c9a2103e1a3af0aea11b96817e6a1861b0d6532eef47")
                .contentType(ContentType.JSON)
                .body("{\"email\":\"" + getRandomEmail() + "\", \"name\": \"Techno\", \"gender\":\"Male\", \"status\": \"Active\"}")
                .when()
                //action
                .post("https://gorest.co.in/public-api/users")
                .then()
                //validations
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", Matchers.equalTo(201))
                .extract().jsonPath().getInt("data.id");
        ;
    }

    @Test(dependsOnMethods = "createUser")
    public void getUserById() {
        RestAssured.given()
                .pathParam("userId", userId)
                .when()
                .get("https://gorest.co.in/public-api/users/{userId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", Matchers.equalTo(200))
                .body("data.id", Matchers.equalTo(userId))
        ;
    }

    @Test(dependsOnMethods = "createUser")
    public void updateUserById(){
        String updateText = "Update goRest.model.User Test";
        RestAssured.given()
                .header("Authorization", "Bearer 55b19d86844d95532f80c9a2103e1a3af0aea11b96817e6a1861b0d6532eef47")
                .contentType(ContentType.JSON)
                .body("{\"name\": \""+updateText+"\"}")
                .pathParam("userId",userId)
                .when()
                .put("https://gorest.co.in/public-api/users/{userId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(200))
                .body("data.name", Matchers.equalTo(updateText));
    }

    @Test(dependsOnMethods = "createUser", priority = 1)
    public void deleteUserById(){
        RestAssured.given()
                .header("Authorization", "Bearer 55b19d86844d95532f80c9a2103e1a3af0aea11b96817e6a1861b0d6532eef47")
                .pathParam("userId",userId)
                .when()
                .delete("https://gorest.co.in/public-api/users/{userId}")
                .then()
                .statusCode(200)
                .body("code", Matchers.equalTo(204))
        ;
    }

    @Test(dependsOnMethods = "deleteUserById")
    public void getUserByIdNegative() {
        RestAssured.given()
                .pathParam("userId", userId)
                .when()
                .get("https://gorest.co.in/public-api/users/{userId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("code", Matchers.equalTo(404))
        ;
    }

    private String getRandomEmail() {
        return RandomStringUtils.randomAlphabetic(8) + "@gmail.com";
    }
}
