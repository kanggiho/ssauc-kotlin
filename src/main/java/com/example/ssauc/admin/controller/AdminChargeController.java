package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.service.AdminChargeService;
import com.example.ssauc.user.cash.entity.Charge;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/admin/charge")
@Controller
public class AdminChargeController {

    @Autowired
    private AdminChargeService adminChargeService;

    @GetMapping
    public String getChargeList(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "chargeId,asc") String sort,
                                @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams[1];

        Page<Charge> chargeList;

        if (keyword != null && !keyword.trim().isEmpty()) {
            chargeList = adminChargeService.searchCharges(keyword, page, sortField, sortDir);
        } else {
            chargeList = adminChargeService.getCharges(page, sortField, sortDir);
        }

        model.addAttribute("chargeList", chargeList);
        model.addAttribute("currentSort", sort);
        model.addAttribute("keyword", keyword);
        return "admin/admincharge";
    }

    @GetMapping("/export")
    public void exportCharges(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=charges.xlsx";
            response.setHeader(headerKey, headerValue);

            List<Charge> charges = adminChargeService.findAllCharges();

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Charges");

            // 헤더 스타일 생성
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"충전 번호", "회원 이름", "IMP_UID", "충전 타입", "금액", "상태", "상세", "영수증 URL", "충전일"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerCellStyle);
            }

            int rowCount = 1;
            for (Charge charge : charges) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(charge.getChargeId());
                row.createCell(1).setCellValue(charge.getUser().getUserName());
                row.createCell(2).setCellValue(charge.getImpUid());
                row.createCell(3).setCellValue(charge.getChargeType());
                row.createCell(4).setCellValue(charge.getAmount());
                row.createCell(5).setCellValue(charge.getStatus());
                row.createCell(6).setCellValue(charge.getDetails());
                row.createCell(7).setCellValue(charge.getReceiptUrl());
                row.createCell(8).setCellValue(charge.getCreatedAt().toString());
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
