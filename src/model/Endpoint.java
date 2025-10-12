package model;

public enum Endpoint {
    // TASK
    GET_ALL_TASKS,          // GET     /tasks
    GET_TASK,               // GET     /tasks/{id}
    CREATE_TASK,            // POST    /tasks
    UPDATE_TASK,            // POST    /tasks/{id}
    DELETE_TASK,            // DELETE  /tasks/{id}

    // SUBTASK
    GET_ALL_SUBTASKS,       // GET     /subtasks
    GET_SUBTASK,            // GET     /subtasks/{id}
    CREATE_SUBTASK,         // POST    /subtasks
    UPDATE_SUBTASK,         // POST    /subtasks/{id}
    DELETE_SUBTASK,         // DELETE  /subtasks/{id}

    // EPIC
    GET_ALL_EPICS,          // GET     /epics
    GET_EPIC,               // GET     /epics/{id}
    GET_EPIC_SUBTASKS,      // GET     /epics/{id}/subtasks
    CREATE_EPIC,            // POST    /epics
    DELETE_EPIC,            // DELETE  /epics/{id}

    // OTHER
    GET_HISTORY,            // GET     /history
    GET_PRIORITIZED,        // GET     /prioritized

    // SPECIAL
    UNKNOWN
}
