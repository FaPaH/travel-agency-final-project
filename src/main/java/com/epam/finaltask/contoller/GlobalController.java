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

    @GetMapping("index")
    public String index(Model model) {
        return "index";
    }

    @GetMapping
    public String index2(Model model) {
        return "index";
    }
}
