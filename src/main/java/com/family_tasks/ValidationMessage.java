package com.family_tasks;

import static com.family_tasks.ValidationConstants.*;
import static com.family_tasks.ValidationConstants.TASK_DESCRIPTION_MIN_LENGTH;

public final class ValidationMessage {

    public static final String USER_NAME_NOT_SPECIFIED = "A user name isn't specified.";
    public static final String USER_NAME_TOO_LONG = "A user name length shouldn't be more than " + USER_NAME_MAX_LENGTH + ".";
    public static final String IS_ADMIN_NOT_SPECIFIED = "A user admin flag isn't specified.";
    public static final String USER_NOT_EXIST = "User with id %s doesn't exist";
    public static final String USER_NOT_SPECIFIED = "A user isn't specified.";
    public static final String TASK_NAME_NOT_SPECIFIED = "A task name isn't specified.";
    public static final String TASK_NAME_TOO_LONG = "A task name length shouldn't be more than " + TASK_NAME_MAX_LENGTH + ".";
    public static final String TASK_DESCRIPTION_TOO_LONG = "A task description length shouldn't be more than " + TASK_DESCRIPTION_MAX_LENGTH + ".";
    public static final String TASK_DESCRIPTION_TOO_SHORT = "A task description length shouldn't be less than " + TASK_DESCRIPTION_MIN_LENGTH + ".";
    public static final String TASK_CONFIDENTIAL_STATUS_NOT_SPECIFIED = "A task confidential status isn't specified.";
    public static final String TASK_DEADLINE_DATE_NOT_FUTURE = "Date must be in future";
    public static final String TASK_PRIORITY_INVALID = "Invalid priority value. Please enter a valid priority.";
    public static final String TASK_STATUS_INVALID = "Invalid status value. Please enter a valid status.";
    public static final String TASK_STATUS_NULL = "Status must be specified";
    public static final String TASK_PRIORITY_NULL = "Priority must be specified";
    public static final String TASK_REPORTER_NULL = "Reporter must be required";
    public static final String TASK_NOT_EXIST = "Task with id %s doesn't exist";
    public static final String TASK_FILTER_INVALID = "Invalid filter value. Please enter a valid filter.";
    public static final String TASK_FILTER_NOT_SPECIFIED = "A task's filter isn't specified.";
}
