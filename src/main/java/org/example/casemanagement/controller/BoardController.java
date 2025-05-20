package org.example.casemanagement.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.casemanagement.dto.BoardRequest;
import org.example.casemanagement.model.entity.User;
import org.example.casemanagement.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public String listBoards(@AuthenticationPrincipal User user, Model model) {
        log.debug("Listing boards for user: {}", user.getUsername());
        model.addAttribute("boards", boardService.getAccessibleBoards(user));
        return "boards/list";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createBoard(
            @Valid @RequestBody BoardRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Creating new board with name: {} for user: {}", request.getName(), user.getUsername());
        try {
            boardService.createBoard(request, user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Доска успешно создана");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating board", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public String viewBoard(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        log.debug("Viewing board {} for user: {}", id, user.getUsername());
        try {
            var board = boardService.getBoard(id, user);
            model.addAttribute("board", board);
            model.addAttribute("isOwner", board.getOwner().equals(user));
            return "boards/view";
        } catch (Exception e) {
            log.error("Error viewing board", e);
            return "redirect:/boards";
        }
    }

    @GetMapping("/{id}/edit")
    public String editBoardForm(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        log.debug("Editing board {} for user: {}", id, user.getUsername());
        try {
            var board = boardService.getBoard(id, user);
            if (!board.getOwner().equals(user)) {
                return "redirect:/boards/" + id;
            }
            model.addAttribute("board", BoardRequest.builder()
                    .name(board.getName())
                    .description(board.getDescription())
                    .public_(board.isPublic())
                    .build());
            return "boards/form";
        } catch (Exception e) {
            log.error("Error editing board", e);
            return "redirect:/boards";
        }
    }

    @PostMapping("/{id}")
    public String updateBoard(
            @PathVariable Long id,
            @Valid @ModelAttribute("board") BoardRequest request,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Updating board {} for user: {}", id, user.getUsername());
        try {
            boardService.updateBoard(id, request, user);
            redirectAttributes.addFlashAttribute("success", "Доска успешно обновлена");
            return "redirect:/boards/" + id;
        } catch (Exception e) {
            log.error("Error updating board", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/boards/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Deleting board {} for user: {}", id, user.getUsername());
        try {
            boardService.deleteBoard(id, user);
            redirectAttributes.addFlashAttribute("success", "Доска успешно удалена");
        } catch (Exception e) {
            log.error("Error deleting board", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/boards";
    }

    @PostMapping("/{id}/members")
    public String addMember(
            @PathVariable Long id,
            @RequestParam String username,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Adding member {} to board {} by user: {}", username, id, user.getUsername());
        try {
            var board = boardService.getBoard(id, user);
            
            if (!board.getOwner().equals(user)) {
                redirectAttributes.addFlashAttribute("error", "Только владелец доски может добавлять участников");
                return "redirect:/boards/" + id;
            }

            if (!board.isPublic()) {
                redirectAttributes.addFlashAttribute("error", "Невозможно добавить участников в приватную доску. Сначала сделайте доску публичной.");
                return "redirect:/boards/" + id;
            }
            
            if (username.equals(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Вы не можете добавить себя в качестве участника.");
                return "redirect:/boards/" + id;
            }

            boardService.addMember(id, username, user);
            redirectAttributes.addFlashAttribute("success", "Участник успешно добавлен");
        } catch (Exception e) {
            log.error("Error adding member", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/boards/" + id;
    }

    @PostMapping("/{id}/members/remove")
    public String removeMember(
            @PathVariable Long id,
            @RequestParam String username,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        log.debug("Removing member {} from board {} by user: {}", username, id, user.getUsername());
        try {
            boardService.removeMember(id, username, user);
            redirectAttributes.addFlashAttribute("success", "Участник успешно удален");
        } catch (Exception e) {
            log.error("Error removing member", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/boards/" + id;
    }

    @PostMapping("/{id}/visibility")
    @ResponseBody
    public ResponseEntity<?> toggleVisibility(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        log.debug("Making board {} public by user: {}", id, user.getUsername());
        try {
            var board = boardService.getBoard(id, user);
            
            if (!board.getOwner().equals(user)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Только владелец доски может изменить видимость"));
            }

            if (board.isPublic()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Доска уже является публичной и не может быть сделана приватной"));
            }

            board.setPublic(true);
            boardService.updateBoard(id, BoardRequest.builder()
                    .name(board.getName())
                    .description(board.getDescription())
                    .public_(true)
                    .build(), user);

            return ResponseEntity.ok(Map.of(
                "message", "Доска теперь публичная"
            ));
        } catch (Exception e) {
            log.error("Error making board public", e);
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }
} 