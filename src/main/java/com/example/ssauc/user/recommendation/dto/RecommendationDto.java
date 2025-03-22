package com.example.ssauc.user.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationDto {
   Long productId;
   String name;
   String description;
   Long price;
   String location;
   String imageUrl;
}
