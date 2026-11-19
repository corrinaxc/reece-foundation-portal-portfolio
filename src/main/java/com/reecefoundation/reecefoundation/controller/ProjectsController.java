package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.*;
import com.reecefoundation.reecefoundation.repository.*;
import jakarta.validation.Valid;
import org.hibernate.engine.jdbc.mutation.spi.BindingGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Controller
public class ProjectsController {
    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OwnerRepository ownerRepository;

    @Autowired
    ClassificationRepository classificationRepository;

    @Autowired
    SubClassificationRepository subClassificationRepository;

    @Autowired
    ProjectBankDetailRepository projectBankDetailRepository;

    @Autowired
    AmountAwardedRepository amountAwardedRepository;

    @Autowired
    PaymentDateRepository paymentDateRepository;

    @Autowired
    ActionRepository actionRepository;

    @Autowired
    MonitoringReportRepository monitoringReportRepository;

    @Autowired
    ProjectNoteRepository projectNoteRepository;

    // Helper methods

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // 👇 true = allow empty strings ("" → null)
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    private String clean(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    // Projects list

    @GetMapping("/projects")
    public String projectsPage(
            @RequestParam(required = false) String projectNumber,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) Long organisationId,
            @RequestParam(required = false) Long classificationId,
            @RequestParam(required = false) Long subClassificationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId,
            Model model) {

        String projectNumberFilter = clean(projectNumber);
        String projectNameFilter = clean(projectName);
        String statusFilter = clean(status);

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
                .filter(p -> subClassificationId == null
                        || p.hasSubClassification(subClassificationId))
                .filter(p -> statusFilter == null || statusFilter.equals(p.getStatus()))
                .filter(p -> ownerId == null
                        || (p.getOwner() != null && ownerId.equals(p.getOwner().getId())))
                .toList();

        model.addAttribute("projects", projects);
        model.addAttribute("organisations", organisationRepository.findAllByOrderByOrganisationNameAsc());
        model.addAttribute("classifications", classificationRepository.findAll());
        model.addAttribute("owners", ownerRepository.findAll());
        model.addAttribute("statuses", List.of("Pending", "Rejected", "Approved", "Open", "Closed"));

        model.addAttribute("selectedProjectNumber", projectNumber);
        model.addAttribute("selectedProjectName", projectName);
        model.addAttribute("selectedOrganisationId", organisationId);
        model.addAttribute("selectedClassificationId", classificationId);
        model.addAttribute("selectedSubClassificationId", subClassificationId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedOwnerId", ownerId);

        return "projects/index";
    }

    // Project Summary

    @GetMapping("/projects/{id}")
    public String projectPage(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        ProjectForm form = new ProjectForm();
        form.setProjectName(project.getProjectName());
        form.setStatus(project.getStatus());
        form.setProjectLength(project.getProjectLength());
        form.setTotalValue(project.getTotalValue());
        form.setApprovedRejectedDate(project.getApprovedRejectedDate());
        form.setOverview(project.getOverview());
        form.setGrantLetterReceived(project.getGrantLetterReceived());
        form.setGrantLetterLink(project.getGrantLetterLink());
        form.setApplicationLink(project.getApplicationLink());
        form.setPotentialFurtherFunding(project.getPotentialFurtherFunding());
        form.setBudgetLink(project.getBudgetLink());

        if (project.getOrganisation() != null)
            form.setOrganisationId(project.getOrganisation().getId());
        project.getClassifications().forEach(c -> form.getClassificationIds().add(c.getId()));
        project.getSubClassifications().forEach(s -> form.getSubClassificationIds().add(s.getId()));
        if (project.getOwner() != null)
            form.setOwnerId(project.getOwner().getId());
        if (project.getAmountAwarded() != null)
            form.setAmountAwardedValue(project.getAmountAwarded().getAmountAwardedValue());
        
        model.addAttribute("project", project);
        model.addAttribute("projectForm", form);
        model.addAttribute("projectBankDetails", project.getBankDetails());
        model.addAttribute("editMode", false);
        model.addAttribute("classifications", classificationRepository.findAll());
        model.addAttribute("statuses", List.of("Pending", "Rejected", "Approved", "Open", "Closed"));
        model.addAttribute("organisations", organisationRepository.findAllByOrderByOrganisationNameAsc());
        model.addAttribute("owners", ownerRepository.findAll());

        return "projects/project/project-summary";
    }

    // Edit Project

    @PostMapping("/projects/{id}/edit")
    public String updateProject(@PathVariable Long id,
                                @Valid @ModelAttribute("projectForm") ProjectForm form,
                                BindingResult bindingResult,
                                Model model) {

            System.out.println("🔥 POST request received for project " + id);
            System.out.println("Form project name: " + form.getProjectName());
            System.out.println("Grant Letter Received from form: " + form.getGrantLetterReceived());



        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));
        System.out.println("⚠️ Validation errors? " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> System.out.println("❌ " + e));
        }

        if (bindingResult.hasErrors()) {
            // Rebuild the dropdowns for redisplay
            model.addAttribute("project", project);
            model.addAttribute("projectForm", form);
            model.addAttribute("organisations", organisationRepository.findAllByOrderByOrganisationNameAsc());
            model.addAttribute("classifications", classificationRepository.findAll());
            model.addAttribute("owners", ownerRepository.findAll());
            model.addAttribute("statuses", List.of("Pending", "Rejected", "Approved", "Open", "Closed"));
            model.addAttribute("editMode", true);
            return "projects/project/project-summary";
        }

        project.setProjectName(form.getProjectName());
        project.setStatus(form.getStatus());
        project.setProjectLength(form.getProjectLength());
        project.setTotalValue(form.getTotalValue());
        project.setApprovedRejectedDate(form.getApprovedRejectedDate());
        project.setOverview(form.getOverview());
        project.setGrantLetterReceived(form.getGrantLetterReceived());
        project.setGrantLetterLink(form.getGrantLetterLink());
        project.setApplicationLink(clean(form.getApplicationLink()));
        project.setPotentialFurtherFunding(Boolean.TRUE.equals(form.getPotentialFurtherFunding()));
        project.setBudgetLink(clean(form.getBudgetLink()));

        project.setOrganisation(organisationRepository.findById(form.getOrganisationId()).orElse(null));
        project.getClassifications().clear();
        project.getClassifications().addAll(classificationRepository.findAllById(form.getClassificationIds()));
        project.getSubClassifications().clear();
        if (form.getSubClassificationIds() != null && !form.getSubClassificationIds().isEmpty()) {
            // Only keep sub classifications that belong to a ticked classification
            project.getSubClassifications().addAll(subClassificationRepository
                    .findAllByIdInAndClassificationIdIn(form.getSubClassificationIds(), form.getClassificationIds()));
        }
        project.setOwner(ownerRepository.findById(form.getOwnerId()).orElse(null));

        if (form.getAmountAwardedValue() != null) {
            AmountAwarded amountAwarded = project.getAmountAwarded();

            if (amountAwarded == null) {
                amountAwarded = new AmountAwarded();
                amountAwarded.setProject(project);
            }

            amountAwarded.setAmountAwardedValue(form.getAmountAwardedValue());
            project.setAmountAwarded(amountAwarded);
        }

        projectRepository.saveAndFlush(project);

        System.out.println("🧾 After setting: project name = " + project.getProjectName());
        System.out.println("✅ Saved project " + id);

        return "redirect:/projects/" + id;
    }

    // Add Payment

