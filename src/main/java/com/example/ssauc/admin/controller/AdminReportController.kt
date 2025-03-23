package com.example.ssauc.admin.controller

import com.example.ssauc.admin.service.AdminReportService
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException

@RequestMapping("/admin/report")
@Controller
class AdminReportController {
    @Autowired
    private val adminReportService: AdminReportService? = null


    @GetMapping
    fun getReportList(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "reportId,asc") sort: String
    ): String {
        val sortParams = sort.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sortField = sortParams[0]
        val sortDir = sortParams[1]
        val reportList = adminReportService!!.getReports(page, sortField, sortDir)
        model.addAttribute("reportList", reportList)
        model.addAttribute("currentSort", sort) // 현재 정렬 상태 전달
        return "admin/adminreport"
    }

    @GetMapping("/detail")
    fun reportDetail(@RequestParam("reportId") reportId: Long?, model: Model): String {
        // reportId를 이용해 신고 내역 정보를 조회
        val report = adminReportService!!.findReportById(reportId)
        model.addAttribute("report", report)
        return "admin/adminreportdetail"
    }

    @PostMapping("/result")
    fun processReportResult(
        @RequestParam("action") action: String,
        @RequestParam("reportId") reportId: Long
    ): ResponseEntity<String> {
        // 전달받은 action 값 확인
        println("선택된 처리 조치: $action")


        return if (adminReportService!!.updateReportInfo(action, reportId)) {
            ResponseEntity.ok("등록완료")
        } else {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("등록실패")
        }
    }

    @GetMapping("/export")
    fun exportReport(response: HttpServletResponse) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val headerKey = "Content-Disposition"
            val headerValue = "attachment; filename=report.xlsx"
            response.setHeader(headerKey, headerValue)

            // Report 데이터 조회
            val reportList = adminReportService!!.findAllReports()

            // Apache POI를 사용하여 워크북 생성
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Report")

            // 헤더 스타일 생성
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.BLUE.getIndex() // 파란색

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            // 헤더 행 생성 및 스타일 적용
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("신고번호", "신고자 아이디", "피신고자 아이디", "신고 사유", "처리 상태", "신고 내용", "신고 시간", "처리 시간")
            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
                headerRow.getCell(i).cellStyle = headerCellStyle
            }

            // 데이터 행 추가
            var rowCount = 1
            for (report in reportList) {
                val row = sheet.createRow(rowCount++)
                row.createCell(0).setCellValue(report.reportId.toDouble())
                row.createCell(1).setCellValue(report.reporter.userName)
                row.createCell(2).setCellValue(report.reportedUser.userName)
                row.createCell(3).setCellValue(report.reportReason)
                row.createCell(4).setCellValue(report.status)
                row.createCell(5).setCellValue(report.details)
                row.createCell(6).setCellValue(report.reportDate.toString())
                val processedAtStr = if (report.processedAt != null) report.processedAt.toString() else ""
                row.createCell(7).setCellValue(processedAtStr)
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
}
