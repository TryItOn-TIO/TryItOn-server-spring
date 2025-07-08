// ProductService에 추가할 최적화된 랜덤 조회 메서드

/**
 * 성능 최적화된 랜덤 상품 조회
 * RAND() 대신 오프셋 기반 랜덤 조회로 성능 향상
 */
public Page<ProductResponseDto> getRandomProductsByCategory(Long categoryId, Long userId, int page, int size) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

    // 1. 해당 카테고리의 총 상품 수 조회
    long totalCount = productRepository.countByCategoryHierarchy(categoryId);
    
    if (totalCount == 0) {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
    }

    // 2. 랜덤 오프셋 계산 (매번 다른 시작점)
    Random random = new Random();
    int maxOffset = Math.max(0, (int) (totalCount - size));
    int randomOffset = random.nextInt(maxOffset + 1);

    // 3. 사용자 찜 목록 조회
    Set<Long> likedProductIds = new HashSet<>();
    if (userId != null) {
        likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
    }

    // 4. 랜덤 오프셋으로 상품 조회
    List<Product> products = productRepository.findRandomByCategoryWithOffset(categoryId, size, randomOffset);
    
    // 5. DTO 변환
    List<ProductResponseDto> productDtos = products.stream()
        .map(product -> new ProductResponseDto(product, likedProductIds.contains(product.getId())))
        .collect(Collectors.toList());

    // 6. Page 객체 생성 (실제 페이징이 아닌 랜덤 조회이므로 totalCount는 조회된 개수로 설정)
    return new PageImpl<>(productDtos, PageRequest.of(0, size), productDtos.size());
}

/**
 * 기존 메서드 개선 - 단순 RAND() 사용 (작은 데이터셋용)
 */
public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Long userId, int page, int size) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

    Pageable pageable = PageRequest.of(page, size);
    Set<Long> likedProductIds = new HashSet<>();

    if (userId != null) {
        likedProductIds.addAll(wishlistRepository.findProductIdsByUserId(userId));
    }

    // 개선된 랜덤 조회 사용
    return productRepository.findPseudoRandomByCategory(category.getId(), pageable)
        .map(product -> new ProductResponseDto(product, likedProductIds.contains(product.getId())));
}
