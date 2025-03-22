package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.service.AdminWithdrawService;
import com.example.ssauc.user.cash.entity.Withdraw;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/admin/withdraw")
@Controller
public class AdminWithdrawController {

    @Autowired
    private AdminWithdrawService adminWithdrawService;

    @GetMapping
    public String getWithdrawList(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "withdrawId,asc") String sort,
                                  @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams[1];

        Page<Withdraw> withdrawList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            withdrawList = adminWithdrawService.searchWithdraws(keyword, page, sortField, sortDir);
        } else {
            withdrawList = adminWithdrawService.getWithdraws(page, sortField, sortDir);
        }

        model.addAttribute("withdrawList", withdrawList);
        model.addAttribute("currentSort", sort);
        model.addAttribute("keyword", keyword);
        return "admin/adminwithdraw";
    }

    @PostMapping("/process")
    public ResponseEntity<?> processWithdraw(@RequestBody Withdraw processRequest) {
        try {
            adminWithdrawService.processWithdraw(processRequest.getWithdrawId());
            return ResponseEntity.ok("처리 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("처리 실패: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportWithdraws(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=withdraws.xlsx";
            response.setHeader(headerKey, headerValue);

            List<Withdraw> withdraws = adminWithdrawService.findAllWithdraws();

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Withdraws");

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"환급 번호", "회원 이름", "금액", "수수료", "은행", "계좌", "신청일", "환급일"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerCellStyle);
            }

            int rowCount = 1;
            for (Withdraw withdraw : withdraws) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(withdraw.getWithdrawId());
                row.createCell(1).setCellValue(withdraw.getUser().getUserName());
                row.createCell(2).setCellValue(withdraw.getAmount());
                row.createCell(3).setCellValue(withdraw.getCommission());
                row.createCell(4).setCellValue(withdraw.getBank());
                row.createCell(5).setCellValue(withdraw.getAccount());
                row.createCell(6).setCellValue(withdraw.getRequestedAt() != null ? withdraw.getRequestedAt().toString() : "");
                row.createCell(7).setCellValue(withdraw.getWithdrawAt() != null ? withdraw.getWithdrawAt().toString() : "");
            }

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
        } catch (IOException e) {
            try {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "엑셀 출력 실패");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
