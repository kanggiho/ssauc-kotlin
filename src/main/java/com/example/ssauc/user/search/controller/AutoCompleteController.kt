package com.example.ssauc.user.search.controller

import com.example.ssauc.user.search.service.ProductSearchService
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/autocomplete")
@RequiredArgsConstructor
class AutoCompleteController {
    private val productSearchService: ProductSearchService? = null

    @GetMapping
    fun getAutoCompleteSuggestions(
        @RequestParam("prefix") prefix: String?
    ): ResponseEntity<List<String?>> {
        val suggestions = productSearchService!!.getAutoCompleteSuggestions(prefix)
        return ResponseEntity.ok(suggestions)
    }
}