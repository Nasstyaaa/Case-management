package org.example.casemanagement.repository;

import org.example.casemanagement.model.entity.Board;
import org.example.casemanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT b FROM Board b WHERE b.owner = :user OR :user MEMBER OF b.members")
    List<Board> findAccessibleBoards(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Board b " +
            "WHERE b.id = :boardId AND (b.owner = :user OR :user MEMBER OF b.members)")
    boolean hasAccess(@Param("boardId") Long boardId, @Param("user") User user);
}
