package com.example.ssauc.user.mypage.controller

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.example.ssauc.user.history.service.HistoryService
import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.mypage.dto.EvaluationDto
import com.example.ssauc.user.mypage.dto.ResponseUserInfoDto
import com.example.ssauc.user.mypage.dto.UserUpdateDto
import com.example.ssauc.user.mypage.service.MypageService
import com.example.ssauc.user.mypage.service.UserProfileService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage") // "/mypage" 경로로 들어오는 요청을 처리
class MypageController {
    private val mypageService: MypageService? = null
    private val historyService: HistoryService? = null
    private val tokenExtractor: TokenExtractor? = null
    private val userProfileService: UserProfileService? = null
    private val amazonS3: AmazonS3? = null

    @Value("\${aws.s3.bucket}")
    private val bucketName: String? = null

    @GetMapping // GET 요청을 받아서 mypage.html을 반환
    fun mypage(request: HttpServletRequest, model: Model): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = mypageService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        // 입찰중
        val pageSize = 10
        val pageable: Pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "product.endAt"))
        val biddingPage = historyService!!.getBiddingHistoryPage(latestUser, pageable)
        model.addAttribute("bidList", biddingPage.content)

        // 판매중
        val sellPageable: Pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val sellPage = historyService.getOngoingSellHistoryPage(latestUser, sellPageable)
        model.addAttribute("sellList", sellPage.content)

        return "mypage/mypage"
    }

    // 프로필 수정 페이지 (개별 주소 필드 분리)
    @GetMapping("/profile-update")
    fun showProfileUpdate(request: HttpServletRequest, model: Model): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val currentUser = userProfileService!!.getCurrentUser(user.email)
        model.addAttribute("user", currentUser)
        // location 필드를 공백 기준으로 분리 (예: "우편번호 기본주소 상세주소")
        val location = currentUser!!.location
        if (location == null || location.isEmpty()) {
            // location이 없을 때 기본값
            model.addAttribute("zipcode", "")
            model.addAttribute("address", "")
            model.addAttribute("addressDetail", "")
        } else {
            val parts = location.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val zipcode = if (parts.size >= 1) parts[0] else ""
            var address = ""
            var addressDetail = ""

            // 기본 주소: index 1,2,3까지
            if (parts.size >= 4) {
                address = parts[1] + " " + parts[2] + " " + parts[3]
            } else if (parts.size == 2) {
                // 우편번호 + 한 단어만 있을 경우
                address = parts[1]
            }
            // 상세 주소: index 4부터 나머지
            if (parts.size > 4) {
                val sb = StringBuilder()
                for (i in 4 until parts.size) {
                    sb.append(parts[i]).append(" ")
                }
                addressDetail = sb.toString().trim { it <= ' ' }
            }

            model.addAttribute("zipcode", zipcode)
            model.addAttribute("address", address)
            model.addAttribute("addressDetail", addressDetail)
        }

        return "mypage/profile-update"
    }

    // 2) 프로필 이미지 업로드 (S3)
    @PostMapping("/uploadImage")
    @ResponseBody
    fun uploadProfileImage(@RequestParam("file") file: MultipartFile): ResponseEntity<*> {
        // 파일 크기 제한 (3MB)
        if (file.size > 3 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("파일 크기는 3MB를 초과할 수 없습니다.")
        }
        // 이미지 여부 간단 체크
        if (!file.contentType!!.startsWith("image/")) {
            return ResponseEntity.badRequest().body("이미지 파일만 업로드 가능합니다.")
        }

        try {
            // 고유 파일명 (timestamp_파일명)
            val fileName = System.currentTimeMillis().toString() + "_" + file.originalFilename
            val metadata = ObjectMetadata()
            metadata.contentLength = file.size
            metadata.contentType = file.contentType

            amazonS3!!.putObject(bucketName, fileName, file.inputStream, metadata)

            val fileUrl = amazonS3.getUrl(bucketName, fileName).toString()
            val result: MutableMap<String, String> = HashMap()
            result["url"] = fileUrl
            return ResponseEntity.ok<Map<String, String>>(result)
        } catch (e: IOException) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업로드 실패: " + e.message)
        }
    }

    // 프로필 업데이트 처리 (AJAX JSON POST)
    @PostMapping("/profile-update")
    @ResponseBody
    fun updateProfile(@RequestBody dto: UserUpdateDto, request: HttpServletRequest): ResponseEntity<*> {
        val user = tokenExtractor!!.getUserFromToken(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("로그인이 필요합니다.")
        try {
            val currentUser = userProfileService!!.getCurrentUser(user.email)
            userProfileService.updateUserProfile(currentUser!!, dto)
            return ResponseEntity.ok("프로필이 성공적으로 수정되었습니다.")
        } catch (ex: RuntimeException) {
            return ResponseEntity.badRequest().body(ex.message)
        }
    }

    // 리뷰 현황 (작성 가능, 받은, 보낸)
    @GetMapping("/evaluation")
    fun evaluatePage(
        @RequestParam(value = "filter", required = false, defaultValue = "pending") filter: String,
        @RequestParam(value = "page", required = false, defaultValue = "1") page: Int,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = mypageService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        val pageSize = 10
        if ("received" == filter) {
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val receivedPageData = mypageService.getReceivedReviews(
                latestUser!!, pageable
            )
            model.addAttribute("reviewList", receivedPageData!!.content)
            model.addAttribute("totalPages", receivedPageData.totalPages)
        } else if ("written" == filter) {
            // 판매 마감 리스트: 판매중 상태에서 (경매 마감 시간 + 30분) < 현재 시간인 상품
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val writtenPageData = mypageService.getWrittenReviews(
                latestUser!!, pageable
            )
            model.addAttribute("reviewList", writtenPageData!!.content)
            model.addAttribute("totalPages", writtenPageData.totalPages)
        } else if ("pending" == filter) {
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"))
            val pendingPageData = mypageService.getPendingReviews(
                latestUser!!, pageable
            )
            model.addAttribute("reviewList", pendingPageData!!.content)
            model.addAttribute("totalPages", pendingPageData.totalPages)
        }
        model.addAttribute("currentPage", page)
        model.addAttribute("filter", filter)
        return "mypage/evaluation"
    }

    // 리뷰 작성 페이지
    @GetMapping("/evaluate")
    fun evaluationPage(
        @RequestParam("orderId") orderId: Long,
        @RequestParam("productId") productId: Long?,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = mypageService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)
        // 주문 정보를 기반으로 평가 데이터 준비 (상품명, 상대방 이름, 거래 유형 등)
        val evaluationDto = mypageService.getEvaluationData(orderId, latestUser!!)
        model.addAttribute("evaluationDto", evaluationDto)
        model.addAttribute("productName", evaluationDto.productName)
        model.addAttribute("otherUserName", evaluationDto.otherUserName)

        return "mypage/evaluate"
    }

    // 리뷰 제출 처리 - JSON POST 요청을 받음
    @PostMapping("/evaluate/submit")
    @ResponseBody
    fun submitEvaluation(@RequestBody evaluationDto: EvaluationDto, request: HttpServletRequest): ResponseEntity<*> {
        val user = tokenExtractor!!.getUserFromToken(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("로그인이 필요합니다.")

        val latestUser = mypageService!!.getCurrentUser(user.email)
        try {
            mypageService.submitEvaluation(evaluationDto, latestUser!!)
            return ResponseEntity.ok("평가가 완료되었습니다.")
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body("평가 제출에 실패했습니다: " + e.message)
        }
    }

    // 리뷰 상세 페이지 - reviewId를 통해 리뷰 상세 정보를 조회
    @GetMapping("/evaluated")
    fun evaluatedPage(
        @RequestParam("reviewId") reviewId: Long,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = mypageService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        val reviewDto = mypageService.getReviewById(reviewId, latestUser!!.userId)
        model.addAttribute("review", reviewDto)

        // reviewType 결정: 현재 사용자가 리뷰 작성자이면 "written", 아니면 "received"
        var reviewType = ""
        reviewType = if (latestUser.userName == reviewDto.reviewerName) {
            "written"
        } else {
            "received"
        }
        model.addAttribute("reviewType", reviewType)

        return "mypage/evaluated"
    }


    // 회원 정보 페이지
    @GetMapping("/info")
    fun memberInfo(request: HttpServletRequest, model: Model): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val userInfo = mypageService!!.getUserInfo(user.email)
        model.addAttribute("user", userInfo)

        val reputationData = mypageService.getReputationHistory(userInfo)
        model.addAttribute("reputationData", reputationData)

        return "mypage/info"
    }

    // 다른 회원 정보 모달
    @GetMapping("/info/json")
    @ResponseBody
    fun getUserInfoJson(@RequestParam userName: String?): ResponseUserInfoDto? {
        return mypageService!!.getUserInfoJson(userName)
    }

    // 회원 탈퇴 페이지 진입
    @GetMapping("/withdraw")
    fun withdrawPage(request: HttpServletRequest, model: Model): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        model.addAttribute("user", user)
        return "mypage/withdraw"
    }

    // 회원 탈퇴 처리 (토큰 쿠키 삭제)
    @PostMapping("/withdraw")
    @ResponseBody
    fun withdrawUser(
        @RequestBody requestBody: Map<String?, String?>,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val user = tokenExtractor!!.getUserFromToken(request)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("로그인이 필요합니다.")
        val inputPassword = requestBody["password"]
        if (inputPassword == null || inputPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("비밀번호를 입력해주세요.")
        }
        try {
            // 탈퇴 로직 (inactive로 변경)
            userProfileService!!.withdrawUser(user, inputPassword)

            // 토큰 쿠키 삭제 (로그아웃)
            val accessCookie = Cookie("jwt_access", null)
            accessCookie.maxAge = 0
            accessCookie.path = "/"
            response.addCookie(accessCookie)

            val refreshCookie = Cookie("jwt_refresh", null)
            refreshCookie.maxAge = 0
            refreshCookie.path = "/"
            response.addCookie(refreshCookie)

            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.")
        } catch (ex: RuntimeException) {
            return ResponseEntity.badRequest().body(ex.message)
        }
    }
}
