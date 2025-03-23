package com.example.ssauc.user.recommendation.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class RecommendationDto {
    var productId: Long? = null
    var name: String? = null
    var description: String? = null
    var price: Long? = null
    var location: String? = null
    var imageUrl: String? = null
}
