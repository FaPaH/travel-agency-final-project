package com.epam.finaltask.contoller;

import com.epam.finaltask.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class GlobalController {

    @GetMapping
    public String index(Model model) {

        return "index";
    }

    @GetMapping("auth/sign-in")
    public String signIn(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());

        return "auth/sign-in";
    }
}
