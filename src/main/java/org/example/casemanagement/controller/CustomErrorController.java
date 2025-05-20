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
                model.addAttribute("error", "Запрашиваемая страница не найдена");
                model.addAttribute("status", "404");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "У вас нет прав для доступа к этому ресурсу");
                model.addAttribute("status", "403");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("error", "Произошла внутренняя ошибка сервера");
                model.addAttribute("status", "500");
            } else {
                model.addAttribute("error", message != null ? message : "Произошла непредвиденная ошибка");
                model.addAttribute("status", status);
            }
        } else {
            model.addAttribute("error", "Произошла непредвиденная ошибка");
            model.addAttribute("status", "Неизвестно");
        }

        return "error";
    }
} 