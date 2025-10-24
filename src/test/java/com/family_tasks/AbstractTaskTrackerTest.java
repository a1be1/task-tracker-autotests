package com.family_tasks;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractTaskTrackerTest {

    @BeforeAll
    static void setup() {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.get("TASK_TRACKER_BASE_URL");
    }
}