package com.reecefoundation.reecefoundation.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.reecefoundation.reecefoundation.model.Payment;
import com.reecefoundation.reecefoundation.repository.PaymentDateRepository;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Project id 1 ("GENERAL LINE") is seeded by V5__insert_project_data.sql and is expected
 * to exist in any environment that has run the Flyway migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentImportControllerTest {

    private static final long EXISTING_PROJECT_ID = 1L;
    private static final long MISSING_PROJECT_ID = 999999L;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PaymentDateRepository paymentDateRepository;

    @Test
    @WithMockUser
    void uploadFormRendersCsrfToken() throws Exception {
        mockMvc.perform(get("/upcoming-payments/upload"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
    }

    @Test
    @WithMockUser
    void uploadFormRendersModeSelectNotInsideRawInputTextStyling() throws Exception {
        mockMvc.perform(get("/upcoming-payments/upload"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<select id=\"mode\" name=\"mode\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("type=\"radio\""))));
    }

    @Test
    @WithMockUser
    void previewMatchesKnownProjectAndFlagsUnknownProjectAndBadAmount() throws Exception {
        MockMultipartFile file = buildWorkbook("Project ID", 1);

        MvcResult result = mockMvc.perform(multipart("/upcoming-payments/upload/preview")
                        .file(file).param("mode", "paid").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("upcoming-payments/upload-preview"))
                .andReturn();

        assertEquals(1L, result.getModelAndView().getModel().get("okCount"));
        assertEquals(2L, result.getModelAndView().getModel().get("errorCount"));
        assertEquals(0L, result.getModelAndView().getModel().get("duplicateCount"));
    }

    @Test
    @WithMockUser
    void confirmImportsOkRowsAndSecondImportOfSameFileIsDetectedAsDuplicate() throws Exception {
        MockMultipartFile file = buildWorkbook("Project ID", 1);

        MvcResult previewResult = mockMvc.perform(multipart("/upcoming-payments/upload/preview")
                        .file(file).param("mode", "paid").with(csrf()))
                .andReturn();
        MockHttpSession session = (MockHttpSession) previewResult.getRequest().getSession();

        mockMvc.perform(post("/upcoming-payments/upload/confirm").with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("upcoming-payments/upload-complete"))
                .andExpect(model().attribute("imported", 1));

        MvcResult secondPreview = mockMvc.perform(multipart("/upcoming-payments/upload/preview")
                        .file(file).param("mode", "paid").with(csrf()))
                .andReturn();

        assertEquals(0L, secondPreview.getModelAndView().getModel().get("okCount"));
        assertEquals(1L, secondPreview.getModelAndView().getModel().get("duplicateCount"));
    }

    @Test
    @WithMockUser
    void unpaidModeRoundsDueDateToFirstOfMonthAndLeavesPaymentUnpaid() throws Exception {
        // header uses "ID" (as in the future-payments sheet) and a mid-month date, to prove
        // both the column alias and the 1st-of-month rounding work.
        MockMultipartFile file = buildWorkbook("ID", 15);

        MvcResult previewResult = mockMvc.perform(multipart("/upcoming-payments/upload/preview")
                        .file(file).param("mode", "unpaid").with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(1L, previewResult.getModelAndView().getModel().get("okCount"));
        MockHttpSession session = (MockHttpSession) previewResult.getRequest().getSession();

        mockMvc.perform(post("/upcoming-payments/upload/confirm").with(csrf()).session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("imported", 1));

        List<Payment> payments = paymentDateRepository.findAllByOrderByIdDesc();
        Payment imported = payments.stream()
                .filter(p -> EXISTING_PROJECT_ID == p.getProject().getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected an imported payment for project " + EXISTING_PROJECT_ID));

        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(imported.getPaymentDueDate());

        assertEquals(1, dueDate.get(Calendar.DAY_OF_MONTH), "due date should be rounded to the 1st of the month");
        assertEquals(Calendar.MARCH, dueDate.get(Calendar.MONTH));
        assertEquals(Boolean.FALSE, imported.getPaid());
        assertEquals(null, imported.getPaymentActualDate());
    }

    private MockMultipartFile buildWorkbook(String projectIdHeader, int dayOfMonth) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("m/d/yy"));

            String[] headers = {"Date", "Account name", "Amount", "Account no", "Sort Code",
                    projectIdHeader, "Bank details confirmed", "Invoice"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Calendar cal = Calendar.getInstance();
            cal.set(2026, Calendar.MARCH, dayOfMonth, 0, 0, 0);

            // Row 1: valid, matches an existing project
            Row r1 = sheet.createRow(1);
            Cell r1Date = r1.createCell(0);
            r1Date.setCellValue(cal.getTime());
            r1Date.setCellStyle(dateStyle);
            r1.createCell(1).setCellValue("Test Payee");
            r1.createCell(2).setCellValue(100.00);
            r1.createCell(3).setCellValue("12345678");
            r1.createCell(4).setCellValue("12-34-56");
            r1.createCell(5).setCellValue(String.valueOf(EXISTING_PROJECT_ID));
            r1.createCell(6).setCellValue("Yes");
            r1.createCell(7).setCellValue("No");

            // Row 2: project id does not exist
            Row r2 = sheet.createRow(2);
            Cell r2Date = r2.createCell(0);
            r2Date.setCellValue(cal.getTime());
            r2Date.setCellStyle(dateStyle);
            r2.createCell(1).setCellValue("Unknown Project Payee");
            r2.createCell(2).setCellValue(200.00);
            r2.createCell(5).setCellValue(String.valueOf(MISSING_PROJECT_ID));

            // Row 3: missing amount
            Row r3 = sheet.createRow(3);
            Cell r3Date = r3.createCell(0);
            r3Date.setCellValue(cal.getTime());
            r3Date.setCellStyle(dateStyle);
            r3.createCell(1).setCellValue("No Amount Payee");
            r3.createCell(5).setCellValue(String.valueOf(EXISTING_PROJECT_ID));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new MockMultipartFile("file", "test-payments.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }
}
