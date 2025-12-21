package com.family_tasks;

import static com.family_tasks.ValidationConstants.*;
import static com.family_tasks.ValidationConstants.TASK_DESCRIPTION_MIN_LENGTH;

public final class ValidationMessage {

    public static final String USER_NAME_NOT_SPECIFIED = "A user name isn't specified.";
    public static final String USER_NAME_TOO_LONG = "A user name length shouldn't be more than " + USER_NAME_MAX_LENGTH + ".";
    public static final String IS_ADMIN_NOT_SPECIFIED = "A user admin flag isn't specified.";
    public static final String USER_NOT_EXIST = "User with id %s doesn't exist.";
    public static final String USER_NOT_SPECIFIED = "A user isn't specified.";
    public static final String TASK_NAME_NOT_SPECIFIED = "A task name isn't specified.";
    public static final String TASK_NAME_TOO_LONG = "A task name length shouldn't be more than " + TASK_NAME_MAX_LENGTH + ".";
    public static final String TASK_DESCRIPTION_TOO_LONG = "A task description length shouldn't be more than " + TASK_DESCRIPTION_MAX_LENGTH + ".";
    public static final String TASK_DESCRIPTION_TOO_SHORT = "A task description length shouldn't be less than " + TASK_DESCRIPTION_MIN_LENGTH + ".";
    public static final String TASK_CONFIDENTIAL_STATUS_NOT_SPECIFIED = "A task confidential status isn't specified.";
    public static final String TASK_DEADLINE_DATE_NOT_FUTURE = "The deadline date must not be in the past.";
    public static final String TASK_PRIORITY_INVALID = "Invalid priority value. Please enter a valid priority.";
    public static final String TASK_STATUS_INVALID = "Invalid status value. Please enter a valid status.";
    public static final String TASK_STATUS_NULL = "Status must be specified.";
    public static final String TASK_PRIORITY_NULL = "Priority must be specified.";
    public static final String TASK_REPORTER_NULL = "Reporter must be required.";
    public static final String TASK_NOT_EXIST = "Task with id %s doesn't exist.";
    public static final String TASK_FILTER_INVALID = "Invalid filter value. Please enter a valid filter.";
    public static final String TASK_FILTER_NOT_SPECIFIED = "A task's filter isn't specified.";
    public static final String GROUP_OWNER_NOT_SPECIFIED = "An owner isn't specified.";
    public static final String USER_ALREADY_IS_OWNER = "User with id %d is already the owner of another group.";
    public static final String USER_ALREADY_HAS_GROUP = "This user is already a member of the group.";
    public static final String GROUP_NOT_EXIST = "Group with id %d doesn't exist.";
    public static final String TASK_DEADLINE_DATE_NOT_PRESENT_OR_FUTURE = "The deadline date must not be in the past";
    public static final String ID_HAS_INVALID_FORMAT = "The provided ID has an invalid format.";
    public static final String GROUP_NOT_SPECIFIED = "A group isn't specified.";
    public static final String INCORRECT_REQUEST_FORMAT = "Incorrect request format. Check data types.";
    public static final String CREATE_TASK_WITHOUT_GROUP = "To create a task you need to join a group or create a new one.";
    public static final String CREATE_OR_UPDATE_TASK_FOR_OWN_GROUP = "A task can be created or updated only for users from own group.";
    public static final String REWARDS_POINTS_POSITIVE = "Reward points cannot be negative.";
    public static final String VALIDATION_FAILED = "Validation failed.";
    public static final String REWARD_NOT_EXIST = "Reward with id %s doesn't exist.";
    public static final String REWARD_DESCRIPTION_TOO_LONG = "A reward description length shouldn't be more than " + REWARD_DESCRIPTION_MAX_LENGTH + ".";
    public static final String REWARD_DESCRIPTION_TOO_SHORT = "A task description length shouldn't be less than " + REWARD_DESCRIPTION_MIN_LENGTH + ".";
    public static final String USER_NOT_ADMIN = "Only the admin can edit rewards.";
    public static final String REWARD_AMOUNT_NULL = "Amount must be specified.";
    public static final String REWARD_DESCRIPTION_NULL = "Description must be specified.";

}
