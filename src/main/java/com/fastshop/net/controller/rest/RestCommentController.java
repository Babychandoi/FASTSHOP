package com.fastshop.net.controller.rest;

import org.springframework.web.bind.annotation.RestController;
import com.fastshop.net.model.Comment;
import com.fastshop.net.service.AccountService;
import com.fastshop.net.service.CommentService;
import com.fastshop.net.service.ProductService;
import com.fastshop.net.service._CookieService;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin("*")
@RestController
public class RestCommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    AccountService accountService;
    @Autowired
    ProductService productService;
    @Autowired
    _CookieService cookie;

    @GetMapping("/rest/comments/{id}")
    public Comment getById(@PathVariable("id") Integer id) {
        return commentService.findById(id);
    }
    
    @GetMapping("/rest/comments/accounts/{username}")
    public List<Comment> getCommentByAccount(@PathVariable("username") String account) {
        try {
            return commentService.findByAccount(accountService.findById(account));
        } catch (Exception e) {
            return null;
        }
    }
    @PostMapping("/rest/comments")
    public ResponseEntity<String> post(@RequestBody Comment comment) {
        if (comment.getRate() == null || comment.getRate() < 1 || comment.getRate() > 5) {
            return ResponseEntity.badRequest().body("(*) Vui lòng chọn số sao từ 1 đến 5.");
        }
        if (comment.getFeedback() == null || comment.getFeedback().isBlank() || comment.getProduct() == null) {
            return ResponseEntity.badRequest().body("(*) Vui lòng nhập nội dung đánh giá.");
        }
        comment.setId(null);
        comment.setAccount(accountService.findById(cookie.getValue("username")));
        comment.setProduct(productService.findById(comment.getProduct().getId()));
        comment.setFeedback(comment.getFeedback().trim());
        comment.setDatePost(new Date());
        commentService.save(comment);
        return ResponseEntity.ok("OK");
    }

    @PutMapping("/rest/comments/{id}")
    public Comment put(@PathVariable("id") Integer id, @RequestBody Comment comment) {
        if (commentService.findAll().contains(comment)) {
            commentService.save(comment);
        }
        return comment;
    }
    
    @DeleteMapping("/rest/comments/{id}")
    public void delete(@PathVariable("id") Integer id) {
        commentService.deleteById(id);
    }
}
