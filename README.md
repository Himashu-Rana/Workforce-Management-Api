# Workforce Management API

A comprehensive Spring Boot REST API for managing workforce tasks, built as part of the Railse Backend Engineer Challenge.

## 🚀 Features

### Core Functionality
- **Task Management**: Create, update, assign, and track tasks
- **Smart Task Filtering**: Intelligent date-based task retrieval 
- **Priority Management**: HIGH/MEDIUM/LOW priority levels with dedicated endpoints
- **Comments & Activity History**: Full task tracking with user comments and automated activity logs

### Bug Fixes Implemented
- ✅ **Task Re-assignment**: Fixed duplicate task creation when reassigning work
- ✅ **Cancelled Task Filter**: Removed cancelled tasks from user views

### New Features Added
- ✅ **Smart Daily Task View**: Enhanced logic showing all actionable tasks
- ✅ **Task Priority System**: Complete priority management workflow
- ✅ **Comments & History**: Comprehensive task tracking and collaboration

## 🛠️ Technology Stack

- **Language**: Java 22
- **Framework**: Spring Boot 3.2.4
- **Build Tool**: Maven 3.9.9
- **Mapping**: MapStruct 1.5.5.Final
- **Code Generation**: Lombok
- **Storage**: In-memory collections (as per requirements)
- Task prioritization (HIGH, MEDIUM, LOW)
- Task comments and activity history tracking

## Features

1. **Bug Fixes**:
   - Fixed duplicate task creation when reassigning tasks by reference
   - Ensured cancelled tasks are filtered out of task views

2. **Smart Daily Task View**:
   - Enhanced date-based task fetching to include:
     - Tasks that started within the specified range
     - Tasks that started before but are still active

3. **Task Priority Management**:
   - Added priority field (HIGH, MEDIUM, LOW)
   - Implemented endpoints for changing priorities and filtering by priority

4. **Task Comments & Activity History**:
   - Automatic logging of key events for each task
   - User comments functionality
   - Chronological display of activity and comments

## API Endpoints

### Task Management
- `GET /task-mgmt/{id}` - Get task by ID
- `POST /task-mgmt/create` - Create tasks
- `POST /task-mgmt/update` - Update tasks
- `POST /task-mgmt/assign-by-ref` - Assign tasks by reference
- `POST /task-mgmt/fetch-by-date/v2` - Get tasks by date range

### Priority Management
- `POST /task-mgmt/update-priority` - Update task priority
- `GET /task-mgmt/priority/{priority}` - Get tasks by priority

### Comments Management
- `POST /task-mgmt/comment` - Add comment to a task

## How to Run

1. Ensure you have Java 17 installed
2. Run `./gradlew bootRun` to start the application
3. The API will be available at `http://localhost:8080`

## Example API Usage

### Creating a task
```bash
curl --location 'http://localhost:8080/task-mgmt/create' \
--header 'Content-Type: application/json' \
--data '{
   "requests": [
       {
           "reference_id": 105,
           "reference_type": "ORDER",
           "task": "CREATE_INVOICE",
           "assignee_id": 1,
           "priority": "HIGH",
           "task_deadline_time": 1728192000000
       }
   ]
}'
```

### Updating task priority
```bash
curl --location 'http://localhost:8080/task-mgmt/update-priority' \
--header 'Content-Type: application/json' \
--data '{
    "task_id": 1,
    "priority": "HIGH"
}'
```

### Adding a comment
```bash
curl --location 'http://localhost:8080/task-mgmt/comment' \
--header 'Content-Type: application/json' \
--data '{
    "task_id": 1,
    "text": "This invoice needs to be processed urgently",
    "user_id": 5,
    "username": "John Doe"
}'
```