@GetMapping("/projects/{id}/add-payment")
    public String addPaymentPage(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        List<ProjectBankDetail> bankDetails = projectBankDetailRepository.findByProjectId(id);

        model.addAttribute("bankDetails", bankDetails);
        model.addAttribute("addPaymentForm", new AddPaymentForm());
        model.addAttribute("project", project);
        return "projects/project/add-payment/add-payment";
    }

    @PostMapping("/projects/{id}/add-payment")
    public String addPayment(
            @PathVariable Long id,
            @Valid @ModelAttribute("addPaymentForm") AddPaymentForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));
            model.addAttribute("project", project);
            model.addAttribute("bankDetails", projectBankDetailRepository.findByProjectId(id));
            return "projects/project/add-payment/add-payment";
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        Payment payment = new Payment();
        payment.setProject(project);

        payment.setPaymentAmount(form.getPaymentAmount());
        payment.setPaymentDueDate(form.getPaymentDueDate());

        if (form.getBankDetailId() != null) {
            ProjectBankDetail bankDetail = projectBankDetailRepository.findById(form.getBankDetailId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid bank detail id: " + form.getBankDetailId()));
            payment.setProjectBankDetail(bankDetail);
        } else {
            payment.setProjectBankDetail(null);
        }


        payment.setInvoiceLink(clean(form.getInvoiceLink()));
        payment.setReceiptLink(null);

        payment.setInvoiced(Boolean.FALSE);
        payment.setBankDetailsConfirmed(Boolean.FALSE);
        payment.setPaid(Boolean.FALSE);
        payment.setReceiptReceived(Boolean.FALSE);
        payment.setPaymentActualDate(null);
        paymentDateRepository.save(payment);

        return "redirect:/projects/" + id;

    }

    @GetMapping("/projects/{projectId}/payment/{paymentId}")
    public String paymentSummaryPage(@PathVariable Long projectId, @PathVariable Long paymentId,  Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));

        Payment payment = paymentDateRepository.findById(paymentId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid payment id: " + paymentId));

        model.addAttribute("project", project);
        model.addAttribute("payment", payment);

        return "projects/project/add-payment/payment-summary";
    }

    // Edit Payment

    @GetMapping("/projects/{projectId}/payment/{paymentId}/edit")
    public String paymentEditPage(@PathVariable Long projectId, @PathVariable Long paymentId, Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));

        Payment payment = paymentDateRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment id: " + paymentId));

        List<ProjectBankDetail> bankDetails = projectBankDetailRepository.findByProjectId(projectId);

        AddPaymentForm form = new AddPaymentForm();
        form.setPaymentAmount(payment.getPaymentAmount());
        form.setPaymentDueDate(payment.getPaymentDueDate());
        form.setInvoiced(payment.getInvoiced());
        form.setInvoiceLink(payment.getInvoiceLink());
        form.setBankDetailsReceived(payment.getBankDetailsConfirmed());
        form.setPaid(payment.getPaid());
        form.setPaymentActualDate(payment.getPaymentActualDate());
        form.setReceiptReceived(payment.getReceiptReceived());
        form.setReceiptLink(payment.getReceiptLink());

        if (payment.getProjectBankDetail() != null) {
            form.setBankDetailId(payment.getProjectBankDetail().getId());
        }

        model.addAttribute("project", project);
        model.addAttribute("payment", payment);
        model.addAttribute("bankDetails", bankDetails);
        model.addAttribute("addPaymentForm", form);

        return "projects/project/add-payment/edit";
    }

    @PostMapping("/projects/{projectId}/payment/{paymentId}/edit")
    public String editPayment(
            @PathVariable Long projectId,
            @PathVariable Long paymentId,
            @Valid @ModelAttribute("addPaymentForm") AddPaymentForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));
            Payment payment = paymentDateRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid payment id: " + paymentId));
            model.addAttribute("project", project);
            model.addAttribute("payment", payment);
            model.addAttribute("bankDetails", projectBankDetailRepository.findByProjectId(projectId));
            return "projects/project/add-payment/edit";
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));

        Payment payment = paymentDateRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment id: " + paymentId));

        List<ProjectBankDetail> bankDetails = projectBankDetailRepository.findByProjectId(projectId);

        String invoiceLink = clean(form.getInvoiceLink());
        String receiptLink = clean(form.getReceiptLink());

        if (form.getBankDetailId() != null) {
            ProjectBankDetail bankDetail = projectBankDetailRepository.findById(form.getBankDetailId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid bank detail id: " + form.getBankDetailId()));
            payment.setProjectBankDetail(bankDetail);
        } else {
            payment.setProjectBankDetail(null);
        }

        payment.setPaymentAmount(form.getPaymentAmount());
        payment.setPaymentDueDate(form.getPaymentDueDate());
        payment.setInvoiced(form.getInvoiced());
        payment.setInvoiceLink(invoiceLink);
        payment.setBankDetailsConfirmed(form.getBankDetailsReceived());
        payment.setPaid(form.getPaid());
        payment.setPaymentActualDate(form.getPaymentActualDate());
        payment.setReceiptReceived(form.getReceiptReceived());
        payment.setReceiptLink(receiptLink);

        paymentDateRepository.save(payment);

        return "redirect:/projects/" + projectId + "/payment/" + paymentId;
    }

    // Delete Payment

    @PostMapping("/projects/{projectId}/payment/{paymentId}/delete")
    public String deletePayment(@PathVariable Long projectId,
                                @PathVariable Long paymentId,
                                @RequestParam(required = false) String redirect) {

        paymentDateRepository.deleteById(paymentId);

        if (redirect != null) {
            return "redirect:" + redirect;
        }

        return "redirect:/projects/" + projectId;
    }

    //Add Bank Details

    @GetMapping("/projects/{id}/bank-details/add-bank-details")
    public String addBankDetailsPage(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        model.addAttribute("project", project);
        model.addAttribute("projectBankDetailsForm", new ProjectBankDetailForm()); // lowercase attribute name
        return "projects/project/bank-details/add-bank-details";
    }

    @PostMapping("/projects/{id}/bank-details/add-bank-details")
    public String addBankDetails(
            @PathVariable Long id,
            @Valid @ModelAttribute("projectBankDetailsForm") ProjectBankDetailForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));
            model.addAttribute("project", project);
            return "projects/project/bank/details/add-bank-details";
        }

        boolean exists = projectBankDetailRepository.existsByProjectIdAndAccountNumberAndSortCode(
                id,
                form.getAccountNumber(),
                form.getSortCode()
        );

        if (exists) {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));
            bindingResult.reject("duplicateBankDetails",
                    "These bank details already exist for this project.");
            model.addAttribute("project", project);
            return "projects/project/add-bank-details";
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        ProjectBankDetail projectBankDetail = new ProjectBankDetail();
        projectBankDetail.setProject(project);

        projectBankDetail.setPayee(form.getPayee());
        projectBankDetail.setAccountNumber(form.getAccountNumber());
        projectBankDetail.setSortCode(form.getSortCode());

        if (form.getLink() != null && !form.getLink().trim().isEmpty()) {
            projectBankDetail.setBankDetailsLink(form.getLink().trim());
        }

        if (form.getNote() != null && !form.getNote().trim().isEmpty()) {
            projectBankDetail.setNote(form.getNote().trim());
        }

        projectBankDetailRepository.save(projectBankDetail);

        return "redirect:/projects/" + id;
    }

    //Edit Bank Details

    @GetMapping("/projects/{projectId}/bank-details/{bankDetailId}/edit")
    public String editBankDetailsPage(@PathVariable Long projectId,
                                      @PathVariable Long bankDetailId,
                                      Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));

        ProjectBankDetail bankDetail = projectBankDetailRepository.findById(bankDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid bank detail id: " + bankDetailId));

        ProjectBankDetailForm form = new ProjectBankDetailForm();
        form.setPayee(bankDetail.getPayee());
        form.setAccountNumber(bankDetail.getAccountNumber());
        form.setSortCode(bankDetail.getSortCode());
        form.setLink(bankDetail.getBankDetailsLink());
        form.setNote(bankDetail.getNote());

        model.addAttribute("project", project);
        model.addAttribute("bankDetail", bankDetail);
        model.addAttribute("projectBankDetailsForm", form);

        return "projects/project/bank-details/edit";

    }

    @PostMapping("/projects/{projectId}/bank-details/{bankDetailId}/edit")
    public String editBankDetails(@PathVariable Long projectId,
                                  @PathVariable Long bankDetailId,
                                  @Valid @ModelAttribute("projectBankDetailsForm") ProjectBankDetailForm form,
                                  BindingResult bindingResult,
                                  Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));

        ProjectBankDetail bankDetail = projectBankDetailRepository.findById(bankDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid bank detail id: " + bankDetailId));

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("bankDetail", bankDetail);
            return "projects/project/bank-details/edit";
        }

        boolean exists = projectBankDetailRepository.existsByProjectIdAndAccountNumberAndSortCodeAndIdNot(
                projectId,
                form.getAccountNumber(),
                form.getSortCode(),
                bankDetailId
        );

        if (exists) {
            bindingResult.reject("duplicateBankDetails",
                    "These bank details already exist for this project.");
            model.addAttribute("project", project);
            return "projects/project/add-bank-details";
        }

        String link = clean(form.getLink());
        String note = clean(form.getNote());

        bankDetail.setPayee(form.getPayee());
        bankDetail.setAccountNumber(form.getAccountNumber());
        bankDetail.setSortCode(form.getSortCode());
        bankDetail.setBankDetailsLink(link);
        bankDetail.setNote(note);

        projectBankDetailRepository.save(bankDetail);

        return "redirect:/projects/" + projectId;

    }

    @PostMapping("/projects/{projectId}/bank-details/{bankDetailId}/delete")
    public String deleteBankDetail(@PathVariable Long projectId,
                                   @PathVariable Long bankDetailId) {
        projectBankDetailRepository.deleteById(bankDetailId);
        return "redirect:/projects/" + projectId;
    }

    // Add Monitoring Report

    @GetMapping("/projects/{id}/add-monitoring-report")
    public String addMonitoringReport(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        model.addAttribute("project", project);
        model.addAttribute("monitoringReportForm", new MonitoringReportForm());
        return "projects/project/add-m-report";
    }

    @PostMapping("/projects/{id}/add-monitoring-report")
    public String submitMonitoringReport(@PathVariable Long id,
                                         @Valid @ModelAttribute("monitoringReportForm") MonitoringReportForm form,
                                         BindingResult bindingResult,
                                         Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("monitoringReportForm", form);
            return "projects/project/add-m-report";
        }

        MonitoringReport monitoringReport = new MonitoringReport();
        monitoringReport.setProject(project);
        monitoringReport.setReportDueDate(form.getReportDueDate());
        monitoringReport.setReportReceivedDate(form.getReportReceivedDate());
        monitoringReport.setReportApprovedDate(form.getReportApprovedDate());
        monitoringReport.setReportLink(clean(form.getReportLink()));

        monitoringReportRepository.save(monitoringReport);

        return "redirect:/projects/" + id;
    }

    // Edit Monitoring Report

    @GetMapping("/projects/{projectId}/monitoring-report/{reportId}/edit")
    public String editMonitoringReportPage(@PathVariable Long projectId,
                                           @PathVariable Long reportId,
                                           Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));
        MonitoringReport monitoringReport = monitoringReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid monitoring report id: " + reportId));

        MonitoringReportForm form = new MonitoringReportForm();
        form.setReportDueDate(monitoringReport.getReportDueDate());
        form.setReportReceivedDate(monitoringReport.getReportReceivedDate());
        form.setReportApprovedDate(monitoringReport.getReportApprovedDate());
        form.setReportLink(monitoringReport.getReportLink());

        model.addAttribute("project", project);
        model.addAttribute("monitoringReport", monitoringReport);
        model.addAttribute("monitoringReportForm", form);
        return "projects/project/monitoring-reports/edit";
    }

    @PostMapping("/projects/{projectId}/monitoring-report/{reportId}/edit")
    public String editMonitoringReport(@PathVariable Long projectId,
                                       @PathVariable Long reportId,
                                       @Valid @ModelAttribute("monitoringReportForm") MonitoringReportForm form,
                                       BindingResult bindingResult,
                                       Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));
        MonitoringReport monitoringReport = monitoringReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid monitoring report id: " + reportId));

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("monitoringReport", monitoringReport);
            return "projects/project/monitoring-reports/edit";
        }

        monitoringReport.setReportDueDate(form.getReportDueDate());
        monitoringReport.setReportReceivedDate(form.getReportReceivedDate());
        monitoringReport.setReportApprovedDate(form.getReportApprovedDate());
        monitoringReport.setReportLink(clean(form.getReportLink()));

        monitoringReportRepository.save(monitoringReport);

        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/monitoring-report/{reportId}/delete")
    public String deleteMonitoringReport(@PathVariable Long projectId,
                                         @PathVariable Long reportId) {
        monitoringReportRepository.deleteById(reportId);
        return "redirect:/projects/" + projectId;
    }

    // Add Action

    @GetMapping("/projects/{id}/add-action")
    public String addActionPage(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        model.addAttribute("project", project);
        model.addAttribute("actionForm", new ActionForm());
        model.addAttribute("owners", ownerRepository.findAll());
        return "projects/project/add-action";
    }

    @PostMapping("/projects/{id}/add-action")
    public String submitAction(@PathVariable Long id, @Valid @ModelAttribute("actionForm") ActionForm form, BindingResult bindingResult, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("owners", ownerRepository.findAll());
            model.addAttribute("actionForm", form);
            return "projects/project/add-action";
        }

        Action action = new Action();
        action.setActionDescription(form.getActionDescription());
        action.setProject(project);

        if(form.getDueDate() != null) {
            action.setDueDate(form.getDueDate());
        }

        if(form.getOwnerId() != null) {
            action.setOwner(ownerRepository.findById(form.getOwnerId()).orElse(null));
        }

        actionRepository.save(action);
        return "redirect:/projects/" + id;
    }

    // Edit Action

    @GetMapping("/projects/{projectId}/action/{actionId}/edit")
    public String editActionPage(@PathVariable Long projectId,
                                 @PathVariable Long actionId,
                                 Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid action id: " + actionId));

        ActionForm form = new ActionForm();
        form.setActionDescription(action.getActionDescription());
        form.setDueDate(action.getDueDate());
        form.setOwnerId(action.getOwner() != null ? action.getOwner().getId() : null);
        form.setCompleted(Boolean.TRUE.equals(action.getCompleted()));

        model.addAttribute("project", project);
        model.addAttribute("action", action);
        model.addAttribute("actionForm", form);
        model.addAttribute("owners", ownerRepository.findAll());
        return "projects/project/actions/edit";
    }

    @PostMapping("/projects/{projectId}/action/{actionId}/edit")
    public String editAction(@PathVariable Long projectId,
                             @PathVariable Long actionId,
                             @Valid @ModelAttribute("actionForm") ActionForm form,
                             BindingResult bindingResult,
                             Model model) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + projectId));
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid action id: " + actionId));

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("action", action);
            model.addAttribute("owners", ownerRepository.findAll());
            return "projects/project/actions/edit";
        }

        action.setActionDescription(form.getActionDescription());
        action.setDueDate(form.getDueDate());
        action.setOwner(form.getOwnerId() != null
                ? ownerRepository.findById(form.getOwnerId()).orElse(null)
                : null);
        action.setCompleted(form.isCompleted());

        actionRepository.save(action);
        return "redirect:/projects/" + projectId;
    }

    @PostMapping("/projects/{projectId}/action/{actionId}/delete")
    public String deleteAction(@PathVariable Long projectId,
                               @PathVariable Long actionId) {
        actionRepository.deleteById(actionId);
        return "redirect:/projects/" + projectId;
    }

    // Called in the background by actions.js, so the page doesn't reload
    @PostMapping(value = "/projects/{projectId}/action/{actionId}/delete", headers = "X-Requested-With=fetch")
    public ResponseEntity<Void> deleteActionInBackground(@PathVariable Long projectId,
                                                         @PathVariable Long actionId) {
        actionRepository.deleteById(actionId);
        return ResponseEntity.noContent().build();
    }

    // Sets completed to the checkbox's state: a ticked box sends completed=true, an unticked one sends nothing
    @PostMapping("/projects/{projectId}/action/{actionId}/toggle-complete")
    public String toggleActionComplete(@PathVariable Long projectId,
                                       @PathVariable Long actionId,
                                       @RequestParam(name = "completed", required = false) Boolean completed,
                                       @RequestParam(name = "returnTo", required = false) String returnTo) {
        setActionCompleted(actionId, completed);

        if ("profile".equals(returnTo)) {
            return "redirect:/profile";
        }
        return "redirect:/projects/" + projectId;
    }

    // Called in the background by actions.js, so the page doesn't reload
    @PostMapping(value = "/projects/{projectId}/action/{actionId}/toggle-complete", headers = "X-Requested-With=fetch")
    public ResponseEntity<Void> toggleActionCompleteInBackground(@PathVariable Long projectId,
                                                                 @PathVariable Long actionId,
                                                                 @RequestParam(name = "completed", required = false) Boolean completed) {
        setActionCompleted(actionId, completed);
        return ResponseEntity.noContent().build();
    }

    private void setActionCompleted(Long actionId, Boolean completed) {
        Action action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid action id: " + actionId));
        action.setCompleted(Boolean.TRUE.equals(completed));
        actionRepository.save(action);
    }

