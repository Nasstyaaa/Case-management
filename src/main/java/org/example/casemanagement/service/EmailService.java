package org.example.casemanagement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.casemanagement.model.entity.Task;
import org.example.casemanagement.model.entity.User;
import org.example.casemanagement.model.entity.Board;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "luk-2004@mail.ru";

    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Welcome to Case Management System");
            message.setText(String.format("""
                Dear %s,
                            
                Welcome to the Case Management System! We're excited to have you on board.
                            
                You can now start creating and managing your cases. If you have any questions,
                please don't hesitate to contact our support team.
                            
                Best regards,
                Case Management Team
                """, username));
            
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send welcome email to: {}. Error: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendTaskAssignmentEmail(User assignee, Task task, User assigner) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(assignee.getEmail());
            message.setSubject("New Task Assignment: " + task.getTitle());
            message.setText(String.format("""
                Dear %s,
                            
                You have been assigned to the task "%s" by %s.
                
                Task Details:
                - Title: %s
                - Description: %s
                - Current Status: %s
                - Board: %s
                            
                You can view and manage this task in your Case Management System dashboard.
                            
                Best regards,
                Case Management Team
                """, 
                assignee.getUsername(),
                task.getTitle(),
                assigner.getUsername(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getBoard().getName()
            ));
            
            mailSender.send(message);
            log.info("Task assignment email sent successfully to: {}", assignee.getEmail());
        } catch (MailException e) {
            log.error("Failed to send task assignment email to: {}. Error: {}", assignee.getEmail(), e.getMessage(), e);
        }
    }

    @Async
    public void sendTaskCompletionEmail(Task task) {
        Duration timeSpent = Duration.between(task.getCreatedAt(), LocalDateTime.now());
        long days = timeSpent.toDays();
        long hours = timeSpent.toHoursPart();
        long minutes = timeSpent.toMinutesPart();
        
        for (User assignee : task.getAssignees()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(FROM_EMAIL);
                message.setTo(assignee.getEmail());
                message.setSubject("Task Completed: " + task.getTitle());
                message.setText(String.format("""
                    Congratulations %s!
                                
                    The task "%s" has been completed successfully.
                    
                    Task Details:
                    - Title: %s
                    - Description: %s
                    - Time spent: %d days, %d hours, %d minutes
                    - Board: %s
                                
                    Great job on completing this task! Keep up the good work.
                                
                    Best regards,
                    Case Management Team
                    """,
                    assignee.getUsername(),
                    task.getTitle(),
                    task.getTitle(),
                    task.getDescription(),
                    days, hours, minutes,
                    task.getBoard().getName()
                ));
                
                mailSender.send(message);
                log.info("Task completion email sent successfully to: {}", assignee.getEmail());
            } catch (MailException e) {
                log.error("Failed to send task completion email to: {}. Error: {}", assignee.getEmail(), e.getMessage(), e);
            }
        }
    }

    @Async
    public void sendBoardMembershipEmail(User newMember, Board board, User owner) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(newMember.getEmail());
            message.setSubject("Added to Board: " + board.getName());
            message.setText(String.format("""
                Dear %s,
                            
                You have been added to the board "%s" by %s.
                
                Board Details:
                - Name: %s
                - Description: %s
                - Owner: %s
                            
                You can now view and manage tasks on this board in your Case Management System dashboard.
                            
                Best regards,
                Case Management Team
                """, 
                newMember.getUsername(),
                board.getName(),
                owner.getUsername(),
                board.getName(),
                board.getDescription(),
                owner.getUsername()
            ));
            
            mailSender.send(message);
            log.info("Board membership email sent successfully to: {}", newMember.getEmail());
        } catch (MailException e) {
            log.error("Failed to send board membership email to: {}. Error: {}", newMember.getEmail(), e.getMessage(), e);
        }
    }
} 