package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.Organisation;
import com.reecefoundation.reecefoundation.model.OrganisationContactDetails;
import com.reecefoundation.reecefoundation.model.OrganisationContactDetailsForm;
import com.reecefoundation.reecefoundation.model.OrganisationForm;
import com.reecefoundation.reecefoundation.model.OrganisationNote;
import com.reecefoundation.reecefoundation.model.OrganisationNoteForm;
import com.reecefoundation.reecefoundation.repository.ProjectBankDetailRepository;
import com.reecefoundation.reecefoundation.repository.OrganisationContactDetailsRepository;
import com.reecefoundation.reecefoundation.repository.OrganisationNoteRepository;
import com.reecefoundation.reecefoundation.repository.OrganisationRepository;
import com.reecefoundation.reecefoundation.repository.PaymentDateRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Controller
public class OrganisationsController {

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationContactDetailsRepository organisationContactDetailsRepository;

    @Autowired
    ProjectBankDetailRepository organisationBankDetailsRepository;

    @Autowired
    PaymentDateRepository paymentDateRepository;

    @Autowired
    OrganisationNoteRepository organisationNoteRepository;

    private String clean(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    @GetMapping("/organisations")
    public String organisationPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String regNumber,
            Model model) {

        String nameFilter = clean(name);
        String typeFilter = clean(type);
        String regNumberFilter = clean(regNumber);

        List<Organisation> organisations = organisationRepository.findAllByOrderByOrganisationNameAsc().stream()
                .filter(o -> nameFilter == null
                        || (o.getOrganisationName() != null
                            && o.getOrganisationName().toLowerCase().contains(nameFilter.toLowerCase())))
                .filter(o -> typeFilter == null || typeFilter.equals(o.getOrganisationType()))
                .filter(o -> regNumberFilter == null
                        || (o.getRegNumber() != null
                            && o.getRegNumber().toLowerCase().contains(regNumberFilter.toLowerCase())))
                .toList();

        model.addAttribute("organisations", organisations);
        model.addAttribute("orgTypes", List.of("Charity", "CIC", "Company"));

        model.addAttribute("selectedName", name);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedRegNumber", regNumber);

        return "organisations/index";
    }

    @GetMapping("/organisations/{id}")
    public String getOrganisationSummary(@PathVariable Long id, Model model) {
        Organisation org = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organisation id: " + id));
        List<OrganisationContactDetails> contactDetails =
                organisationContactDetailsRepository.findByOrganisationId(id);
        OrganisationForm form = new OrganisationForm();
        form.setOrganisationName(org.getOrganisationName());
        form.setOrganisationType(org.getOrganisationType());
        form.setRegNumber(org.getRegNumber());

        model.addAttribute("editMode", false);
        model.addAttribute("organisation", org);
        model.addAttribute("organisationForm", form);
        model.addAttribute("organisationContactDetails", contactDetails);
        model.addAttribute("projects", org.getProjects());
        model.addAttribute("payments", paymentDateRepository.findByProject_Organisation_IdOrderByPaymentDueDateDesc(id));
        model.addAttribute("orgTypes", List.of("Charity", "CIC", "Company"));

        return "organisations/organisation-summary";
    }

    @PostMapping("/organisations/{id}/edit")
    public String updateOrg(@PathVariable Long id,
                            @Valid @ModelAttribute("organisationForm") OrganisationForm form,
                            BindingResult bindingResult,
                            Model model){
        System.out.println("🔥 POST request received for organisation " + id);
        System.out.println("Form org name: " + form.getOrganisationName());

        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organisation ID " + id));

        List<OrganisationContactDetails> contactDetails =
                organisationContactDetailsRepository.findByOrganisationId(id);

        System.out.println("⚠️ Validation errors? " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> System.out.println("❌ " + e));
        }

        if (bindingResult.hasErrors()) {
            // Rebuild the dropdowns for redisplay
            model.addAttribute("organisation", organisation);
            model.addAttribute("organisationForm", form);
            model.addAttribute("organisationContactDetails", contactDetails);
            model.addAttribute("projects", organisation.getProjects());
            model.addAttribute("payments", paymentDateRepository.findByProject_Organisation_IdOrderByPaymentDueDateDesc(id));
            model.addAttribute("editMode", true);
            model.addAttribute("orgTypes", List.of("Charity", "CIC", "Company"));
            return "organisations/organisation-summary";
        }

        organisation.setOrganisationType(form.getOrganisationType());
        organisation.setRegNumber(form.getRegNumber());
        organisation.setOrganisationName(form.getOrganisationName());

        organisationRepository.save(organisation);

        System.out.println("🧾 After setting: organisation name = " + organisation.getOrganisationName());
        System.out.println("✅ Saved organisation " + id);