// Add Note

    @GetMapping("/projects/{id}/add-note")
    public String addNotePage(@PathVariable Long id, Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        model.addAttribute("project", project);
        model.addAttribute("projectNoteForm", new ProjectNoteForm());
        return "projects/project/add-note";
    }

    @PostMapping("/projects/{id}/add-note")
    public String submitNote(@PathVariable Long id,
                             @Valid @ModelAttribute("projectNoteForm") ProjectNoteForm form,
                             BindingResult bindingResult,
                             Model model) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project id: " + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("projectNoteForm", form);
            return "projects/project/add-note";
        }

        ProjectNote projectNote = new ProjectNote();
        projectNote.setProject(project);
        projectNote.setProjectNote(form.getProjectNote());
        projectNote.setCreatedAt(Date.from(form.getNoteDate()
                .atTime(LocalTime.now())
                .atZone(ZoneId.systemDefault())
                .toInstant()));

        projectNoteRepository.save(projectNote);

        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{projectId}/note/{noteId}/delete")
    public String deleteNote(@PathVariable Long projectId,
                             @PathVariable Long noteId) {
        projectNoteRepository.deleteById(noteId);
        return "redirect:/projects/" + projectId;
    }

    // Applications List
    @GetMapping("/projects/applications")
    public String applicationManagerPage(Model model) {
        List<Project> projects = projectRepository.findByStatusOrderByIdDesc("Pending");
        model.addAttribute("projects", projects);
        return "projects/applications/index";
    }

}
