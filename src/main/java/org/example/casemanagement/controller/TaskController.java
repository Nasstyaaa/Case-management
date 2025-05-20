package org.example.casemanagement.controller;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.casemanagement.dto.TaskRequest;
import org.example.casemanagement.model.entity.TaskStatus;
import org.example.casemanagement.model.entity.User;
import org.example.casemanagement.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/boards/{boardId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<?> getTasks(
            @PathVariable Long boardId,
            @RequestParam(required = false) TaskStatus status,
            @AuthenticationPrincipal User user
    ) {
        try {
            var tasks = status != null 
                    ? taskService.getBoardTasksByStatus(boardId, status, user)
                    : taskService.getBoardTasks(boardId, user);
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request getting tasks", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied getting tasks", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error getting tasks", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createTask(
            @PathVariable Long boardId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user
    ) {
        try {
            var task = taskService.createTask(boardId, request, user);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request creating task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied creating task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error creating task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long boardId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user
    ) {
        try {
            var task = taskService.updateTask(taskId, request, user);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request updating task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied updating task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error updating task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long boardId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal User user
    ) {
        try {
            taskService.deleteTask(taskId, user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Task deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request deleting task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (AccessDeniedException e) {
            log.error("Access denied deleting task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            log.error("Error deleting task", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 