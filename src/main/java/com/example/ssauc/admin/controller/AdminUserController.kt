package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.dto.ProductStatusRequestDto;
import com.example.ssauc.admin.dto.UsersStatusRequestDto;
import com.example.ssauc.admin.service.AdminProductService;
import com.example.ssauc.admin.service.AdminUserService;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/admin/user")
@Controller
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;


    @GetMapping
    public String getUsersList(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "userId,asc") String sort,
                               @RequestParam(required = false) String keyword) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams[1];

        Page<Users> usersList;

        if (keyword != null && !keyword.trim().isEmpty()) {
            usersList = adminUserService.searchUsersByName(keyword, page, sortField, sortDir);
        } else {
            usersList = adminUserService.getUsers(page, sortField, sortDir);
        }

        model.addAttribute("usersList", usersList);
        model.addAttribute("currentSort", sort);
        model.addAttribute("keyword", keyword); // 검색어 유지
        return "admin/adminuser";
    }


    @GetMapping("/detail")
    public String usersDetail(@RequestParam("userId") Long userId, Model model) {
        // userId를 이용해 신고 내역 정보를 조회
        Users user = adminUserService.findUsersById(userId);
        model.addAttribute("user", user);
        return "admin/adminuserdetail";
    }

    @PostMapping("/result")
    public ResponseEntity<?> changeProductStatus(@RequestBody UsersStatusRequestDto request) {
        try {
            adminUserService.changeUsersStatus(request.getUserId(), request.getStatus());
            return ResponseEntity.ok("유저 상태가 변경되었습니다.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경에 실패했습니다.");
        }
    }


    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=users.xlsx";
            response.setHeader(headerKey, headerValue);

            // Users 데이터 조회
            List<Users> usersList = adminUserService.findAllUsers();

            // Apache POI를 사용하여 워크북 생성
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Users");

            // 헤더 스타일 생성
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex()); // 파란색

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // 헤더 행 생성 및 스타일 적용
            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"회원아이디", "회원이름", "이메일", "비밀번호", "연락처", "프로필사진", "상태",
                    "평가지표", "경고횟수", "가입일", "수정된시간", "마지막로그인", "지역", "보유중인쏙머니"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerCellStyle);
            }

            // 데이터 행 추가
            int rowCount = 1;
            for (Users user : usersList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(user.getUserId());
                row.createCell(1).setCellValue(user.getUserName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getPassword());
                row.createCell(4).setCellValue(user.getPhone());
                row.createCell(5).setCellValue(user.getProfileImage());
                row.createCell(6).setCellValue(user.getStatus());
                row.createCell(7).setCellValue(user.getReputation());
                row.createCell(8).setCellValue(user.getWarningCount());
                row.createCell(9).setCellValue(user.getCreatedAt());
                row.createCell(10).setCellValue(user.getUpdatedAt());
                row.createCell(11).setCellValue(user.getLastLogin());
                row.createCell(12).setCellValue(user.getLocation());
                row.createCell(13).setCellValue(user.getCash());

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

    @Scheduled(cron = "0 0 0 * * *")  // 🔥 매일 0시 0분
    public void blockUsersWithHighWarningCount() {
        log.info("🔔 [스케줄러 실행] 경고 횟수가 3 이상인 유저 상태 BLOCKED로 변경 시작");

        int updatedUsers = adminUserService.blockUsersWithHighWarningCount();
        log.info("✅ [완료] 총 {}명의 유저 상태가 BLOCKED로 변경되었습니다.", updatedUsers);
    }


}