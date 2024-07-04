package com.exam;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.Test;

import java.io.File;

public class JiraApiTest {

    private static final String BASE_URI = "https://nagasaikumar.atlassian.net";
    private static final String ISSUE = "/rest/api/3/issue";
    private static final String ISSUE_WITH_KEY = "/rest/api/3/issue/{issueIdOrKey}";
    private static final String ISSUE_KEY_WITH_ATTACHMENTS = "/rest/api/3/issue/{issueIdOrKey}/attachments";
    private static final String CREATE_ISSUE_BODY = "{\"fields\":{\"summary\":\"Test Automation\",\"description\":{\"type\":\"doc\",\"version\":1,\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"API Automation Issue\"}]}]},\"project\":{\"key\":\"TA\",\"name\":\"Jira API Automation\",\"id\":\"10000\"},\"issuetype\":{\"id\":\"10001\"}}}";
    private static final String UPDATE_ISSUE_BODY = "{\"fields\":{\"summary\":\"Updating the existing issue\"}}";

    private static final String JIRA_USERNAME = "naga_saikumar@epam.com";
    private static final String JIRA_PASSWORD = "ATATT3xFfGF0dB_UFe45cprpOL18QiSHBNL jm8MdbDo6aYH6g2rTFObRh8pgWdYQvg-N1841j29kuAbHWz7mN40gEzr1CuUSGMSznGqJNRN8u";

    private static final ResponseSpecification putAndDeleteSpecification = new ResponseSpecBuilder()
            .expectStatusCode(204)
            .build();

    @Test
    public void testJiraApi() {
        String issueKey = doPostRequestToCreateJiraIssue();
        System.out.println("Jira issue is created, key: " + issueKey);
        doGetRequestToValidateTheIssue(issueKey);
        System.out.println("GET - Issue existed in the JIRA portal");
        doPutRequestToUpdateTheIssue(issueKey);
        System.out.println("PUT - Updated the Issue summary");
        doPostRequestToAddAttachmentsToIssue(issueKey);
        System.out.println("POST - Added the attachment to the issue");
        doDeleteRequestToDeleteTheJiraIssue(issueKey);
        System.out.println("Delete - Created Jira issue is deleted");
    }

    private String doPostRequestToCreateJiraIssue() {
        RestAssured.baseURI = BASE_URI;
        RequestSpecification rspec = RestAssured.given()
                .auth()
                .preemptive()
                .basic(JIRA_USERNAME, JIRA_PASSWORD)
                .contentType(ContentType.JSON)
                .body(CREATE_ISSUE_BODY);
        Response response = rspec.when().post(ISSUE);
        response.then().assertThat().statusCode(201);
        return response.getBody().jsonPath().getString("key");
    }

    private void doGetRequestToValidateTheIssue(String issueKey) {
        RestAssured.baseURI = BASE_URI;
        RequestSpecification rspec = RestAssured.given()
                .auth()
                .preemptive()
                .basic(JIRA_USERNAME, JIRA_PASSWORD)
                .pathParam("issueIdOrKey", issueKey);
        Response response = rspec.when().get(ISSUE_WITH_KEY);
        response.then().assertThat().statusCode(200);
    }

    private void doPutRequestToUpdateTheIssue(String issueKey) {
        RestAssured.baseURI = BASE_URI;
        RequestSpecification rspec = RestAssured.given()
                .auth()
                .preemptive()
                .basic(JIRA_USERNAME, JIRA_PASSWORD)
                .pathParam("issueIdOrKey", issueKey)
                .contentType(ContentType.JSON)
                .body(UPDATE_ISSUE_BODY);
        Response response = rspec.when().put(ISSUE_WITH_KEY);
        response.then().assertThat().spec(putAndDeleteSpecification);
    }

    private void doPostRequestToAddAttachmentsToIssue(String issueKey) {
        RestAssured.baseURI = BASE_URI;
        RequestSpecification rspec = RestAssured.given()
                .auth()
                .preemptive()
                .basic(JIRA_USERNAME, JIRA_PASSWORD)
                .pathParam("issueIdOrKey", issueKey)
                .header("X-Atlassian-Token", "no-check")
                .contentType(ContentType.MULTIPART)
                .multiPart("file", new File("issue.txt"));
        Response response = rspec.when().post(ISSUE_KEY_WITH_ATTACHMENTS);
        response.then().assertThat().statusCode(200);
    }

    private void doDeleteRequestToDeleteTheJiraIssue(String issueKey) {
        RestAssured.baseURI = BASE_URI;
        RequestSpecification rspec = RestAssured.given()
                .auth()
                .preemptive()
                .basic(JIRA_USERNAME, JIRA_PASSWORD)
                .pathParam("issueIdOrKey", issueKey);
        Response response = rspec.when().delete(ISSUE_WITH_KEY);
        response.then().assertThat().spec(putAndDeleteSpecification);
    }
}