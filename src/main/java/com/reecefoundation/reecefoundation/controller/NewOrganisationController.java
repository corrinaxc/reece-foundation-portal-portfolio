package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.Organisation;
import com.reecefoundation.reecefoundation.model.OrganisationContactDetails;
import com.reecefoundation.reecefoundation.model.OrganisationForm;
import com.reecefoundation.reecefoundation.model.OrganisationNote;
import com.reecefoundation.reecefoundation.repository.OrganisationContactDetailsRepository;
import com.reecefoundation.reecefoundation.repository.OrganisationRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class NewOrganisationController {

    private final OrganisationRepository organisationRepository;
    private final OrganisationContactDetailsRepository organisationContactDetailsRepository;

    public NewOrganisationController(OrganisationRepository organisationRepository, OrganisationContactDetailsRepository organisationContactDetailsRepository) {
        this.organisationRepository = organisationRepository;
        this.organisationContactDetailsRepository = organisationContactDetailsRepository;
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("orgTypes", List.of("Charity", "CIC", "Company"));
    }

    @GetMapping("/organisations/new")
    public String newOrganisationPage(Model model) {
        populateFormOptions(model);
        model.addAttribute("organisationForm", new OrganisationForm());
        return "organisations/new";
    }

    @PostMapping("/organisations/new")
    public String addOrganisation(
            @Valid @ModelAttribute("organisationForm") OrganisationForm form,
            BindingResult bindingResult,
            Model model) {

        if (organisationRepository.existsByOrganisationName(form.getOrganisationName())) {
            bindingResult.rejectValue("organisationName", "unique", "Organisation Name already exists");
        }

        if (organisationRepository.existsByRegNumber(form.getRegNumber())) {
            bindingResult.rejectValue("regNumber", "unique", "Registration Number already exists");
        }

        if (bindingResult.hasErrors()) {
            populateFormOptions(model);
            model.addAttribute("organisationForm", form);
            return "organisations/new";
        }

        Organisation organisation = new Organisation();
        organisation.setOrganisationName(form.getOrganisationName());
        organisation.setOrganisationType(form.getOrganisationType());
        organisation.setRegNumber(form.getRegNumber());

        if (form.getOrganisationNote() != null && !form.getOrganisationNote().isBlank()) {
            OrganisationNote organisationNote = new OrganisationNote();
            organisationNote.setOrganisationNote(form.getOrganisationNote());
            organisationNote.setOrganisation(organisation);
            organisation.getOrganisationNotes().add(organisationNote);
        }

        if (StringUtils.hasText(form.getName()) ||
                StringUtils.hasText(form.getAddress()) ||
                StringUtils.hasText(form.getJobTitle()) ||
                StringUtils.hasText(form.getTelephone()) ||
                StringUtils.hasText(form.getEmail())) {

            OrganisationContactDetails contact = new OrganisationContactDetails();
            contact.setName(form.getName());
            contact.setAddress(form.getAddress());
            contact.setJobTitle(form.getJobTitle());
            contact.setTelephone(form.getTelephone());
            contact.setEmail(form.getEmail());
            contact.setOrganisation(organisation);

            organisation.getContactDetails().add(contact);
        }

        try {
            organisationRepository.save(organisation);
        } catch (DataIntegrityViolationException e) {
            if (e.getMostSpecificCause().getMessage().contains("organisation_name")) {
                bindingResult.rejectValue("organisationName", "unique", "Organisation Name already exists");
            } else if (e.getMostSpecificCause().getMessage().contains("reg_number")) {
                bindingResult.rejectValue("regNumber", "unique", "Registration Number already exists");
            }
            populateFormOptions(model);
            return "organisations/new";
        }
        return "redirect:/organisations";
    }
}
