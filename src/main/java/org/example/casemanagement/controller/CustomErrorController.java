package org.example.casemanagement.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        log.debug("Handling error: status={}, message={}, exception={}", status, message, exception);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "The page you're looking for was not found");
                model.addAttribute("status", "404");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "You don't have permission to access this resource");
                model.addAttribute("status", "403");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "An internal server error occurred");
                model.addAttribute("status", "500");
            } else {
                model.addAttribute("error", message != null ? message : "An unexpected error occurred");
                model.addAttribute("status", status);
            }
        } else {
            model.addAttribute("error", "An unexpected error occurred");
            model.addAttribute("status", "Unknown");
        }

        return "error";
    }
} 