package com.example.ssauc.user.search.controller

import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.search.document.ProductDocument
import com.example.ssauc.user.search.service.ProductIndexService
import com.example.ssauc.user.search.service.ProductSearchService
import lombok.RequiredArgsConstructor
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
class AdminSearchController {
    private val productIndexService: ProductIndexService? = null
    private val productSearchService: ProductSearchService? = null

    @PostMapping("/products")
    fun createProduct(@RequestBody doc: ProductDocument): ProductDocument {
        val product = Product()
        if (doc.productId != null) {
            product.productId = doc.productId.toLong()
        }
        product.name = doc.name
        product.description = doc.description
        product.price = doc.price
        // 필요한 필드 매핑 추가
        productIndexService!!.saveProduct(product)
        return doc
    }

    @GetMapping("/analyze")
    fun analyze(
        @RequestParam analyzer: String?,
        @RequestParam text: String?
    ): String? {
        return productSearchService!!.analyzeKeyword(analyzer, text)
    }

    @GetMapping("/search")
    fun search(@RequestParam keyword: String?): List<ProductDocument?> {
        return productSearchService!!.adminSearchProducts(keyword)
    }
}
