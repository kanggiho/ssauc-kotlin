package com.example.ssauc.user.history.controller

import com.example.ssauc.user.bid.dto.CarouselImage
import com.example.ssauc.user.history.service.HistoryService
import com.example.ssauc.user.login.util.TokenExtractor
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@Controller
@RequestMapping("history")
@RequiredArgsConstructor
class HistoryController {
    private val historyService: HistoryService? = null

    private val tokenExtractor: TokenExtractor? = null

    @Value("\${smarttracker.apiKey}")
    private val smartTrackerApiKey: String? = null

    // ===================== 차단 관리 =====================
    // 차단 내역 페이지: 로그인한 사용자의 차단 내역 조회
    @GetMapping("/ban")
    fun banPage(
        @RequestParam(defaultValue = "1") page: Int,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        // 페이지 번호는 0부터 시작하므로 (page - 1)로 변환
        val pageable: Pageable = PageRequest.of(page - 1, 10)
        val banPage = historyService.getBanListForUser(latestUser.userId, pageable)
        model.addAttribute("banList", banPage.content)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", banPage.totalPages)
        return "history/ban"
    }

    // 차단 해제 요청 처리
    @PostMapping("/ban/unban")
    fun unbanUser(
        @RequestParam("banId") banId: Long,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        historyService.unbanUser(banId, latestUser.userId)
        return "redirect:/history/ban"
    }

