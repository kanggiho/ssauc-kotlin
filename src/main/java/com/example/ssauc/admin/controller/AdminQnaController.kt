package com.example.ssauc.admin.controller

import com.example.ssauc.admin.dto.ReplyDto
import com.example.ssauc.admin.entity.Admin
import com.example.ssauc.admin.service.AdminQnaService
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
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

@RequestMapping("/admin/qna")
@Controller
class AdminQnaController {
    @Autowired
    private val adminQnaService: AdminQnaService? = null


    @GetMapping
    fun getBoardList(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "boardId,asc") sort: String
    ): String {
        val sortParams = sort.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sortField = sortParams[0]
        val sortDir = sortParams[1]
        val boardList = adminQnaService!!.getBoards(page, sortField, sortDir)
        model.addAttribute("boardList", boardList)
        model.addAttribute("currentSort", sort) // 현재 정렬 상태 전달
        return "admin/adminqna"
    }

    @GetMapping("/detail")
    fun boardDetail(@RequestParam("boardId") boardId: Long?, model: Model): String {
        // boardId를 이용해 신고 내역 정보를 조회
        val board = adminQnaService!!.findBoardById(boardId)
        model.addAttribute("board", board)

        // reply 테이블에 해당 boardId가 존재하는지 확인
        val reply = adminQnaService.findReplyByBoardId(boardId)
        if (reply != null) {
            println("널 아님")
            model.addAttribute("reply", reply)
            model.addAttribute("isReply", true)
        } else {
            println("널 임")
            model.addAttribute("isReply", false)
        }

        return "admin/adminqnadetail"
    }

    @PostMapping("/result")
    fun processBoardResult(
        session: HttpSession,
        @RequestParam("answerTitle") answerTitle: String?,
        @RequestParam("answerContent") answerContent: String?,
        @RequestParam("boardId") boardId: Long
    ): ResponseEntity<String> {
        // 전달받은 값 확인
        System.out.printf("선택된 처리 조치: %s %s %d%n", answerTitle, answerContent, boardId)

        if (session.getAttribute("admin") != null) {
            val tempAdmin = session.getAttribute("admin") as Admin
            val replyDto = ReplyDto(boardId, answerTitle, answerContent, tempAdmin)

            return if (adminQnaService!!.updateBoardInfo(replyDto)) {
                ResponseEntity.ok("등록완료")
            } else {
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("등록실패")
            }
        } else {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("로그인오류")
        }
    }

    @GetMapping("/export")
    fun exportBoard(response: HttpServletResponse) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            val headerKey = "Content-Disposition"
            val headerValue = "attachment; filename=board.xlsx"
            response.setHeader(headerKey, headerValue)

            // Board 데이터 조회
            val boardList = adminQnaService!!.findAllBoards()

            // Apache POI를 사용하여 워크북 생성
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Board")

            // 헤더 스타일 생성
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.BLUE.getIndex() // 파란색

            val headerCellStyle = workbook.createCellStyle()
            headerCellStyle.setFont(headerFont)

            // 헤더 행 생성 및 스타일 적용
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("게시글번호", "작성자 아이디", "문의 제목", "문의 내용", "문의 시간", "답변 상태")
            for (i in headers.indices) {
                headerRow.createCell(i).setCellValue(headers[i])
                headerRow.getCell(i).cellStyle = headerCellStyle
            }

            // 데이터 행 추가
            var rowCount = 1
            for (board in boardList) {
                val row = sheet.createRow(rowCount++)
                row.createCell(0).setCellValue(board.boardId.toDouble())
                row.createCell(1).setCellValue(board.user.userName)
                row.createCell(2).setCellValue(board.subject)
                row.createCell(3).setCellValue(board.message)
                row.createCell(4).setCellValue(board.createdAt.toString())
                row.createCell(5).setCellValue(board.status)
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
