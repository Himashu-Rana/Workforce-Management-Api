package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.InMemoryTaskRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

   private final TaskRepository taskRepository;
   private final ITaskManagementMapper taskMapper;
   private final InMemoryTaskRepository inMemoryTaskRepository;

   public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper, 
                                  InMemoryTaskRepository inMemoryTaskRepository) {
       this.taskRepository = taskRepository;
       this.taskMapper = taskMapper;
       this.inMemoryTaskRepository = inMemoryTaskRepository;
   }

   @Override
   public TaskManagementDto findTaskById(Long id) {
       TaskManagement task = taskRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
       return taskMapper.modelToDto(task);
   }

   @Override
   public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
       List<TaskManagement> createdTasks = new ArrayList<>();
       for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
           TaskManagement newTask = new TaskManagement();
           newTask.setReferenceId(item.getReferenceId());
           newTask.setReferenceType(item.getReferenceType());
           newTask.setTask(item.getTask());
           newTask.setAssigneeId(item.getAssigneeId());
           newTask.setPriority(item.getPriority());
           newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
           newTask.setStatus(TaskStatus.ASSIGNED);
           newTask.setDescription("New task created.");
           
           TaskManagement savedTask = taskRepository.save(newTask);
           
           // Add activity log
           inMemoryTaskRepository.addActivityLog(
               savedTask.getId(), 
               "Task created and assigned to user " + item.getAssigneeId(),
               0L, // System user
               "System"
           );
           
           createdTasks.add(savedTask);
       }
       return taskMapper.modelListToDtoList(createdTasks);
   }

   @Override
   public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
       List<TaskManagement> updatedTasks = new ArrayList<>();
       for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
           TaskManagement task = taskRepository.findById(item.getTaskId())
                   .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

           TaskStatus previousStatus = task.getStatus();
           
           if (item.getTaskStatus() != null) {
               task.setStatus(item.getTaskStatus());
           }
           if (item.getDescription() != null) {
               task.setDescription(item.getDescription());
           }
           
           TaskManagement updatedTask = taskRepository.save(task);
           
           // Add activity log if status changed
           if (item.getTaskStatus() != null && previousStatus != item.getTaskStatus()) {
               inMemoryTaskRepository.addActivityLog(
                   updatedTask.getId(), 
                   "Task status changed from " + previousStatus + " to " + item.getTaskStatus(),
                   0L, // System user
                   "System"
               );
           }
           
           updatedTasks.add(updatedTask);
       }
       return taskMapper.modelListToDtoList(updatedTasks);
   }

   @Override
   public String assignByReference(AssignByReferenceRequest request) {
       List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
       List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(request.getReferenceId(), request.getReferenceType());

       // Fix for Bug #1: Cancel old tasks when reassigning
       for (Task taskType : applicableTasks) {
           List<TaskManagement> tasksOfType = existingTasks.stream()
                   .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                   .collect(Collectors.toList());

           if (!tasksOfType.isEmpty()) {
               // Get the assignee from the first task (they should all have the same assignee)
               Long currentAssigneeId = tasksOfType.get(0).getAssigneeId();
               
               // If the task is being reassigned to a different person
               if (!currentAssigneeId.equals(request.getAssigneeId())) {
                   // Cancel all existing tasks for this reference+type
                   for (TaskManagement oldTask : tasksOfType) {
                       oldTask.setStatus(TaskStatus.CANCELLED);
                       taskRepository.save(oldTask);
                       
                       // Log the cancellation
                       inMemoryTaskRepository.addActivityLog(
                           oldTask.getId(),
                           "Task cancelled due to reassignment from user " + oldTask.getAssigneeId() + " to user " + request.getAssigneeId(),
                           0L, // System user
                           "System"
                       );
                   }
                   
                   // Create a new task for the new assignee
                   TaskManagement newTask = new TaskManagement();
                   newTask.setReferenceId(request.getReferenceId());
                   newTask.setReferenceType(request.getReferenceType());
                   newTask.setTask(taskType);
                   newTask.setAssigneeId(request.getAssigneeId());
                   newTask.setStatus(TaskStatus.ASSIGNED);
                   newTask.setPriority(tasksOfType.get(0).getPriority()); // Keep the same priority
                   newTask.setTaskDeadlineTime(tasksOfType.get(0).getTaskDeadlineTime()); // Keep the same deadline
                   newTask.setDescription("Task reassigned from user " + currentAssigneeId);
                   
                   TaskManagement savedTask = taskRepository.save(newTask);
                   
                   // Log the new assignment
                   inMemoryTaskRepository.addActivityLog(
                       savedTask.getId(),
                       "Task created by reassignment to user " + request.getAssigneeId() + " from user " + currentAssigneeId,
                       0L, // System user
                       "System"
                   );
               }
           } else {
               // Create a new task if none exist
               TaskManagement newTask = new TaskManagement();
               newTask.setReferenceId(request.getReferenceId());
               newTask.setReferenceType(request.getReferenceType());
               newTask.setTask(taskType);
               newTask.setAssigneeId(request.getAssigneeId());
               newTask.setStatus(TaskStatus.ASSIGNED);
               newTask.setPriority(Priority.MEDIUM); // Default priority
               newTask.setTaskDeadlineTime(System.currentTimeMillis() + 86400000); // 1 day from now
               newTask.setDescription("New task assigned");
               
               TaskManagement savedTask = taskRepository.save(newTask);
               
               // Log the new assignment
               inMemoryTaskRepository.addActivityLog(
                   savedTask.getId(),
                   "New task assigned to user " + request.getAssigneeId(),
                   0L, // System user
                   "System"
               );
           }
       }
       return "Tasks assigned successfully for reference " + request.getReferenceId();
   }

   @Override
   public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
       List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

       // Fix for Bug #2: Filter out CANCELLED tasks
       // Enhanced for Feature #1: Smart Daily Task View
       List<TaskManagement> filteredTasks = tasks.stream()
               .filter(task -> task.getStatus() != TaskStatus.CANCELLED) // Bug #2 fix
               .filter(task -> {
                   Long taskTime = task.getTaskDeadlineTime() - 86400000; // Approximate start time (1 day before deadline)
                   
                   // Feature #1: Smart Daily Task View - Include tasks that:
                   // 1. Started within the range OR
                   // 2. Started before but are still active (not completed)
                   boolean startedWithinRange = taskTime >= request.getStartDate() && taskTime <= request.getEndDate();
                   boolean startedBeforeButActive = taskTime < request.getStartDate() && 
                                                   task.getStatus() != TaskStatus.COMPLETED && 
                                                   task.getStatus() != TaskStatus.CANCELLED;
                   
                   return startedWithinRange || startedBeforeButActive;
               })
               .collect(Collectors.toList());

       return taskMapper.modelListToDtoList(filteredTasks);
   }
   
   @Override
   public TaskManagementDto updateTaskPriority(UpdateTaskPriorityRequest request) {
       TaskManagement task = taskRepository.findById(request.getTaskId())
               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));
       
       Priority previousPriority = task.getPriority();
       task.setPriority(request.getPriority());
       
       TaskManagement updatedTask = taskRepository.save(task);
       
       // Add activity log for priority change
       inMemoryTaskRepository.addActivityLog(
           updatedTask.getId(),
           "Task priority changed from " + previousPriority + " to " + request.getPriority(),
           0L, // System user
           "System"
       );
       
       return taskMapper.modelToDto(updatedTask);
   }
   
   @Override
   public List<TaskManagementDto> findTasksByPriority(Priority priority) {
       List<TaskManagement> tasks = taskRepository.findByPriority(priority);
       
       // Filter out CANCELLED tasks
       List<TaskManagement> filteredTasks = tasks.stream()
               .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
               .collect(Collectors.toList());
               
       return taskMapper.modelListToDtoList(filteredTasks);
   }
   
   @Override
   public CommentDto addComment(AddCommentRequest request) {
       // Check if task exists
       taskRepository.findById(request.getTaskId())
               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));
       
       // Add the comment
       var comment = inMemoryTaskRepository.addComment(
           request.getTaskId(),
           request.getText(),
           request.getUserId(),
           request.getUsername()
       );
       
       // Log the activity
       inMemoryTaskRepository.addActivityLog(
           request.getTaskId(),
           "Comment added by user " + request.getUsername(),
           request.getUserId(),
           request.getUsername()
       );
       
       return taskMapper.commentToDto(comment);
   }
}
