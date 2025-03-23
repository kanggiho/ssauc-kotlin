package com.example.ssauc.user.main.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal

@org.springframework.stereotype.Controller
@lombok.RequiredArgsConstructor
class RecentlyViewedController {
    private val recentlyViewedService: RecentlyViewedService? = null
    private val tokenExtractor: TokenExtractor? = null

    @GetMapping("/recently-viewed")
    fun getRecentlyViewed(
        request: jakarta.servlet.http.HttpServletRequest,
        model: org.springframework.ui.Model
    ): String {
        val user: Users = tokenExtractor.getUserFromToken(request)
        val recentViews: List<RecentlyViewed> = recentlyViewedService.getRecentlyViewedItems(user)
        model.addAttribute("recentViews", recentViews)
        return "recently_viewed_list" // templates/recently_viewed_list.html
    }
}
