package com.fastshop.net.config;

import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/** Dữ liệu không tồn tại (findById(...).get()) -> 404 thay vì lỗi 500. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public Object notFound(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/rest/")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy dữ liệu.");
        }
        ModelAndView view = new ModelAndView("error/error-404");
        view.setStatus(HttpStatus.NOT_FOUND);
        return view;
    }
}
