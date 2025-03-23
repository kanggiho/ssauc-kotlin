package com.example.ssauc.user.product.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDto {
    private Long productId;
    private String categoryName;
    private String name;
    private String description;
    private Long price;
    private Long startPrice;
    private String imageUrl;
    private String auctionDate;
    private Integer auctionHour;
    private Integer auctionMinute;
    private int minIncrement;
    private int dealType;
}
