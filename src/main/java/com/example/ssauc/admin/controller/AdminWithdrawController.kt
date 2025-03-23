package com.example.ssauc.admin.controller

import com.example.ssauc.admin.service.AdminWithdrawService
import com.example.ssauc.user.cash.entity.Withdraw
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.IOException

@Slf4j
@RequestMapping("/admin/withdraw")
@Controller
class AdminWithdrawController {
    @Autowired
    private val adminWithdrawService: AdminWithdrawService? = null

    @GetMapping
    fun getWithdrawList(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "withdrawId,asc") sort: String,
        @RequestParam(required = false) keyword: String?
    ): String {
        val sortParams = sort.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sortField = sortParams[0]
        val sortDir = sortParams[1]
        val withdrawList =
            if (keyword != null && !keyword.trim { it <= ' ' }.isEmpty()) {
                adminWithdrawService!!.searchWithdraws(keyword, page, sortField, sortDir)
            } else {
                adminWithdrawService!!.getWithdraws(page, sortField, sortDir)
            }

        model.addAttribute("withdrawList", withdrawList)
        model.addAttribute("currentSort", sort)
        model.addAttribute("keyword", keyword)
        return "admin/adminwithdraw"
    }

    @PostMapping("/process")
    fun processWithdraw(@RequestBody processRequest: Withdraw): ResponseEntity<*> {
        try {
            adminWithdrawService!!.processWithdraw(processRequest.withdrawId)
            return ResponseEntity.ok("처리 완료되었습니다.")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("처리 실패: " + e.message)
        }
    }

    @GetMapping("/export")
    fun exportWithdraws(response: HttpServletResponse) {
        try {
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val headerKey = "Content-Disposition"
            val headerValue = "attachment; filename=withdraws.xlsx"
            response.setHeader(headerKey, headerValue)

            val withdraws = adminWithdrawService!!.findAllWithdraws()

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Withdraws")

            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.BLUE.getIndex()

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            val headerRow = sheet.createRow(0)
            val headers = arrayOf("환급 번호", "회원 이름", "금액", "수수료", "은행", "계좌", "신청일", "환급일")
            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
                headerRow.getCell(i).cellStyle = headerCellStyle
            }

            var rowCount = 1
            for (withdraw in withdraws) {
                val row = sheet.createRow(rowCount++)
                row.createCell(0).setCellValue(withdraw.withdrawId!!.toDouble())
                row.createCell(1).setCellValue(withdraw.user!!.userName)
                row.createCell(2).setCellValue(withdraw.amount!!.toDouble())
                row.createCell(3).setCellValue(withdraw.commission!!.toDouble())
                row.createCell(4).setCellValue(withdraw.bank)
                row.createCell(5).setCellValue(withdraw.account)
                row.createCell(6)
                    .setCellValue(if (withdraw.requestedAt != null) withdraw.requestedAt.toString() else "")
                row.createCell(7).setCellValue(if (withdraw.withdrawAt != null) withdraw.withdrawAt.toString() else "")
            }

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
