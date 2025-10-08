package com.family_tasks;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractTaskTrackerTest {
    //TODO pull this value from the ".env" file
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8080";
    }
}