package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.Payment;
import com.reecefoundation.reecefoundation.model.ProjectBankDetail;
import com.reecefoundation.reecefoundation.repository.OrganisationRepository;
import com.reecefoundation.reecefoundation.repository.PaymentDateRepository;
import com.reecefoundation.reecefoundation.repository.ProjectRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Controller
public class UpcomingPaymentsController {
    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    PaymentDateRepository paymentDateRepository;

    @GetMapping("/upcoming-payments")
    public String upcomingPaymentsPage(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "false") boolean unpaidOnly,
            Model model) {

        List<Payment> payments;
        if (month != null && year != null && unpaidOnly) {
            payments = paymentDateRepository.findUnpaidByMonthAndYear(month, year);
        } else if (month != null && year != null) {
            payments = paymentDateRepository.findByMonthAndYear(month, year);
        } else if (unpaidOnly) {
            payments = paymentDateRepository.findAllUnpaid();
        } else {
            payments = paymentDateRepository.findAllByOrderByIdDesc();
        }

        model.addAttribute("payments", payments);
        model.addAttribute("months", Month.values());
        model.addAttribute("currentYear", java.time.Year.now().getValue());
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("unpaidOnly", unpaidOnly);

        return "upcoming-payments/index";
    }

    @GetMapping("/upcoming-payments/export")
    public void exportToExcel(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "false") boolean unpaidOnly,
            HttpServletResponse response) throws IOException {

        List<Payment> payments;
        String label;
        if (month != null && year != null && unpaidOnly) {
            payments = paymentDateRepository.findUnpaidByMonthAndYear(month, year);
            label = "unpaid_" + Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "_" + year;
        } else if (month != null && year != null) {
            payments = paymentDateRepository.findByMonthAndYear(month, year);
            label = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "_" + year;
        } else if (unpaidOnly) {
            payments = paymentDateRepository.findAllUnpaid();
            label = "unpaid_all";
        } else {
            payments = paymentDateRepository.findAllByOrderByIdDesc();
            label = "all";
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=payments_" + label + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payments");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] columns = {"Project Number", "Project Name", "Organisation", "Payment Amount", "Payment Due Date", "Invoice Received", "Paid", "Payee", "Account Number", "Sort Code"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            int rowNum = 1;
            for (Payment p : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getProject().getId());
                row.createCell(1).setCellValue(p.getProject().getProjectName());
                row.createCell(2).setCellValue(p.getProject().getOrganisation().getOrganisationName());
                row.createCell(3).setCellValue(p.getPaymentAmount() != null ? p.getPaymentAmount().doubleValue() : 0);
                row.createCell(4).setCellValue(p.getPaymentDueDate() != null ? sdf.format(p.getPaymentDueDate()) : "");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(p.getInvoiced()) ? "Yes" : "No");
                row.createCell(6).setCellValue(Boolean.TRUE.equals(p.getPaid()) ? "Yes" : "No");
                ProjectBankDetail bank = p.getProjectBankDetail();
                row.createCell(7).setCellValue(bank != null ? bank.getPayee() : "");
                row.createCell(8).setCellValue(bank != null ? bank.getAccountNumber() : "");
                row.createCell(9).setCellValue(bank != null ? bank.getSortCode() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}