        return "redirect:/organisations/" + id;

    }

    // Add Contact Details

    @GetMapping("/organisations/{id}/add-contact-details")
    public String addContactDetailsPage(@PathVariable Long id, Model model) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organisation id: " + id));

        model.addAttribute("organisation", organisation);
        model.addAttribute("organisationContactDetailsForm", new OrganisationContactDetailsForm());
        return "organisations/add-contact-details";
    }

    @PostMapping("/organisations/{id}/add-contact-details")
    public String submitContactDetails(@PathVariable Long id,
                                       @Valid @ModelAttribute("organisationContactDetailsForm") OrganisationContactDetailsForm form,
                                       BindingResult bindingResult,
                                       Model model) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organisation id: " + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("organisation", organisation);
            model.addAttribute("organisationContactDetailsForm", form);
            return "organisations/add-contact-details";
        }

        OrganisationContactDetails contactDetails = new OrganisationContactDetails();
        contactDetails.setOrganisation(organisation);
        contactDetails.setName(clean(form.getName()));
        contactDetails.setJobTitle(clean(form.getJobTitle()));
        contactDetails.setAddress(clean(form.getAddress()));
        contactDetails.setEmail(clean(form.getEmail()));
        contactDetails.setTelephone(clean(form.getTelephone()));
        contactDetails.setNotes(clean(form.getNotes()));

        organisationContactDetailsRepository.save(contactDetails);

        return "redirect:/organisations/" + id;
    }

    // Edit Contact Details

    @GetMapping("/organisations/{organisationId}/contact-details/{contactId}/edit")
    public String editContactDetailsPage(@PathVariable Long organisationId,
                                         @PathVariable Long contactId,
                                         Model model) {
        OrganisationContactDetails contactDetails = findContactDetails(organisationId, contactId);

        OrganisationContactDetailsForm form = new OrganisationContactDetailsForm();
        form.setName(contactDetails.getName());
        form.setJobTitle(contactDetails.getJobTitle());
        form.setAddress(contactDetails.getAddress());
        form.setEmail(contactDetails.getEmail());
        form.setTelephone(contactDetails.getTelephone());
        form.setNotes(contactDetails.getNotes());

        model.addAttribute("organisation", contactDetails.getOrganisation());
        model.addAttribute("contactId", contactId);
        model.addAttribute("organisationContactDetailsForm", form);
        return "organisations/edit-contact-details";
    }

    @PostMapping("/organisations/{organisationId}/contact-details/{contactId}/edit")
    public String updateContactDetails(@PathVariable Long organisationId,
                                       @PathVariable Long contactId,
                                       @Valid @ModelAttribute("organisationContactDetailsForm") OrganisationContactDetailsForm form,
                                       BindingResult bindingResult,
                                       Model model) {
        OrganisationContactDetails contactDetails = findContactDetails(organisationId, contactId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("organisation", contactDetails.getOrganisation());
            model.addAttribute("contactId", contactId);
            model.addAttribute("organisationContactDetailsForm", form);
            return "organisations/edit-contact-details";
        }

        contactDetails.setName(clean(form.getName()));
        contactDetails.setJobTitle(clean(form.getJobTitle()));
        contactDetails.setAddress(clean(form.getAddress()));
        contactDetails.setEmail(clean(form.getEmail()));
        contactDetails.setTelephone(clean(form.getTelephone()));
        contactDetails.setNotes(clean(form.getNotes()));

        organisationContactDetailsRepository.save(contactDetails);

        return "redirect:/organisations/" + organisationId;
    }

    // Delete Contact Details

    @PostMapping("/organisations/{organisationId}/contact-details/{contactId}/delete")
    public String deleteContactDetails(@PathVariable Long organisationId,
                                       @PathVariable Long contactId) {
        OrganisationContactDetails contactDetails = findContactDetails(organisationId, contactId);
        organisationContactDetailsRepository.delete(contactDetails);
        return "redirect:/organisations/" + organisationId;
    }

    private OrganisationContactDetails findContactDetails(Long organisationId, Long contactId) {
        OrganisationContactDetails contactDetails = organisationContactDetailsRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid contact id: " + contactId));
        if (!contactDetails.getOrganisation().getId().equals(organisationId)) {
            throw new IllegalArgumentException("Contact " + contactId + " does not belong to organisation " + organisationId);
        }
        return contactDetails;
    }

    // Add Note

    @GetMapping("/organisations/{id}/add-note")
    public String addNotePage(@PathVariable Long id, Model model) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organisation id: " + id));

        model.addAttribute("organisation", organisation);
        model.addAttribute("organisationNoteForm", new OrganisationNoteForm());
        return "organisations/add-note";
    }

    @PostMapping("/organisations/{id}/add-note")
    public String submitNote(@PathVariable Long id,
                             @Valid @ModelAttribute("organisationNoteForm") OrganisationNoteForm form,
                             BindingResult bindingResult,
                             Model model) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organisation id: " + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("organisation", organisation);
            model.addAttribute("organisationNoteForm", form);
            return "organisations/add-note";
        }

        OrganisationNote organisationNote = new OrganisationNote();
        organisationNote.setOrganisation(organisation);
        organisationNote.setOrganisationNote(form.getOrganisationNote());
        organisationNote.setCreatedAt(Date.from(form.getNoteDate()
                .atTime(LocalTime.now())
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        organisationNoteRepository.save(organisationNote);

        return "redirect:/organisations/" + id;
    }

    // Delete Note

    @PostMapping("/organisations/{organisationId}/note/{noteId}/delete")
    public String deleteNote(@PathVariable Long organisationId,
                             @PathVariable Long noteId) {
        organisationNoteRepository.deleteById(noteId);
        return "redirect:/organisations/" + organisationId;
    }

}
