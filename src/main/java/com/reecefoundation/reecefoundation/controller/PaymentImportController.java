package com.reecefoundation.reecefoundation.controller;

import com.reecefoundation.reecefoundation.model.ParsedPaymentRow;
import com.reecefoundation.reecefoundation.model.Payment;
import com.reecefoundation.reecefoundation.model.Project;
import com.reecefoundation.reecefoundation.model.ProjectBankDetail;
import com.reecefoundation.reecefoundation.repository.PaymentDateRepository;
import com.reecefoundation.reecefoundation.repository.ProjectBankDetailRepository;
import com.reecefoundation.reecefoundation.repository.ProjectRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class PaymentImportController {

    private static final String SESSION_KEY = "paymentImportRows";

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectBankDetailRepository projectBankDetailRepository;

    @Autowired
    PaymentDateRepository paymentDateRepository;

    @GetMapping("/upcoming-payments/upload")
    public String uploadPage() {
        return "upcoming-payments/upload";
    }

    @PostMapping("/upcoming-payments/upload/preview")
    public String preview(@RequestParam("file") MultipartFile file,
                           @RequestParam("mode") String mode,
                           HttpSession session, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("error", "Please choose a file to upload.");
            return "upcoming-payments/upload";
        }

        if (!"paid".equals(mode) && !"unpaid".equals(mode)) {
            model.addAttribute("error", "Please choose whether these are paid or unpaid payments.");
            return "upcoming-payments/upload";
        }

        List<ParsedPaymentRow> rows;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            rows = parseWorkbook(workbook, mode);
        } catch (Exception e) {
            model.addAttribute("error", "Could not read that file: " + e.getMessage());
            return "upcoming-payments/upload";
        }

        session.setAttribute(SESSION_KEY, rows);

        long okCount = rows.stream().filter(r -> "OK".equals(r.getStatus())).count();
        long duplicateCount = rows.stream().filter(r -> "DUPLICATE".equals(r.getStatus())).count();
        long errorCount = rows.stream().filter(r -> "ERROR".equals(r.getStatus())).count();

        model.addAttribute("rows", rows);
        model.addAttribute("okCount", okCount);
        model.addAttribute("duplicateCount", duplicateCount);
        model.addAttribute("errorCount", errorCount);
        model.addAttribute("mode", mode);

        return "upcoming-payments/upload-preview";
    }

    @PostMapping("/upcoming-payments/upload/confirm")
    public String confirm(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ParsedPaymentRow> rows = (List<ParsedPaymentRow>) session.getAttribute(SESSION_KEY);

        if (rows == null) {
            return "redirect:/upcoming-payments/upload";
        }

        int imported = 0;
        for (ParsedPaymentRow row : rows) {
            if (!"OK".equals(row.getStatus())) {
                continue;
            }

            Optional<Project> projectOpt = projectRepository.findById(row.getProjectId());
            if (projectOpt.isEmpty()) {
                continue;
            }

            Payment payment = new Payment();
            payment.setProject(projectOpt.get());
            payment.setPaymentAmount(row.getAmount());
            payment.setPaymentDueDate(row.getDate());
            payment.setPaid(row.isPaid());
            payment.setPaymentActualDate(row.isPaid() ? row.getDate() : null);
            payment.setInvoiced(row.isInvoiced());
            payment.setBankDetailsConfirmed(row.isBankDetailsConfirmed());
            payment.setReceiptReceived(Boolean.FALSE);
            payment.setInvoiceLink(null);
            payment.setReceiptLink(null);

            if (row.getMatchedBankDetailId() != null) {
                projectBankDetailRepository.findById(row.getMatchedBankDetailId())
                        .ifPresent(payment::setProjectBankDetail);
            }

            paymentDateRepository.save(payment);
            imported++;
        }

        session.removeAttribute(SESSION_KEY);
        model.addAttribute("imported", imported);
        return "upcoming-payments/upload-complete";
    }

    private List<ParsedPaymentRow> parseWorkbook(Workbook workbook, String mode) {
        boolean paid = "paid".equals(mode);

        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow == null) {
            throw new IllegalArgumentException("Sheet has no header row");
        }

        int dateCol = findColumn(headerRow, "Date");
        int amountCol = findColumn(headerRow, "Amount");
        int accountNameCol = findColumn(headerRow, "Account name");
        int accountNoCol = findColumn(headerRow, "Account no");
        int sortCodeCol = findColumn(headerRow, "Sort Code");
        int projectIdCol = findColumnAny(headerRow, "Project ID", "ID");
        int bankConfirmedCol = findColumn(headerRow, "Bank details confirmed");
        int invoiceCol = findColumn(headerRow, "Invoice");

        if (dateCol < 0 || amountCol < 0 || projectIdCol < 0) {
            throw new IllegalArgumentException("Expected columns 'Date', 'Amount' and 'Project ID' were not found");
        }

        List<ParsedPaymentRow> rows = new ArrayList<>();
        int lastRow = sheet.getLastRowNum();

        for (int r = headerRow.getRowNum() + 1; r <= lastRow; r++) {
            Row excelRow = sheet.getRow(r);
            if (excelRow == null || isRowBlank(excelRow)) {
                continue;
            }

            ParsedPaymentRow parsed = new ParsedPaymentRow();
            parsed.setRowNumber(r + 1);

            Date date = readDate(excelRow.getCell(dateCol));
            if (date != null && !paid) {
                date = firstOfMonth(date);
            }
            BigDecimal amount = readAmount(excelRow.getCell(amountCol));
            String rawProjectId = readString(excelRow.getCell(projectIdCol));
            String accountName = accountNameCol >= 0 ? readString(excelRow.getCell(accountNameCol)) : null;
            String accountNo = accountNoCol >= 0 ? readString(excelRow.getCell(accountNoCol)) : null;
            String sortCode = sortCodeCol >= 0 ? normaliseSortCode(readString(excelRow.getCell(sortCodeCol))) : null;
            boolean bankConfirmed = bankConfirmedCol >= 0 && "yes".equalsIgnoreCase(readString(excelRow.getCell(bankConfirmedCol)));
            boolean invoiced = invoiceCol >= 0 && "yes".equalsIgnoreCase(readString(excelRow.getCell(invoiceCol)));

            parsed.setDate(date);
            parsed.setAmount(amount);
            parsed.setRawProjectId(rawProjectId);
            parsed.setAccountName(accountName);
            parsed.setAccountNo(accountNo);
            parsed.setSortCode(sortCode);
            parsed.setBankDetailsConfirmed(bankConfirmed);
            parsed.setInvoiced(invoiced);
            parsed.setPaid(paid);

            if (date == null) {
                parsed.setStatus("ERROR");
                parsed.setMessage("Missing or unreadable date");
                rows.add(parsed);
                continue;
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                parsed.setStatus("ERROR");
                parsed.setMessage("Missing or invalid amount");
                rows.add(parsed);
                continue;
            }

            Long projectId = parseLong(rawProjectId);
            if (projectId == null) {
                parsed.setStatus("ERROR");
                parsed.setMessage("Invalid Project ID: '" + rawProjectId + "'");
                rows.add(parsed);
                continue;
            }
            parsed.setProjectId(projectId);

            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                parsed.setStatus("ERROR");
                parsed.setMessage("No project found with ID " + projectId);
                rows.add(parsed);
                continue;
            }
            parsed.setProjectName(projectOpt.get().getProjectName());

            boolean duplicate = paymentDateRepository.existsByProjectIdAndPaymentAmountAndPaymentDueDate(
                    projectId, amount, date);
            if (duplicate) {
                parsed.setStatus("DUPLICATE");
                parsed.setMessage("A payment for this project with the same amount and date already exists - will be skipped");
                rows.add(parsed);
                continue;
            }

            if (accountNo != null && !accountNo.isBlank() && sortCode != null && !sortCode.isBlank()) {
                projectBankDetailRepository
                        .findFirstByProjectIdAndAccountNumberAndSortCode(projectId, accountNo, sortCode)
                        .ifPresent(bd -> {
                            parsed.setMatchedBankDetailId(bd.getId());
                            parsed.setMatchedBankDetailLabel(bd.getPayee() + " - " + bd.getAccountNumber());
                        });
            }

            String bankNote = parsed.getMatchedBankDetailId() != null
                    ? ""
                    : " (no matching bank details on file - will be left unlinked)";
            parsed.setStatus("OK");
            parsed.setMessage("Ready to import as " + (paid ? "Paid" : "Unpaid") + bankNote);
            rows.add(parsed);
        }

        return rows;
    }

    private int findColumn(Row headerRow, String name) {
        for (Cell cell : headerRow) {
            String value = readString(cell);
            if (value != null && value.trim().equalsIgnoreCase(name.trim())) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private int findColumnAny(Row headerRow, String... names) {
        for (String name : names) {
            int col = findColumn(headerRow, name);
            if (col >= 0) {
                return col;
            }
        }
        return -1;
    }

    private Date firstOfMonth(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private boolean isRowBlank(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) {
                String value = readString(cell);
                if (value != null && !value.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String readString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                String s = cell.getStringCellValue();
                return s == null ? null : s.trim();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private Date readDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private BigDecimal readAmount(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
            }
            String s = readString(cell);
            if (s == null || s.isBlank()) {
                return null;
            }
            s = s.replaceAll("[^0-9.\\-]", "");
            return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normaliseSortCode(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("[^0-9]", "");
    }
}
