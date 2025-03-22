package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.service.AdminProductReportService;
import com.example.ssauc.admin.service.AdminReportService;
import com.example.ssauc.user.bid.entity.ProductReport;
import com.example.ssauc.user.chat.entity.Report;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@RequestMapping("/admin/product-report")
@Controller
public class AdminProductReportController {

    @Autowired
    private AdminProductReportService adminProductReportService;


    @GetMapping
    public String getProductReportList(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "reportId,asc") String sort) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams[1];
        Page<ProductReport> productReportList = adminProductReportService.getReports(page, sortField, sortDir);
        model.addAttribute("productReportList", productReportList);
        model.addAttribute("currentSort", sort); // 현재 정렬 상태 전달
        return "admin/adminproductreport";
    }

    @GetMapping("/detail")
    public String productReportDetail(@RequestParam("reportId") Long reportId, Model model){
        // reportId를 이용해 신고 내역 정보를 조회
        ProductReport productReport = adminProductReportService.findProductReportById(reportId);
        model.addAttribute("productReport", productReport);
        return "admin/adminproductreportdetail";
    }

    @PostMapping("/result")
    public ResponseEntity<String> processProductReportResult(@RequestParam("action") String action, @RequestParam("reportId") long reportId) {
        // 전달받은 action 값 확인
        System.out.println("선택된 처리 조치: " + action);


        if(adminProductReportService.updateProductReportInfo(action, reportId)){
            return ResponseEntity.ok("등록완료");
        }else{
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("등록실패");

        }
    }

    @GetMapping("/export")
    public void exportProductReport(HttpServletResponse response) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=product_report.xlsx";
            response.setHeader(headerKey, headerValue);

            // Report 데이터 조회
            List<ProductReport> productReportList = adminProductReportService.findAllProductReports();

            // Apache POI를 사용하여 워크북 생성
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("ProductReport");

            // 헤더 스타일 생성
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex()); // 파란색

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // 헤더 행 생성 및 스타일 적용
            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"신고번호","상품 아이디", "신고자 아이디", "피신고자 아이디", "신고 사유", "처리 상태", "신고 내용", "신고 시간", "처리 시간"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerCellStyle);
            }

            // 데이터 행 추가
            int rowCount = 1;
            for (ProductReport productReport : productReportList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(productReport.getReportId());
                row.createCell(1).setCellValue(productReport.getProduct().getProductId());
                row.createCell(2).setCellValue(productReport.getReporter().getUserName());
                row.createCell(3).setCellValue(productReport.getReportedUser().getUserName());
                row.createCell(4).setCellValue(productReport.getReportReason());
                row.createCell(5).setCellValue(productReport.getStatus());
                row.createCell(6).setCellValue(productReport.getDetails());
                row.createCell(7).setCellValue(productReport.getReportDate().toString());
                String processedAtStr = productReport.getProcessedAt() != null ? productReport.getProcessedAt().toString() : "";
                row.createCell(8).setCellValue(processedAtStr);
            }

            // 엑셀 파일을 응답 스트림에 작성
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
