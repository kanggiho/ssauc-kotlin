package com.example.ssauc.user.main.controller;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.main.service.RecentlyViewedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RecentlyViewedController {

    private final RecentlyViewedService recentlyViewedService;
    private final TokenExtractor tokenExtractor;

    @GetMapping("/recently-viewed")
    public String getRecentlyViewed(HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        List<RecentlyViewed> recentViews = recentlyViewedService.getRecentlyViewedItems(user);
        model.addAttribute("recentViews", recentViews);
        return "recently_viewed_list"; // templates/recently_viewed_list.html
    }
}
