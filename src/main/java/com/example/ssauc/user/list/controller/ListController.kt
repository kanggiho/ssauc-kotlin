package com.example.ssauc.user.list.controller;

import com.example.ssauc.user.list.Service.ListService;
import com.example.ssauc.user.list.dto.TempDto;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.main.service.RecentlyViewedService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("list")
@Slf4j
@RequiredArgsConstructor
public class ListController {

    private final ListService listService;
    private final TokenExtractor tokenExtractor;
    private final RecentlyViewedService recentlyViewedService;

    @GetMapping("/list")
    public String secondhandauction(Model model, @PageableDefault(size = 30) Pageable pageable, HttpServletRequest request) {
        Users user = null;
        try {
            user = tokenExtractor.getUserFromToken(request);
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        Page<TempDto> secondList = (user != null) ? listService.list(pageable, user) : listService.list(pageable, null);
        if(user != null) {
            List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);
            model.addAttribute("recentViews", recentlyVieweds);
        }
        model.addAttribute("secondList", secondList);
        model.addAttribute("user", user);
        return "list/list";
    }

    @GetMapping("/premiumlist")
    public String premiumlist() {
        return "list/premiumlist";
    }

    @GetMapping("/likelist")
    public String likelist(Model model, @PageableDefault(size = 30) Pageable pageable, HttpServletRequest request) {
        Users user = tokenExtractor.getUserFromToken(request);
        Page<TempDto> likelist = listService.likelist(pageable, user);
        List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);
        model.addAttribute("recentViews", recentlyVieweds);
        model.addAttribute("likelist", likelist);
        model.addAttribute("user", user);
        return "likelist/likelist";
    }

    @GetMapping("/category")
    public String category(Model model, @RequestParam("categoryId") Long categoryId, @PageableDefault(size = 30) Pageable pageable, HttpServletRequest request) {
        Users user = null;
        try {
            user = tokenExtractor.getUserFromToken(request);
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        Page<TempDto> categoryList = (user != null)
                ? listService.categoryList(pageable, user, categoryId)
                : listService.categoryList(pageable, null, categoryId);

        if(user != null) {
            List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);
            model.addAttribute("recentViews", recentlyVieweds);
        }
        model.addAttribute("secondList", categoryList);
        model.addAttribute("user", user);
        return "list/list";
    }

    @GetMapping("/price")
    public String getProductsByPrice(
            @RequestParam(name = "minPrice", required = false, defaultValue = "0") int minPrice,
            @RequestParam(name = "maxPrice", required = false, defaultValue = "99999999") int maxPrice,
            @PageableDefault(size = 30) Pageable pageable,
            HttpServletRequest request,
            Model model) {
        Users user = null;
        try {
            user = tokenExtractor.getUserFromToken(request);
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        Page<TempDto> filteredProducts = (user != null)
                ? listService.getProductsByPrice(pageable, user, minPrice, maxPrice)
                : listService.getProductsByPrice(pageable, null, minPrice, maxPrice);
        if(user != null) {
            List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);
            model.addAttribute("recentViews", recentlyVieweds);
        }
        model.addAttribute("secondList", filteredProducts);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("user", user);
        return "list/list";
    }

    @GetMapping("/availableBid")
    public String getAvailableBid(Pageable pageable, HttpServletRequest request, Model model) {
        Users user = null;
        try {
            user = tokenExtractor.getUserFromToken(request);
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        Page<TempDto> availableBid = (user != null)
                ? listService.getAvailableBidWithLike(pageable, user)
                : listService.getAvailableBid(pageable);
        if(user != null) {
            List<RecentlyViewed> recentlyVieweds = recentlyViewedService.getRecentlyViewedItems(user);
            model.addAttribute("recentViews", recentlyVieweds);
        }
        model.addAttribute("secondList", availableBid);
        model.addAttribute("user", user);
        return "list/list";
    }
}
