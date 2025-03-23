package com.example.ssauc.user.recommendation.dto

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class RecommendationResponse {
    private val recommendationProducts: List<RecommendationDto>? = null
    private val getExplanation: String? = null
}
