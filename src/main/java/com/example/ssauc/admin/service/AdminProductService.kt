package com.example.ssauc.admin.service

import com.example.ssauc.admin.repository.AdminProductRepository
import com.example.ssauc.user.product.entity.Product
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AdminProductService {
    @Autowired
    private val adminProductRepository: AdminProductRepository? = null


    fun getProducts(page: Int, sortField: String, sortDir: String): Page<Product?> {
        val sort = Sort.by(Sort.Direction.fromString(sortDir), sortField)
        return adminProductRepository!!.findAll(PageRequest.of(page, 10, sort))
    }

    fun searchProductsByName(keyword: String?, page: Int, sortField: String, sortDir: String): Page<Product?>? {
        val pageable: Pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.fromString(sortDir), sortField))
        return adminProductRepository!!.findByNameContainingIgnoreCase(keyword, pageable)
    }

    fun findProductById(productId: Long): Product? {
        return adminProductRepository!!.findById(productId).orElse(null)
    }

    @Transactional
    fun changeProductStatus(productId: Long, status: String) {
        val product = adminProductRepository!!.findById(productId)
            .orElseThrow { EntityNotFoundException("상품을 찾을 수 없습니다.") }!!

        require(isValidStatus(status)) { "잘못된 상태 값입니다." }

        product.status = status
        adminProductRepository.save(product) // 상태 변경 후 저장
    }

    private fun isValidStatus(status: String): Boolean {
        return "판매중" == status || "판매중지" == status
    }


    fun findAllProducts(): List<Product?> {
        return adminProductRepository!!.findAll()
    }
}