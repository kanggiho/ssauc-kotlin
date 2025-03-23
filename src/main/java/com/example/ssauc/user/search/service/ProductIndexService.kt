package com.example.ssauc.user.search.service

import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.ProductRepository
import com.example.ssauc.user.search.document.ProductDocument
import com.example.ssauc.user.search.repository.ProductSearchRepository
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import javax.annotation.PostConstruct


@Service
class ProductIndexService // 생성자 주입 방식 사용 (Spring 권장)
    (private val productRepository: ProductRepository, private val productSearchRepository: ProductSearchRepository) {
    //MySQL → Elasticsearch로 전체 데이터 색인 (애플리케이션 실행 시 자동 실행)
    @PostConstruct
    fun initIndex() {
        reindexAllProducts()
        println("✅ MySQL 데이터를 Elasticsearch에 색인 완료!")
    }

    // MySQL에서 모든 상품을 가져와 Elasticsearch에 저장
    fun reindexAllProducts() {
        val products = productRepository.findAll()
        val documents = products.stream()
            .map { product: Product? ->
                this.convertToDocument(
                    product!!
                )
            }  // 🔵 변환 로직을 별도 메서드로 분리
            .collect(Collectors.toList())
        productSearchRepository.saveAll(documents)
    }

    //새로운 상품 추가 시 MySQL과 Elasticsearch에 저장
    fun saveProduct(product: Product) {
        // MySQL에 저장
        productRepository.save(product)
        // Elasticsearch에 저장
        val doc = convertToDocument(product)
        productSearchRepository.save(doc)
        println("✅ 새로운 상품 저장됨! (MySQL + Elasticsearch)")
    }

    //상품 삭제 시 MySQL과 Elasticsearch에서도 삭제
    fun deleteProduct(productId: Long) {
        // MySQL에서 삭제
        productRepository.deleteById(productId)
        // Elasticsearch에서 삭제
        productSearchRepository.deleteById(productId.toString())
        println(" 상품 삭제됨! (MySQL + Elasticsearch)")
    }


    //  MySQL 상품 데이터를 Elasticsearch 문서로 변환하는 메서드
    private fun convertToDocument(product: Product): ProductDocument {
        val doc = ProductDocument()
        doc.productId = product.productId.toString()
        doc.name = product.name
        doc.category = if (product.getCategory() != null) product.getCategory().getName() else null
        doc.description = product.description
        doc.price = product.price
        doc.createdAt = product.createdAt
        doc.updatedAt = product.updatedAt
        doc.endAt = product.endAt
        doc.viewCount = product.viewCount
        doc.dealType = product.dealType
        doc.bidCount = product.bidCount
        doc.likeCount = product.likeCount
        return doc
    }
}