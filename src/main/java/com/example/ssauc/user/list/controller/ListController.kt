package com.example.ssauc.user.list.controller

import com.example.ssauc.user.list.Service.ListService
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.util.TokenExtractor
import com.example.ssauc.user.main.service.RecentlyViewedService
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("list")
@Slf4j
@RequiredArgsConstructor
class ListController {
    private val listService: ListService? = null
    private val tokenExtractor: TokenExtractor? = null
    private val recentlyViewedService: RecentlyViewedService? = null

    @GetMapping("/list")
    fun secondhandauction(
        model: Model,
        @PageableDefault(size = 30) pageable: Pageable,
        request: HttpServletRequest
    ): String {
        var user: Users? = null
        try {
            user = tokenExtractor!!.getUserFromToken(request)
        } catch (e: Exception) {
            ListController.log.error(e.message)
        }
        val secondList = if ((user != null)) listService!!.list(pageable, user) else listService!!.list(pageable, null)
        if (user != null) {
            val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)
            model.addAttribute("recentViews", recentlyVieweds)
        }
        model.addAttribute("secondList", secondList)
        model.addAttribute("user", user)
        return "list/list"
    }

    @GetMapping("/premiumlist")
    fun premiumlist(): String {
        return "list/premiumlist"
    }

    @GetMapping("/likelist")
    fun likelist(model: Model, @PageableDefault(size = 30) pageable: Pageable, request: HttpServletRequest): String {
        val user = tokenExtractor!!.getUserFromToken(request)
        val likelist = listService!!.likelist(pageable, user)
        val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)
        model.addAttribute("recentViews", recentlyVieweds)
        model.addAttribute("likelist", likelist)
        model.addAttribute("user", user)
        return "likelist/likelist"
    }

    @GetMapping("/category")
    fun category(
        model: Model,
        @RequestParam("categoryId") categoryId: Long?,
        @PageableDefault(size = 30) pageable: Pageable,
        request: HttpServletRequest
    ): String {
        var user: Users? = null
        try {
            user = tokenExtractor!!.getUserFromToken(request)
        } catch (e: Exception) {
            ListController.log.error(e.message)
        }
        val categoryList = if ((user != null))
            listService!!.categoryList(pageable, user, categoryId)
        else
            listService!!.categoryList(pageable, null, categoryId)

        if (user != null) {
            val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)
            model.addAttribute("recentViews", recentlyVieweds)
        }
        model.addAttribute("secondList", categoryList)
        model.addAttribute("user", user)
        return "list/list"
    }

    @GetMapping("/price")
    fun getProductsByPrice(
        @RequestParam(name = "minPrice", required = false, defaultValue = "0") minPrice: Int,
        @RequestParam(name = "maxPrice", required = false, defaultValue = "99999999") maxPrice: Int,
        @PageableDefault(size = 30) pageable: Pageable,
        request: HttpServletRequest,
        model: Model
    ): String {
        var user: Users? = null
        try {
            user = tokenExtractor!!.getUserFromToken(request)
        } catch (e: Exception) {
            ListController.log.error(e.message)
        }
        val filteredProducts = if ((user != null))
            listService!!.getProductsByPrice(pageable, user, minPrice, maxPrice)
        else
            listService!!.getProductsByPrice(pageable, null, minPrice, maxPrice)
        if (user != null) {
            val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)
            model.addAttribute("recentViews", recentlyVieweds)
        }
        model.addAttribute("secondList", filteredProducts)
        model.addAttribute("minPrice", minPrice)
        model.addAttribute("maxPrice", maxPrice)
        model.addAttribute("user", user)
        return "list/list"
    }

    @GetMapping("/availableBid")
    fun getAvailableBid(pageable: Pageable, request: HttpServletRequest, model: Model): String {
        var user: Users? = null
        try {
            user = tokenExtractor!!.getUserFromToken(request)
        } catch (e: Exception) {
            ListController.log.error(e.message)
        }
        val availableBid = if ((user != null))
            listService!!.getAvailableBidWithLike(pageable, user)
        else
            listService!!.getAvailableBid(pageable)
        if (user != null) {
            val recentlyVieweds = recentlyViewedService!!.getRecentlyViewedItems(user)
            model.addAttribute("recentViews", recentlyVieweds)
        }
        model.addAttribute("secondList", availableBid)
        model.addAttribute("user", user)
        return "list/list"
    }
}
