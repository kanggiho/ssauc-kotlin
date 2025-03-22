package com.example.ssauc.user.search.controller;


import com.example.ssauc.user.search.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/autocomplete")
@RequiredArgsConstructor
public class AutoCompleteController {

    private final ProductSearchService productSearchService;

    @GetMapping
    public ResponseEntity<List<String>> getAutoCompleteSuggestions(
            @RequestParam("prefix") String prefix) {
        List<String> suggestions = productSearchService.getAutoCompleteSuggestions(prefix);
        return ResponseEntity.ok(suggestions);
    }
}