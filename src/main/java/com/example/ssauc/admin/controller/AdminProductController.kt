package com.example.ssauc.admin.controller

import com.example.ssauc.admin.dto.ProductStatusRequestDto
import com.example.ssauc.admin.service.AdminProductService
import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.IOException

@RequestMapping("/admin/product")
@Controller
class AdminProductController {
    @Autowired
    private val adminProductService: AdminProductService? = null


    @GetMapping
    fun getProductsList(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "productId,asc") sort: String,
        @RequestParam(required = false) keyword: String?
    ): String {
        val sortParams = sort.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sortField = sortParams[0]
        val sortDir = sortParams[1]

        val productList =
            if (keyword != null && !keyword.trim { it <= ' ' }.isEmpty()) {
                adminProductService!!.searchProductsByName(keyword, page, sortField, sortDir)
            } else {
                adminProductService!!.getProducts(page, sortField, sortDir)
            }

        model.addAttribute("productList", productList)
        model.addAttribute("currentSort", sort)
        model.addAttribute("keyword", keyword) // 검색어 유지
        return "admin/adminproduct"
    }


    @GetMapping("/detail")
    fun productDetail(@RequestParam("productId") productId: Long?, model: Model): String {
        // productId를 이용해 신고 내역 정보를 조회
        val product = adminProductService!!.findProductById(productId)
        model.addAttribute("product", product)
        return "admin/adminproductdetail"
    }

    @PostMapping("/result")
    fun changeProductStatus(@RequestBody request: ProductStatusRequestDto): ResponseEntity<*> {
        try {
            adminProductService!!.changeProductStatus(request.productId, request.status)
            return ResponseEntity.ok("상품 상태가 변경되었습니다.")
        } catch (e: EntityNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품을 찾을 수 없습니다.")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경에 실패했습니다.")
        }
    }


    @GetMapping("/export")
    fun exportProduct(response: HttpServletResponse) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val headerKey = "Content-Disposition"
            val headerValue = "attachment; filename=product.xlsx"
            response.setHeader(headerKey, headerValue)

            // Product 데이터 조회
            val productList = adminProductService!!.findAllProducts()

            // Apache POI를 사용하여 워크북 생성
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Product")

            // 헤더 스타일 생성
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.BLUE.getIndex() // 파란색

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            // 헤더 행 생성 및 스타일 적용
            val headerRow = sheet.createRow(0)
            val headers = arrayOf(
                "상품아이디", "판매자이름", "카테고리", "상품이름", "상품설명", "즉시구매가",
                "현재입찰가", "경매시작가", "이미지주소", "판매상태", "등록시간", "마감시간", "조회수",
                "거래방식", "최소입찰단위", "좋아요수", "입찰수"
            )
            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
                headerRow.getCell(i).setCellStyle(headerCellStyle)
            }

            // 데이터 행 추가
            var rowCount = 1
            for (product in productList) {
                val row = sheet.createRow(rowCount++)
                row.createCell(0).setCellValue(product.productId!!.toDouble())
                row.createCell(1).setCellValue(product.seller!!.userName)
                row.createCell(2).setCellValue(product.getCategory().getName())
                row.createCell(3).setCellValue(product.name)
                row.createCell(4).setCellValue(product.description)
                row.createCell(5).setCellValue(product.price!!.toDouble())
                row.createCell(6).setCellValue(product.tempPrice!!.toDouble())
                row.createCell(7).setCellValue(product.startPrice!!.toDouble())
                row.createCell(8).setCellValue(product.imageUrl)
                row.createCell(9).setCellValue(product.status)
                row.createCell(10).setCellValue(product.createdAt)
                row.createCell(11).setCellValue(product.endAt)
                row.createCell(12).setCellValue(product.viewCount!!.toDouble())
                row.createCell(13).setCellValue(product.dealType.toDouble())
                row.createCell(14).setCellValue(product.minIncrement.toDouble())
                row.createCell(15).setCellValue(product.likeCount.toDouble())
                row.createCell(16).setCellValue(product.bidCount.toDouble())
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
