package com.example.ssauc.admin.controller

import com.example.ssauc.admin.service.AdminChargeService
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException

@Slf4j
@RequestMapping("/admin/charge")
@Controller
class AdminChargeController {
    @Autowired
    private val adminChargeService: AdminChargeService? = null

    @GetMapping
    fun getChargeList(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "chargeId,asc") sort: String,
        @RequestParam(required = false) keyword: String?
    ): String {
        val sortParams = sort.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sortField = sortParams[0]
        val sortDir = sortParams[1]

        val chargeList =
            if (keyword != null && !keyword.trim { it <= ' ' }.isEmpty()) {
                adminChargeService!!.searchCharges(keyword, page, sortField, sortDir)
            } else {
                adminChargeService!!.getCharges(page, sortField, sortDir)
            }

        model.addAttribute("chargeList", chargeList)
        model.addAttribute("currentSort", sort)
        model.addAttribute("keyword", keyword)
        return "admin/admincharge"
    }

    @GetMapping("/export")
    fun exportCharges(response: HttpServletResponse) {
        try {
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val headerKey = "Content-Disposition"
            val headerValue = "attachment; filename=charges.xlsx"
            response.setHeader(headerKey, headerValue)

            val charges = adminChargeService!!.findAllCharges()

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Charges")

            // 헤더 스타일 생성
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.BLUE.getIndex()

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            val headerRow = sheet.createRow(0)
            val headers = arrayOf("충전 번호", "회원 이름", "IMP_UID", "충전 타입", "금액", "상태", "상세", "영수증 URL", "충전일")
            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
                headerRow.getCell(i).cellStyle = headerCellStyle
            }

            var rowCount = 1
            for (charge in charges) {
                val row = sheet.createRow(rowCount++)
                row.createCell(0).setCellValue(charge.chargeId!!.toDouble())
                row.createCell(1).setCellValue(charge.user!!.userName)
                row.createCell(2).setCellValue(charge.impUid)
                row.createCell(3).setCellValue(charge.chargeType)
                row.createCell(4).setCellValue(charge.amount!!.toDouble())
                row.createCell(5).setCellValue(charge.status)
                row.createCell(6).setCellValue(charge.details)
                row.createCell(7).setCellValue(charge.receiptUrl)
                row.createCell(8).setCellValue(charge.createdAt.toString())
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
