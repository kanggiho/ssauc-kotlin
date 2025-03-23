package com.example.ssauc.common.algorithm

import com.example.ssauc.user.main.repository.ProductLikeRepository
import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.product.repository.CategoryRepository
import com.example.ssauc.user.product.repository.ProductRepository
import java.util.*
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class RecommendationAlgorithm // 수정: 생성자 주입을 위해 필드를 final로 선언
    (
    private val productLikeRepository: ProductLikeRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {
    /**
     * 확률 분포를 기반으로 해당 productId와 연관된 좋아요 데이터를 활용하여,
     * 카테고리별 상품 중 중복 없이 최대 MAX_SELECTION 개의 상품의 productId를 추천합니다.
     *
     * @param userId 기준 상품 ID
     * @return 추천된 상품의 productId 리스트
     */
    fun recommendAlgorithm(userId: Long): List<Long?> {
        // 1. userId를 기준으로 좋아요 데이터를 조회하여 카테고리별 추천수 배열 생성
        // 수정: productId 대신 userId 기준으로 조회 (findByUser_UserId 메서드 사용)
        val productLikeList = productLikeRepository.findByUser_UserId(userId)
        val categoryCount = categoryRepository.count().toInt()
        val categoryArray = IntArray(categoryCount)

        for (productLike in productLikeList) {
            val category: Int = productLike.product.getCategory().getCategoryId().intValue()
            // 카테고리 id가 1부터 시작한다고 가정하여 인덱스는 category - 1 사용
            categoryArray[category - 1]++
        }

        // 2. 확률 분포 계산
        val temperature = computeTemperatureByStd(categoryArray)
        val weightList = temperedSoftmax(categoryArray, temperature)

        // 3. 확률 분포에 따라 추천 진행 (중복 없이 최대 MAX_SELECTION 개 추천)
        val recommendedProductIds: MutableList<Long?> = ArrayList()
        val random = Random()

        // 반복: 추천할 수 있는 상품이 있고, MAX_SELECTION 미만인 동안 진행
        while (recommendedProductIds.size < MAX_SELECTION) {
            // 카테고리 선택: 누적 확률을 이용해 랜덤하게 카테고리 인덱스 선택
            val r = random.nextDouble()
            var cumulative = 0.0
            var selectedCategoryIndex = -1
            for (i in weightList.indices) {
                cumulative += weightList[i]
                if (r <= cumulative) {
                    selectedCategoryIndex = i
                    break
                }
            }
            if (selectedCategoryIndex == -1) {
                selectedCategoryIndex = weightList.size - 1
            }

            // 카테고리 id: 인덱스 + 1 (카테고리 id가 1부터 시작한다고 가정)
            val selectedCategoryId = (selectedCategoryIndex + 1).toLong()

            // 해당 카테고리의 모든 상품 조회
            val products = productRepository.findByCategory_CategoryId(selectedCategoryId)

            // 수정: 이미 추천된 상품은 제외
            val availableProducts = products.stream()
                .filter { product: Product -> !recommendedProductIds.contains(product.productId) }
                .toList()

            // 더 이상 추천할 상품이 없으면 해당 카테고리는 건너뜁니다.
            if (availableProducts.isEmpty()) {
                // 만약 모든 카테고리에서 추천 가능한 상품이 없다면 종료
                var allEmpty = true
                for (i in 0 until categoryCount) {
                    val catId = (i + 1).toLong()
                    val prodList = productRepository.findByCategory_CategoryId(catId).stream()
                        .filter { product: Product -> !recommendedProductIds.contains(product.productId) }
                        .toList()
                    if (!prodList.isEmpty()) {
                        allEmpty = false
                        break
                    }
                }
                if (allEmpty) {
                    break
                }
                continue
            }

            // 해당 카테고리에서 랜덤하게 하나 선택
            val randomIndex = random.nextInt(availableProducts.size)
            val selectedProduct = availableProducts[randomIndex]
            recommendedProductIds.add(selectedProduct.productId)
        }

        return recommendedProductIds
    }

    companion object {
        // 수정: 전역 상수 MAX_SELECTION 추가 (기본값 10)
        private const val MAX_SELECTION = 10


        /**
         * 표준편차 기반 temperature 계산.
         * 각 카테고리 추천수 분포의 표준편차가 클수록 temperature가 커져 확률 분포가 평탄해지도록 함.
         *
         * @param counts 각 카테고리의 추천수 배열
         * @return 계산된 temperature 값
         */
        fun computeTemperatureByStd(counts: IntArray): Double {
            val mean = Arrays.stream(counts).average().orElse(0.0)
            val variance = Arrays.stream(counts)
                .mapToDouble { c: Int -> (c - mean).pow(2.0) }
                .sum() / counts.size
            return max(1.0, sqrt(variance))
        }

        /**
         * tempered softmax 함수: 추천 수 배열과 temperature를 받아 확률 분포를 계산
         *
         * @param recommendationCounts 각 카테고리의 추천수 배열
         * @param temperature          temperature 파라미터
         * @return 확률 분포 배열
         */
        fun temperedSoftmax(recommendationCounts: IntArray, temperature: Double): DoubleArray {
            val expValues = DoubleArray(recommendationCounts.size)
            var sumExp = 0.0

            // 각 추천수에 대해 exp(추천수 / temperature)를 계산
            for (i in recommendationCounts.indices) {
                expValues[i] = exp(recommendationCounts[i] / temperature)
                sumExp += expValues[i]
            }

            // 확률 분포 계산: 각 값 / 전체 합
            val probabilities = DoubleArray(recommendationCounts.size)
            for (i in recommendationCounts.indices) {
                probabilities[i] = expValues[i] / sumExp
            }

            return probabilities
        }
    }
}
