package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.MyUserDetails;
import sk.tuke.gamestudio.server.controller.UserController;
import sk.tuke.gamestudio.server.dto.CommentDto;
import sk.tuke.gamestudio.service.CommentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comment")
public class CommentServiceRest {
    @Autowired
    private CommentService commentService;

    @GetMapping("/{game}")
    public List<CommentDto> getComments(@PathVariable @NotBlank String game) {
        return commentService.getComments(game).stream().map(CommentDto::fromComment).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Void> addComment(@RequestBody @Valid CommentDto commentDto) {
        /*if (SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);*/
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment comment = commentDto.toComment();
        comment.setPlayer(userDetails.getUser());
        comment.setGame("JigsawSudoku");
        commentService.addComment(comment);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}