package com.example.ssauc.admin.controller;

import com.example.ssauc.admin.dto.ReplyDto;
import com.example.ssauc.admin.entity.Admin;
import com.example.ssauc.admin.entity.Reply;
import com.example.ssauc.admin.service.AdminQnaService;
import com.example.ssauc.user.contact.entity.Board;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

@RequestMapping("/admin/qna")
@Controller
public class AdminQnaController {

    @Autowired
    private AdminQnaService adminQnaService;


    @GetMapping
    public String getBoardList(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "boardId,asc") String sort) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams[1];
        Page<Board> boardList = adminQnaService.getBoards(page, sortField, sortDir);
        model.addAttribute("boardList", boardList);
        model.addAttribute("currentSort", sort); // 현재 정렬 상태 전달
        return "admin/adminqna";
    }

    @GetMapping("/detail")
    public String boardDetail(@RequestParam("boardId") Long boardId, Model model){
        // boardId를 이용해 신고 내역 정보를 조회
        Board board = adminQnaService.findBoardById(boardId);
        model.addAttribute("board", board);

        // reply 테이블에 해당 boardId가 존재하는지 확인
        Reply reply = adminQnaService.findReplyByBoardId(boardId);
        if(reply != null){
            System.out.println("널 아님");
            model.addAttribute("reply", reply);
            model.addAttribute("isReply", true);
        } else {
            System.out.println("널 임");
            model.addAttribute("isReply", false);
        }

        return "admin/adminqnadetail";
    }

    @PostMapping("/result")
    public ResponseEntity<String> processBoardResult(HttpSession session,
                                                     @RequestParam("answerTitle") String answerTitle,
                                                     @RequestParam("answerContent") String answerContent,
                                                     @RequestParam("boardId") long boardId) {
        // 전달받은 값 확인
        System.out.printf("선택된 처리 조치: %s %s %d%n", answerTitle, answerContent, boardId);

        if(session.getAttribute("admin")!=null){
            Admin tempAdmin = (Admin) session.getAttribute("admin");
            ReplyDto replyDto = new ReplyDto(boardId, answerTitle, answerContent, tempAdmin);

            if(adminQnaService.updateBoardInfo(replyDto)){
                return ResponseEntity.ok("등록완료");
            }else{
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("등록실패");
            }
        }else{
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그인오류");
        }

    }

    @GetMapping("/export")
    public void exportBoard(HttpServletResponse response) {
        try {
            // 엑셀 파일 다운로드 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=board.xlsx";
            response.setHeader(headerKey, headerValue);

            // Board 데이터 조회
            List<Board> boardList = adminQnaService.findAllBoards();

            // Apache POI를 사용하여 워크북 생성
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Board");

            // 헤더 스타일 생성
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex()); // 파란색

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // 헤더 행 생성 및 스타일 적용
            XSSFRow headerRow = sheet.createRow(0);
            String[] headers = {"게시글번호", "작성자 아이디", "문의 제목", "문의 내용", "문의 시간", "답변 상태"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerCellStyle);
            }

            // 데이터 행 추가
            int rowCount = 1;
            for (Board board : boardList) {
                XSSFRow row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(board.getBoardId());
                row.createCell(1).setCellValue(board.getUser().getUserName());
                row.createCell(2).setCellValue(board.getSubject());
                row.createCell(3).setCellValue(board.getMessage());
                row.createCell(4).setCellValue(board.getCreatedAt().toString());
                row.createCell(5).setCellValue(board.getStatus());
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
