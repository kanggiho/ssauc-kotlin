package com.example.ssauc.user.product.entity

import jakarta.persistence.*
import lombok.*

@Entity
@Builder
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var categoryId: Long? = null


    @Column(nullable = false, length = 100)
    var name: String? = null

    // 연관 관계 설정
    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true)
    var products: List<Product>? = null
}
