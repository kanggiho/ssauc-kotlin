package com.example.ssauc.user.search.document

import com.example.ssauc.user.product.entity.Product
import com.fasterxml.jackson.annotation.JsonFormat
import lombok.Data
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import java.time.LocalDateTime

@Data
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/mappings/products_settings.json")
@Mapping(mappingPath = "elasticsearch/mappings/products-mapping.json") // 자동완성 기능
class ProductDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private var productId: String? = null

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private var category: String? = null

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private var name: String? = null

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private var description: String? = null

    @Field(type = FieldType.Long)
    private var price: Long? = null

    @Field(type = FieldType.Date, format = [], pattern = ["yyyy-MM-dd'T'HH:mm:ss"])
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private var createdAt: LocalDateTime? = null

    @Field(type = FieldType.Date, format = [], pattern = ["yyyy-MM-dd'T'HH:mm:ss"])
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private var updatedAt: LocalDateTime? = null

    @Field(type = FieldType.Date, format = [], pattern = ["yyyy-MM-dd'T'HH:mm:ss"])
    private var endAt: LocalDateTime? = null

    @Field(type = FieldType.Long)
    private var viewCount: Long? = null

    @Field(type = FieldType.Integer)
    private var dealType: Int? = null

    @Field(type = FieldType.Integer)
    private var bidCount: Int? = null

    @Field(type = FieldType.Integer)
    private var likeCount: Int? = null

    @Field(type = FieldType.Text, analyzer = "edge_ngram_analyzer")
    private var suggest: String? = null

    constructor(product: Product) {
        this.productId = if ((product.productId != null)) product.productId.toString() else
            null

        // ✅ 카테고리 변경: getCategory() → getCategory().getName()
        this.category = if ((product.getCategory() != null)) product.getCategory().getName() else null

        this.name = product.name
        this.description = product.description
        this.price = product.price
        this.createdAt = product.createdAt
        this.updatedAt = product.updatedAt
        this.endAt = product.endAt
        this.viewCount = product.viewCount

        // ✅ dealType, bidCount, likeCount는 int → Integer 변환 필요
        this.dealType = product.dealType
        this.bidCount = product.bidCount
        this.likeCount = product.likeCount

        // 자동완성을 위해 상품명을 기반으로 CompletionField 생성
        this.suggest = product.name
    }

    constructor()
}