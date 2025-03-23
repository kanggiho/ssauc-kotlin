package com.example.ssauc.user.recommendation.dto;

import com.example.ssauc.user.product.entity.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecommendationResponse {
    private List<RecommendationDto> recommendationProducts;
    private String getExplanation;
}
