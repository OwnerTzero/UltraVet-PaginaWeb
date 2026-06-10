package com.ultravet.veterinaria.controller;

import com.ultravet.veterinaria.dto.LoginForm;
import com.ultravet.veterinaria.dto.RegistroUsuarioForm;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }

        return requestUri;
    }

    @ModelAttribute
    public void authForms(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }

        if (!model.containsAttribute("registroUsuarioForm")) {
            model.addAttribute("registroUsuarioForm", new RegistroUsuarioForm());
        }

        if (!model.containsAttribute("abrirAuthModal")) {
            model.addAttribute("abrirAuthModal", false);
        }

        if (!model.containsAttribute("authMode")) {
            model.addAttribute("authMode", "login");
        }
    }
}
