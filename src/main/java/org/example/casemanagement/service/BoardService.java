package org.example.casemanagement.service;

import lombok.RequiredArgsConstructor;
import org.example.casemanagement.dto.BoardRequest;
import org.example.casemanagement.model.entity.Board;
import org.example.casemanagement.model.entity.User;
import org.example.casemanagement.repository.BoardRepository;
import org.example.casemanagement.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(BoardService.class);

    @Transactional(readOnly = true)
    public List<Board> getAccessibleBoards(User user) {
        return boardRepository.findAccessibleBoards(user);
    }

    @Transactional
    public Board createBoard(BoardRequest request, User owner) {
        var board = Board.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .owner(owner)
                .build();
        return boardRepository.save(board);
    }

    @Transactional
    public Board updateBoard(Long boardId, BoardRequest request, User user) {
        var board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        if (!board.getOwner().equals(user)) {
            throw new AccessDeniedException("Only the board owner can update it");
        }

        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setPublic(request.isPublic());

        return boardRepository.save(board);
    }

    @Transactional
    public void deleteBoard(Long boardId, User user) {
        var board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        if (!board.getOwner().equals(user)) {
            throw new AccessDeniedException("Only the board owner can delete it");
        }

        boardRepository.delete(board);
    }

    @Transactional
    public Board addMember(Long boardId, String username, User user) {
        var board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        if (!board.getOwner().equals(user)) {
            throw new AccessDeniedException("Only the board owner can add members");
        }

        var memberToAdd = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        board.getMembers().add(memberToAdd);
        board = boardRepository.save(board);
        
        // Send email notification to the new member
        emailService.sendBoardMembershipEmail(memberToAdd, board, user);

        return board;
    }

    @Transactional
    public Board removeMember(Long boardId, String username, User user) {
        var board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        if (!board.getOwner().equals(user)) {
            throw new AccessDeniedException("Only the board owner can remove members");
        }

        var memberToRemove = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        board.getMembers().remove(memberToRemove);
        return boardRepository.save(board);
    }

    @Transactional(readOnly = true)
    public Board getBoard(Long boardId, User user) {
        log.debug("Getting board {} for user: {}", boardId, user.getUsername());
        var board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));
        log.debug("Found board: {}, owner: {}, isPublic: {}, members: {}", 
                board.getId(), board.getOwner().getUsername(), board.isPublic(), 
                board.getMembers().stream().map(User::getUsername).toList());

        if (!hasAccess(boardId, user)) {
            log.error("Access denied for user {} to board {}", user.getUsername(), boardId);
            throw new AccessDeniedException("You don't have access to this board");
        }

        return board;
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(Long boardId, User user) {
        log.debug("Checking access for user {} to board {}", user.getUsername(), boardId);
        boolean hasAccess = boardRepository.hasAccess(boardId, user);
        log.debug("Access check result: {}", hasAccess);
        return hasAccess;
    }
} 