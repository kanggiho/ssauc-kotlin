package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.dto.ProductStatusRequestDto;
import com.example.ssauc.admin.service.AdminProductService;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/admin/product")
@Controller
public class AdminProductController {

    @Autowired
    private AdminProductService adminProductService;


    @GetMapping
    public String getProductsList(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "productId,asc") String sort,
                                  @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams[1];

        Page<Product> productList;

        if (keyword != null && !keyword.trim().isEmpty()) {
            productList = adminProductService.searchProductsByName(keyword, page, sortField, sortDir);
        } else {
            productList = adminProductService.getProducts(page, sortField, sortDir);
        }

        model.addAttribute("productList", productList);
        model.addAttribute("currentSort", sort);
        model.addAttribute("keyword", keyword); // 검색어 유지
        return "admin/adminproduct";
    }


    @GetMapping("/detail")
    public String productDetail(@RequestParam("productId") Long productId, Model model){
        // productId를 이용해 신고 내역 정보를 조회
        Product product = adminProductService.findProductById(productId);
        model.addAttribute("product", product);
        return "admin/adminproductdetail";
    }

    @PostMapping("/result")
    public ResponseEntity<?> changeProductStatus(@RequestBody ProductStatusRequestDto request) {
        try {
            adminProductService.changeProductStatus(request.getProductId(), request.getStatus());
            return ResponseEntity.ok("상품 상태가 변경되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품을 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경에 실패했습니다.");
        }
    }


    @GetMapping("/export")
    public void exportProduct(HttpServletResponse response) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=product.xlsx";
            response.setHeader(headerKey, headerValue);

            // Product 데이터 조회
            List<Product> productList = adminProductService.findAllProducts();

            // Apache POI를 사용하여 워크북 생성
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Product");

            // 헤더 스타일 생성
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex()); // 파란색

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // 헤더 행 생성 및 스타일 적용
            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"상품아이디", "판매자이름", "카테고리", "상품이름", "상품설명", "즉시구매가",
                    "현재입찰가", "경매시작가", "이미지주소", "판매상태", "등록시간", "마감시간", "조회수",
                    "거래방식", "최소입찰단위", "좋아요수", "입찰수"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerCellStyle);
            }

            // 데이터 행 추가
            int rowCount = 1;
            for (Product product : productList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(product.getProductId());
                row.createCell(1).setCellValue(product.getSeller().getUserName());
                row.createCell(2).setCellValue(product.getCategory().getName());
                row.createCell(3).setCellValue(product.getName());
                row.createCell(4).setCellValue(product.getDescription());
                row.createCell(5).setCellValue(product.getPrice());
                row.createCell(6).setCellValue(product.getTempPrice());
                row.createCell(7).setCellValue(product.getStartPrice());
                row.createCell(8).setCellValue(product.getImageUrl());
                row.createCell(9).setCellValue(product.getStatus());
                row.createCell(10).setCellValue(product.getCreatedAt());
                row.createCell(11).setCellValue(product.getEndAt());
                row.createCell(12).setCellValue(product.getViewCount());
                row.createCell(13).setCellValue(product.getDealType());
                row.createCell(14).setCellValue(product.getMinIncrement());
                row.createCell(15).setCellValue(product.getLikeCount());
                row.createCell(16).setCellValue(product.getBidCount());
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
