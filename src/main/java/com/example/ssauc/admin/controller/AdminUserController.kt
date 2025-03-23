package com.example.ssauc.admin.controller

import com.example.ssauc.admin.dto.UsersStatusRequestDto
import com.example.ssauc.admin.service.AdminUserService
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.IOException

@Slf4j
@RequestMapping("/admin/user")
@Controller
class AdminUserController {
    @Autowired
    private val adminUserService: AdminUserService? = null


    @GetMapping
    fun getUsersList(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "userId,asc") sort: String,
        @RequestParam(required = false) keyword: String?
    ): String {
        val sortParams = sort.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sortField = sortParams[0]
        val sortDir = sortParams[1]

        val usersList = if (keyword != null && !keyword.trim { it <= ' ' }.isEmpty()) {
            adminUserService!!.searchUsersByName(keyword, page, sortField, sortDir)
        } else {
            adminUserService!!.getUsers(page, sortField, sortDir)
        }

        model.addAttribute("usersList", usersList)
        model.addAttribute("currentSort", sort)
        model.addAttribute("keyword", keyword) // 검색어 유지
        return "admin/adminuser"
    }


    @GetMapping("/detail")
    fun usersDetail(@RequestParam("userId") userId: Long?, model: Model): String {
        // userId를 이용해 신고 내역 정보를 조회
        val user = adminUserService!!.findUsersById(userId)
        model.addAttribute("user", user)
        return "admin/adminuserdetail"
    }

    @PostMapping("/result")
    fun changeProductStatus(@RequestBody request: UsersStatusRequestDto): ResponseEntity<*> {
        try {
            adminUserService!!.changeUsersStatus(request.userId, request.status)
            return ResponseEntity.ok("유저 상태가 변경되었습니다.")
        } catch (e: EntityNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경에 실패했습니다.")
        }
    }


    @GetMapping("/export")
    fun exportUsers(response: HttpServletResponse) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val headerKey = "Content-Disposition"
            val headerValue = "attachment; filename=users.xlsx"
            response.setHeader(headerKey, headerValue)

            // Users 데이터 조회
            val usersList = adminUserService!!.findAllUsers()

            // Apache POI를 사용하여 워크북 생성
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Users")

            // 헤더 스타일 생성
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.BLUE.getIndex() // 파란색

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            // 헤더 행 생성 및 스타일 적용
            val headerRow = sheet.createRow(0)
            val headers = arrayOf(
                "회원아이디", "회원이름", "이메일", "비밀번호", "연락처", "프로필사진", "상태",
                "평가지표", "경고횟수", "가입일", "수정된시간", "마지막로그인", "지역", "보유중인쏙머니"
            )
            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
                headerRow.getCell(i).cellStyle = headerCellStyle
            }

            // 데이터 행 추가
            var rowCount = 1
            for (user in usersList) {
                val row = sheet.createRow(rowCount++)
                row.createCell(0).setCellValue(user.userId!!.toDouble())
                row.createCell(1).setCellValue(user.userName)
                row.createCell(2).setCellValue(user.email)
                row.createCell(3).setCellValue(user.password)
                row.createCell(4).setCellValue(user.phone)
                row.createCell(5).setCellValue(user.profileImage)
                row.createCell(6).setCellValue(user.status)
                row.createCell(7).setCellValue(user.reputation!!)
                row.createCell(8).setCellValue(user.warningCount.toDouble())
                row.createCell(9).setCellValue(user.createdAt)
                row.createCell(10).setCellValue(user.updatedAt)
                row.createCell(11).setCellValue(user.lastLogin)
                row.createCell(12).setCellValue(user.location)
                row.createCell(13).setCellValue(user.cash!!.toDouble())
            }

            // 엑셀 파일을 응답 스트림에 작성
            val outputStream = response.outputStream
            workbook.write(outputStream)
            workbook.close()
            outputStream.close()
        } catch (e: IOException) {
            try {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "엑셀 출력 실패")
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // 🔥 매일 0시 0분
    fun blockUsersWithHighWarningCount() {
        AdminUserController.log.info("🔔 [스케줄러 실행] 경고 횟수가 3 이상인 유저 상태 BLOCKED로 변경 시작")

        val updatedUsers = adminUserService!!.blockUsersWithHighWarningCount()
        AdminUserController.log.info("✅ [완료] 총 {}명의 유저 상태가 BLOCKED로 변경되었습니다.", updatedUsers)
    }
}