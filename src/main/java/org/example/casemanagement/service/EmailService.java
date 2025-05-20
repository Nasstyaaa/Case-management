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
            message.setSubject("Добро пожаловать в систему управления задачами");
            message.setText(String.format("""
                Уважаемый(ая) %s,
                            
                Добро пожаловать в систему управления задачами! Мы рады видеть вас в нашей команде.
                            
                Теперь вы можете начать создавать и управлять своими задачами. Если у вас возникнут вопросы,
                пожалуйста, не стесняйтесь обращаться в нашу службу поддержки.
                            
                С наилучшими пожеланиями,
                Команда управления задачами
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
            message.setSubject("Новое назначение задачи: " + task.getTitle());
            message.setText(String.format("""
                Уважаемый(ая) %s,
                            
                Вам назначена задача "%s" пользователем %s.
                
                Детали задачи:
                - Название: %s
                - Описание: %s
                - Текущий статус: %s
                - Доска: %s
                            
                Вы можете просматривать и управлять этой задачей в панели управления системой.
                            
                С наилучшими пожеланиями,
                Команда управления задачами
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
                message.setSubject("Задача выполнена: " + task.getTitle());
                message.setText(String.format("""
                    Поздравляем, %s!
                                
                    Задача "%s" успешно выполнена.
                    
                    Детали задачи:
                    - Название: %s
                    - Описание: %s
                    - Затраченное время: %d дней, %d часов, %d минут
                    - Доска: %s
                                
                    Отличная работа! Продолжайте в том же духе.
                                
                    С наилучшими пожеланиями,
                    Команда управления задачами
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