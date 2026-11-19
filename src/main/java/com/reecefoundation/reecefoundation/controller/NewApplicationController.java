package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.Project;
import com.reecefoundation.reecefoundation.model.ProjectForm;
import com.reecefoundation.reecefoundation.model.ProjectNote;
import com.reecefoundation.reecefoundation.repository.ClassificationRepository;
import com.reecefoundation.reecefoundation.repository.OrganisationRepository;
import com.reecefoundation.reecefoundation.repository.OwnerRepository;
import com.reecefoundation.reecefoundation.repository.ProjectRepository;
import com.reecefoundation.reecefoundation.repository.SubClassificationRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class NewApplicationController {

    private final OrganisationRepository organisationRepository;

    private final ClassificationRepository classificationRepository;

    private final SubClassificationRepository subClassificationRepository;

    private final OwnerRepository ownerRepository;

    private final ProjectRepository projectRepository;

    public NewApplicationController(OrganisationRepository organisationRepository, ClassificationRepository classificationRepository, SubClassificationRepository subClassificationRepository, OwnerRepository ownerRepository, ProjectRepository projectRepository) {
        this.organisationRepository = organisationRepository;
        this.classificationRepository = classificationRepository;
        this.subClassificationRepository = subClassificationRepository;
        this.ownerRepository = ownerRepository;
        this.projectRepository = projectRepository;
    }

    private void populateFormOptions(Model model) {
            model.addAttribute("organisations", organisationRepository.findAllByOrderByOrganisationNameAsc());
            model.addAttribute("classifications", classificationRepository.findAll());
            model.addAttribute("owners", ownerRepository.findAll());
            model.addAttribute("statuses", List.of("Pending", "Rejected", "Approved", "Open", "Closed"));
    }

    @GetMapping("/projects/applications/new")
    public String newApplicationPage(Model model) {
        populateFormOptions(model);
        model.addAttribute("projectForm", new ProjectForm());
        return "projects/applications/new";
    }

    @PostMapping("/projects/applications")
    public String submitApplication(
            @Valid @ModelAttribute("projectForm") ProjectForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormOptions(model);
            model.addAttribute("projectForm", form);
            return "projects/applications/new";
        }

        Project project = new Project();
        project.setProjectName(form.getProjectName());
        project.setOverview(form.getOverview());
        project.setStatus(form.getStatus());
        project.setTotalValue(form.getTotalValue());
        project.setProjectLength(form.getProjectLength());
        project.setApplicationDate(form.getApplicationDate());

        String applicationLink = form.getApplicationLink();
        if (applicationLink != null && !applicationLink.isBlank()) {
            project.setApplicationLink(applicationLink.trim());
        }

        project.setPotentialFurtherFunding(Boolean.TRUE.equals(form.getPotentialFurtherFunding()));

        String budgetLink = form.getBudgetLink();
        if (budgetLink != null && !budgetLink.isBlank()) {
            project.setBudgetLink(budgetLink.trim());
        }

        project.setOrganisation(organisationRepository.findById(form.getOrganisationId()).orElseThrow());
        project.getClassifications().clear();
        project.getClassifications().addAll(classificationRepository.findAllById(form.getClassificationIds()));
        project.getSubClassifications().clear();
        if (form.getSubClassificationIds() != null && !form.getSubClassificationIds().isEmpty()) {
            // Only keep sub classifications that belong to a ticked classification
            project.getSubClassifications().addAll(subClassificationRepository
                    .findAllByIdInAndClassificationIdIn(form.getSubClassificationIds(), form.getClassificationIds()));
        }

        if (form.getOwnerId() != null) {
            project.setOwner(ownerRepository.findById(form.getOwnerId()).orElse(null));
        }

        if(form.getProjectNote() != null) {
            ProjectNote projectNote = new ProjectNote();
            projectNote.setProjectNote(form.getProjectNote());
            projectNote.setProject(project);
            project.getProjectNotes().add(projectNote);
        }

        project.setGrantLetterReceived(false);

        projectRepository.save(project);
        return "redirect:/projects/applications";
    }
}
