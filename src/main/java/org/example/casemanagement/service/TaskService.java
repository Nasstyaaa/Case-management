package org.example.casemanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.casemanagement.dto.TaskRequest;
import org.example.casemanagement.model.entity.Board;
import org.example.casemanagement.model.entity.Task;
import org.example.casemanagement.model.entity.TaskStatus;
import org.example.casemanagement.model.entity.User;
import org.example.casemanagement.repository.TaskRepository;
import org.example.casemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<Task> getBoardTasks(Long boardId, User user) {
        var board = boardService.getBoard(boardId, user);
        return taskRepository.findByBoardOrderByCreatedAtDesc(board);
    }

    @Transactional(readOnly = true)
    public List<Task> getBoardTasksByStatus(Long boardId, TaskStatus status, User user) {
        var board = boardService.getBoard(boardId, user);
        return taskRepository.findByBoardAndStatusOrderByCreatedAtDesc(board, status);
    }

    @Transactional
    public Task createTask(Long boardId, TaskRequest request, User creator) {
        var board = boardService.getBoard(boardId, creator);
        
        Set<User> assignees = new HashSet<>();
        if (request.getAssigneeUsernames() != null && !request.getAssigneeUsernames().isEmpty()) {
            for (String username : request.getAssigneeUsernames()) {
                User assignee = userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Assignee not found: " + username));
                
                // Check if assignee has access to the board
                boolean isOwner = board.getOwner().getId().equals(assignee.getId());
                boolean isMember = board.getMembers().contains(assignee);
                
                if (!isOwner && !isMember) {
                    throw new IllegalArgumentException("Assignee '" + username + "' must be either the board owner or a board member");
                }
                assignees.add(assignee);
            }
        }

        var task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .board(board)
                .creator(creator)
                .assignees(assignees)
                .build();

        task = taskRepository.save(task);

        // Send email notifications to assignees
        for (User assignee : assignees) {
            emailService.sendTaskAssignmentEmail(assignee, task, creator);
        }

        return task;
    }

    @Transactional
    public Task updateTask(Long taskId, TaskRequest request, User user) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Check if user has access to the board
        if (!boardService.hasAccess(task.getBoard().getId(), user)) {
            throw new AccessDeniedException("You don't have access to this task's board");
        }

        // Store old status and assignees for comparison
        TaskStatus oldStatus = task.getStatus();
        Set<User> oldAssignees = new HashSet<>(task.getAssignees());

        // Update only the fields that are provided
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            // Set completedAt when task is moved to DONE
            if (request.getStatus() == TaskStatus.DONE && oldStatus != TaskStatus.DONE) {
                task.setCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() != TaskStatus.DONE) {
                task.setCompletedAt(null); // Reset completedAt if task is moved out of DONE
            }
        }

        // Handle assignees update
        if (request.getAssigneeUsernames() != null) {
            Set<User> newAssignees = new HashSet<>();
            Board board = task.getBoard();
            
            for (String username : request.getAssigneeUsernames()) {
                if (username == null || username.trim().isEmpty()) continue;
                
                User assignee = userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Assignee not found: " + username));
                
                // Check if assignee has access to the board
                boolean isOwner = board.getOwner().getId().equals(assignee.getId());
                boolean isMember = board.getMembers().contains(assignee);
                
                if (!isOwner && !isMember) {
                    throw new IllegalArgumentException("Assignee '" + username + "' must be either the board owner or a board member");
                }
                newAssignees.add(assignee);
            }
            
            task.setAssignees(newAssignees);

            // Send email notifications to new assignees
            for (User assignee : newAssignees) {
                if (!oldAssignees.contains(assignee)) {
                    emailService.sendTaskAssignmentEmail(assignee, task, user);
                }
            }
        }

        task = taskRepository.save(task);

        // Check if task was moved to DONE status and send completion email
        if (oldStatus != TaskStatus.DONE && task.getStatus() == TaskStatus.DONE) {
            emailService.sendTaskCompletionEmail(task);
        }

        return task;
    }

    @Transactional
    public void deleteTask(Long taskId, Long id, User user) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        log.debug("Attempting to delete task: id={}, title={}, creator={}, board.owner={}, current_user={}",
                task.getId(), task.getTitle(), task.getCreator().getUsername(), 
                task.getBoard().getOwner().getUsername(), user.getUsername());

        // Check if user has access to the board
        if (!boardService.hasAccess(task.getBoard().getId(), user)) {
            log.error("Access denied: user {} does not have access to the board", user.getUsername());
            throw new AccessDeniedException("You don't have access to this task's board");
        }

        taskRepository.delete(task);
        log.debug("Task deleted successfully");
    }

    @Transactional(readOnly = true)
    public Task getTask(Long taskId, User user) {
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Check if user has access to the board
        if (!boardService.hasAccess(task.getBoard().getId(), user)) {
            throw new AccessDeniedException("You don't have access to this task's board");
        }

        return task;
    }

    @Transactional(readOnly = true)
    public Task getRandomTodoTask(Long boardId, User user) {
        var board = boardService.getBoard(boardId, user);
        var todoTasks = taskRepository.findByBoardAndStatusOrderByCreatedAtDesc(board, TaskStatus.TODO);
        
        if (todoTasks.isEmpty()) {
            throw new IllegalArgumentException("No tasks found in TODO status");
        }
        
        int randomIndex = (int) (Math.random() * todoTasks.size());
        return todoTasks.get(randomIndex);
    }
} 