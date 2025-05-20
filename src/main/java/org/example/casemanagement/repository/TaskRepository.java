package org.example.casemanagement.repository;

import org.example.casemanagement.model.entity.Board;
import org.example.casemanagement.model.entity.Task;
import org.example.casemanagement.model.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByBoardOrderByCreatedAtDesc(Board board);
    List<Task> findByBoardAndStatusOrderByCreatedAtDesc(Board board, TaskStatus status);
} 