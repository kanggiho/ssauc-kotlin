package com.example.ssauc.user.search.document;

import com.example.ssauc.user.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Data
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/mappings/products_settings.json")
@Mapping(mappingPath = "elasticsearch/mappings/products-mapping.json") // 자동완성 기능
public class ProductDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String productId;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String category;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endAt;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Integer)
    private Integer dealType;

    @Field(type = FieldType.Integer)
    private Integer bidCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Text, analyzer = "edge_ngram_analyzer")
    private String suggest;

    public ProductDocument(Product product) {
        this.productId = (product.getProductId() != null)
                ? String.valueOf(product.getProductId())
                : null;

        // ✅ 카테고리 변경: getCategory() → getCategory().getName()
        this.category = (product.getCategory() != null) ? product.getCategory().getName() : null;

        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.endAt = product.getEndAt();
        this.viewCount = product.getViewCount();

        // ✅ dealType, bidCount, likeCount는 int → Integer 변환 필요
        this.dealType = product.getDealType();
        this.bidCount = product.getBidCount();
        this.likeCount = product.getLikeCount();

        // 자동완성을 위해 상품명을 기반으로 CompletionField 생성
        this.suggest = product.getName();
    }

    public ProductDocument() {
        // unchanged
    }
}