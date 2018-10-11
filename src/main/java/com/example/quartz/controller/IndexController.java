package com.example.quartz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {
    private static final Long timestamp = System.currentTimeMillis();

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("timestamp", timestamp);
        return "index";
    }
}
