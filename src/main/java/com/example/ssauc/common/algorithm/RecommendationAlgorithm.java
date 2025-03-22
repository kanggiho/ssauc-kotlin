package com.example.ssauc.common.algorithm;

import com.example.ssauc.user.main.entity.ProductLike;
import com.example.ssauc.user.main.repository.ProductLikeRepository;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.repository.CategoryRepository;
import com.example.ssauc.user.product.repository.ProductRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RecommendationAlgorithm {

    // 수정: 전역 상수 MAX_SELECTION 추가 (기본값 10)
    private static final int MAX_SELECTION = 10;

    // 수정: 생성자 주입을 위해 필드를 final로 선언


    public RecommendationAlgorithm(ProductLikeRepository productLikeRepository, ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productLikeRepository = productLikeRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 확률 분포를 기반으로 해당 productId와 연관된 좋아요 데이터를 활용하여,
     * 카테고리별 상품 중 중복 없이 최대 MAX_SELECTION 개의 상품의 productId를 추천합니다.
     *
     * @param userId 기준 상품 ID
     * @return 추천된 상품의 productId 리스트
     */
    public List<Long> recommendAlgorithm(long userId) {
        // 1. userId를 기준으로 좋아요 데이터를 조회하여 카테고리별 추천수 배열 생성
        // 수정: productId 대신 userId 기준으로 조회 (findByUser_UserId 메서드 사용)
        List<ProductLike> productLikeList = productLikeRepository.findByUser_UserId(userId);
        int categoryCount = (int) categoryRepository.count();
        int[] categoryArray = new int[categoryCount];

        for (ProductLike productLike : productLikeList) {
            int category = productLike.getProduct().getCategory().getCategoryId().intValue();
            // 카테고리 id가 1부터 시작한다고 가정하여 인덱스는 category - 1 사용
            categoryArray[category - 1]++;
        }

        // 2. 확률 분포 계산
        double temperature = computeTemperatureByStd(categoryArray);
        double[] weightList = temperedSoftmax(categoryArray, temperature);

        // 3. 확률 분포에 따라 추천 진행 (중복 없이 최대 MAX_SELECTION 개 추천)
        List<Long> recommendedProductIds = new ArrayList<>();
        Random random = new Random();

        // 반복: 추천할 수 있는 상품이 있고, MAX_SELECTION 미만인 동안 진행
        while (recommendedProductIds.size() < MAX_SELECTION) {
            // 카테고리 선택: 누적 확률을 이용해 랜덤하게 카테고리 인덱스 선택
            double r = random.nextDouble();
            double cumulative = 0.0;
            int selectedCategoryIndex = -1;
            for (int i = 0; i < weightList.length; i++) {
                cumulative += weightList[i];
                if (r <= cumulative) {
                    selectedCategoryIndex = i;
                    break;
                }
            }
            if (selectedCategoryIndex == -1) {
                selectedCategoryIndex = weightList.length - 1;
            }

            // 카테고리 id: 인덱스 + 1 (카테고리 id가 1부터 시작한다고 가정)
            long selectedCategoryId = selectedCategoryIndex + 1;

            // 해당 카테고리의 모든 상품 조회
            List<Product> products = productRepository.findByCategory_CategoryId(selectedCategoryId);

            // 수정: 이미 추천된 상품은 제외
            List<Product> availableProducts = products.stream()
                    .filter(product -> !recommendedProductIds.contains(product.getProductId()))
                    .toList();

            // 더 이상 추천할 상품이 없으면 해당 카테고리는 건너뜁니다.
            if (availableProducts.isEmpty()) {
                // 만약 모든 카테고리에서 추천 가능한 상품이 없다면 종료
                boolean allEmpty = true;
                for (int i = 0; i < categoryCount; i++) {
                    long catId = i + 1;
                    List<Product> prodList = productRepository.findByCategory_CategoryId(catId).stream()
                            .filter(product -> !recommendedProductIds.contains(product.getProductId()))
                            .toList();
                    if (!prodList.isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                }
                if (allEmpty) {
                    break;
                }
                continue;
            }

            // 해당 카테고리에서 랜덤하게 하나 선택
            int randomIndex = random.nextInt(availableProducts.size());
            Product selectedProduct = availableProducts.get(randomIndex);
            recommendedProductIds.add(selectedProduct.getProductId());
        }

        return recommendedProductIds;
    }

    /**
     * 표준편차 기반 temperature 계산.
     * 각 카테고리 추천수 분포의 표준편차가 클수록 temperature가 커져 확률 분포가 평탄해지도록 함.
     *
     * @param counts 각 카테고리의 추천수 배열
     * @return 계산된 temperature 값
     */
    public static double computeTemperatureByStd(int[] counts) {
        double mean = Arrays.stream(counts).average().orElse(0);
        double variance = Arrays.stream(counts)
                .mapToDouble(c -> Math.pow(c - mean, 2))
                .sum() / counts.length;
        return Math.max(1.0, Math.sqrt(variance));
    }

    /**
     * tempered softmax 함수: 추천 수 배열과 temperature를 받아 확률 분포를 계산
     *
     * @param recommendationCounts 각 카테고리의 추천수 배열
     * @param temperature          temperature 파라미터
     * @return 확률 분포 배열
     */
    public static double[] temperedSoftmax(int[] recommendationCounts, double temperature) {
        double[] expValues = new double[recommendationCounts.length];
        double sumExp = 0.0;

        // 각 추천수에 대해 exp(추천수 / temperature)를 계산
        for (int i = 0; i < recommendationCounts.length; i++) {
            expValues[i] = Math.exp(recommendationCounts[i] / temperature);
            sumExp += expValues[i];
        }

        // 확률 분포 계산: 각 값 / 전체 합
        double[] probabilities = new double[recommendationCounts.length];
        for (int i = 0; i < recommendationCounts.length; i++) {
            probabilities[i] = expValues[i] / sumExp;
        }

        return probabilities;
    }
}
