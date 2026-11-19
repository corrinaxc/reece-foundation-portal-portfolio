package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.Action;
import com.reecefoundation.reecefoundation.model.Owner;
import com.reecefoundation.reecefoundation.model.Project;
import com.reecefoundation.reecefoundation.model.User;
import com.reecefoundation.reecefoundation.repository.ActionRepository;
import com.reecefoundation.reecefoundation.repository.ProjectRepository;
import com.reecefoundation.reecefoundation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ActionRepository actionRepository;

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
        Owner owner = user.getOwner();

        List<Project> projects = projectRepository.findByOwner(owner);
        List<Action> actions = actionRepository.findByOwner(owner).stream()
                .sorted(Action.DISPLAY_ORDER)
                .toList();

        model.addAttribute("owner", owner);
        model.addAttribute("projects", projects);
        model.addAttribute("actions", actions);

        return "profile";
    }
}
