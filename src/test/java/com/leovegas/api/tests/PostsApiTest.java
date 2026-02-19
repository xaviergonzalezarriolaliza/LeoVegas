package com.leovegas.api.tests;

import com.leovegas.api.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Sample API tests demonstrating RestAssured + JUnit 5 usage.
 *
 * These tests use the public JSONPlaceholder API as an example target.
 * Replace with the actual LeoVegas API endpoints and base URL as needed.
 */
@DisplayName("Posts API Tests")
public class PostsApiTest extends BaseApiTest {

    private static final String POSTS_ENDPOINT = "/posts";

    @Test
    @DisplayName("GET /posts returns HTTP 200 and a non-empty list")
    void getAllPosts_shouldReturn200WithNonEmptyList() {
        given()
            .spec(requestSpec)
        .when()
            .get(POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType("application/json")
            .body("$", not(empty()));
    }

    @Test
    @DisplayName("GET /posts/{id} returns the correct post")
    void getPostById_shouldReturnCorrectPost() {
        int postId = 1;

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(POSTS_ENDPOINT + "/" + postId)
        .then()
            .statusCode(200)
            .extract().response();

        assertThat(response.jsonPath().getInt("id")).isEqualTo(postId);
        assertThat(response.jsonPath().getString("title")).isNotBlank();
        assertThat(response.jsonPath().getString("body")).isNotBlank();
    }

    @Test
    @DisplayName("POST /posts creates a new post and returns HTTP 201")
    void createPost_shouldReturn201WithCreatedPost() {
        String requestBody = "{"
                + "\"title\": \"LeoVegas Test Post\","
                + "\"body\": \"This is a test post body.\","
                + "\"userId\": 1"
                + "}";

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .extract().response();

        assertThat(response.jsonPath().getString("title")).isEqualTo("LeoVegas Test Post");
        assertThat(response.jsonPath().getInt("userId")).isEqualTo(1);
        assertThat(response.jsonPath().getInt("id")).isPositive();
    }

    @Test
    @DisplayName("PUT /posts/{id} updates the post and returns HTTP 200")
    void updatePost_shouldReturn200WithUpdatedPost() {
        int postId = 1;
        String requestBody = "{"
                + "\"id\": 1,"
                + "\"title\": \"Updated Title\","
                + "\"body\": \"Updated body content.\","
                + "\"userId\": 1"
                + "}";

        given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .put(POSTS_ENDPOINT + "/" + postId)
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Title"));
    }

    @Test
    @DisplayName("DELETE /posts/{id} deletes the post and returns HTTP 200")
    void deletePost_shouldReturn200() {
        int postId = 1;

        given()
            .spec(requestSpec)
        .when()
            .delete(POSTS_ENDPOINT + "/" + postId)
        .then()
            .statusCode(200);
    }
}
