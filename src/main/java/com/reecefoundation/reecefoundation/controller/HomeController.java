package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.Project;
import com.reecefoundation.reecefoundation.repository.ClassificationRepository;
import com.reecefoundation.reecefoundation.repository.OrganisationRepository;
import com.reecefoundation.reecefoundation.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    ClassificationRepository classificationRepository;

    private String clean(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String projectNumber,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Long organisationId,
            @RequestParam(required = false) Long classificationId,
            Model model) {

        String projectNumberFilter = clean(projectNumber);
        String projectNameFilter = clean(projectName);

        boolean searched = projectNumberFilter != null || projectNameFilter != null
                || organisationId != null || classificationId != null;

        if (searched) {
            List<Project> projects = projectRepository.findAllByOrderByIdDesc().stream()
                    .filter(p -> projectNumberFilter == null
                            || String.valueOf(p.getId()).contains(projectNumberFilter))
                    .filter(p -> projectNameFilter == null
                            || (p.getProjectName() != null
                                && p.getProjectName().toLowerCase().contains(projectNameFilter.toLowerCase())))
                    .filter(p -> organisationId == null
                            || (p.getOrganisation() != null && organisationId.equals(p.getOrganisation().getId())))
                    .filter(p -> classificationId == null
                            || p.hasClassification(classificationId))
                    .toList();
            model.addAttribute("projects", projects);
        }

        model.addAttribute("searched", searched);
        model.addAttribute("organisations", organisationRepository.findAllByOrderByOrganisationNameAsc());
        model.addAttribute("classifications", classificationRepository.findAll());

        model.addAttribute("selectedProjectNumber", projectNumber);
        model.addAttribute("selectedProjectName", projectName);
        model.addAttribute("selectedOrganisationId", organisationId);
        model.addAttribute("selectedClassificationId", classificationId);

        return "index";
    }
}