    // ===================== 신고 내역 =====================
    // 신고 내역 리스트
    @GetMapping("/report")
    fun reportPage(
        @RequestParam(value = "filter", required = false, defaultValue = "product") filter: String,
        @RequestParam(value = "page", required = false, defaultValue = "1") page: Int,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        val pageSize = 10
        if ("product" == filter) {
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "reportDate"))
            val productReports = historyService.getProductReportHistoryPage(latestUser, pageable)
            model.addAttribute("list", productReports.content)
            model.addAttribute("totalPages", productReports.totalPages)
        } else if ("user" == filter) {
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "reportDate"))
            val userReports = historyService.getUserReportHistoryPage(latestUser, pageable)
            model.addAttribute("list", userReports.content)
            model.addAttribute("totalPages", userReports.totalPages)
        }
        model.addAttribute("filter", filter)
        model.addAttribute("currentPage", page)
        return "history/report"
    }

    // 신고 상세 내역
    @GetMapping("/reported")
    fun reportedPage(
        @RequestParam("filter") filter: String,  // 신고 유형
        @RequestParam("id") id: Long,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        // 신고 상세 내역 조회 (신고 유형에 따라 다르게 조회)
        val reportDetail = historyService.getReportDetail(filter, id)
        model.addAttribute("reportDetail", reportDetail)

        return "history/reported"
    }

    // ===================== 판매 내역 =====================
    // 판매 현황 리스트 (판매중, 만료, 완료)
    @GetMapping("/sell")
    fun sellPage(
        @RequestParam(value = "filter", required = false, defaultValue = "ongoing") filter: String,
        @RequestParam(value = "page", required = false, defaultValue = "1") page: Int,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        val pageSize = 10
        if ("ongoing" == filter) {
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val ongoingPageData = historyService.getOngoingSellHistoryPage(latestUser, pageable)
            model.addAttribute("list", ongoingPageData.content)
            model.addAttribute("totalPages", ongoingPageData.totalPages)
        } else if ("ended" == filter) {
            // 판매 마감 리스트: 판매중 상태에서 (경매 마감 시간 + 30분) < 현재 시간인 상품
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val endedPageData = historyService.getEndedSellHistoryPage(latestUser, pageable)
            model.addAttribute("list", endedPageData.content)
            model.addAttribute("totalPages", endedPageData.totalPages)
        } else if ("completed" == filter) {
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
            val completedPageData = historyService.getCompletedSellHistoryPage(latestUser, pageable)
            model.addAttribute("list", completedPageData.content)
            model.addAttribute("totalPages", completedPageData.totalPages)
        }
        model.addAttribute("currentPage", page)
        model.addAttribute("filter", filter)
        return "history/sell"
    }

    // 판매 내역 상세 페이지
    @GetMapping("/sold")
    fun soldPage(
        @RequestParam("id") productId: Long,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        // 상품 및 주문 상세 정보 조회 (SoldDetailDto를 이용)
        val soldDetail = historyService.getSoldDetailByProductId(productId)
        model.addAttribute("soldDetail", soldDetail)

        // imageUrl을 쉼표로 분리하여 CarouselImage 리스트 생성 (BidService와 유사한 로직)
        val imageUrlStr = soldDetail.imageUrl // SoldDetailDto의 imageUrl 필드 (쉼표로 구분된 문자열)
        val urls = imageUrlStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val carouselImages: MutableList<CarouselImage> = ArrayList()
        var i = 1
        for (url in urls) {
            val image = CarouselImage()
            image.url = url.trim { it <= ' ' }
            image.alt = "Slide $i"
            i++
            carouselImages.add(image)
        }
        model.addAttribute("carouselImages", carouselImages)

        return "history/sold"
    }

    // 운송장 번호 등록
    @PostMapping("/sold/update-tracking")
    @ResponseBody
    fun updateDeliveryStatus(
        @RequestParam("orderId") orderId: Long,
        @RequestParam("newTracking") newTracking: String?
    ): Map<String, Any> {
        val updated = historyService!!.updateDeliveryStatus(orderId, newTracking)
        val result: MutableMap<String, Any> = HashMap()
        result["updated"] = updated
        return result
    }

    // ===================== 구매 내역 =====================
    // 구매 내역 리스트 (완료)
    @GetMapping("/buy")
    fun buyPage(
        @RequestParam(value = "filter", required = false, defaultValue = "bidding") filter: String,
        @RequestParam(value = "page", required = false, defaultValue = "1") page: Int,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        val pageSize = 10

        if ("complete" == filter) {
            // 구매 완료 필터 (default)
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"))
            val purchasePage = historyService.getPurchaseHistoryPage(latestUser, pageable)
            model.addAttribute("list", purchasePage.content)
            model.addAttribute("totalPages", purchasePage.totalPages)
        } else {
            // 입찰중 필터: Bid 테이블 기준
            val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "product.endAt"))
            val biddingPage = historyService.getBiddingHistoryPage(latestUser, pageable)
            model.addAttribute("list", biddingPage.content)
            model.addAttribute("totalPages", biddingPage.totalPages)
        }
        model.addAttribute("currentPage", page)
        model.addAttribute("filter", filter)
        return "history/buy"
    }

    // 구매 내역 상세 페이지
    @GetMapping("/bought")
    fun boughtPage(
        @RequestParam("id") productId: Long,
        request: HttpServletRequest, model: Model
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)

        // 구매 내역 상세 조회 (구매자 기준)
        val boughtDetail = historyService.getBoughtDetailByProductId(productId, latestUser)
        model.addAttribute("boughtDetail", boughtDetail)

        // 이미지 Carousel 생성 (BidService와 유사한 로직)
        val imageUrlStr = boughtDetail.imageUrl // 쉼표로 구분된 문자열
        val urls = imageUrlStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val carouselImages: MutableList<CarouselImage> = ArrayList()
        var i = 1
        for (url in urls) {
            val image = CarouselImage()
            image.url = url.trim { it <= ' ' }
            image.alt = "Slide $i"
            i++
            carouselImages.add(image)
        }
        model.addAttribute("carouselImages", carouselImages)
        model.addAttribute("apiKey", smartTrackerApiKey)
        return "history/bought"
    }

    // 배송 조회 프록시 (클라이언트에서 호출)
    @PostMapping("/tracking-proxy")
    @ResponseBody
    fun trackingProxy(
        @RequestParam("t_code") t_code: String?,
        @RequestParam("t_invoice") t_invoice: String?
    ): String? {
        // (사용할 템플릿의 코드(1: Cyan, 2: Pink, 3: Gray, 4: Tropical, 5: Sky)를 URL 경로에 추가)
        val url = "https://info.sweettracker.co.kr/tracking/5"
        // 요청 파라미터 구성 (API KEY는 서버에서 주입)
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("t_key", smartTrackerApiKey)
        formData.add("t_code", t_code)
        formData.add("t_invoice", t_invoice)

        val restTemplate = RestTemplate()
        val response = restTemplate.postForEntity(url, formData, String::class.java)
        var body = response.body

        // <head> 태그가 있다면 <base> 태그 삽입
        if (body != null && body.contains("<head>")) {
            body = body.replace("<head>", "<head><base href=\"https://info.sweettracker.co.kr/\">")
        }
        return body
    }

    // 거래 완료 요청 처리
    @PostMapping("/bought/complete")
    @ResponseBody
    fun completeOrder(
        @RequestParam("orderId") orderId: Long,
        request: HttpServletRequest
    ): String {
        val user = tokenExtractor!!.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser = historyService!!.getCurrentUser(user.email)
        historyService.completeOrder(orderId, latestUser)
        return "완료"
    }
}
