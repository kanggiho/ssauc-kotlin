package com.example.ssauc.user.bid.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.RequiredArgsConstructor
import lombok.Setter


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
class CarouselImage {
    var url: String? = null
    var alt: String? = null
}
