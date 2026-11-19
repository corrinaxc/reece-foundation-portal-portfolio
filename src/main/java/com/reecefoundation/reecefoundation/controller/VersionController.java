package com.reecefoundation.reecefoundation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VersionController {
    @GetMapping("/versions")
    public String versionPage(Model model) {
        return "versions";
    }
}
