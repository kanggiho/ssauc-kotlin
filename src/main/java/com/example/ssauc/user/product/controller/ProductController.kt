package com.example.ssauc.user.product.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal

@org.springframework.stereotype.Controller
@RequestMapping("/product")
@lombok.RequiredArgsConstructor
class ProductController {
    private val productService: ProductService? = null

    private val amazonS3: AmazonS3? = null

    private val tokenExtractor: TokenExtractor? = null

    @org.springframework.beans.factory.annotation.Value("\${aws.s3.bucket}")
    private val bucketName: String? = null

    // GET: 상품 등록 페이지
    @GetMapping("/insert")
    fun insertPage(request: jakarta.servlet.http.HttpServletRequest, model: org.springframework.ui.Model): String {
        val user: Users = tokenExtractor.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser: Users = productService.getCurrentUser(user.email)
        model.addAttribute("user", latestUser)
        return "product/insert"
    }

    // POST: 상품 등록 처리 (AJAX로 호출)
    @PostMapping("/insert")
    @org.springframework.web.bind.annotation.ResponseBody
    fun insertProduct(
        @org.springframework.web.bind.annotation.RequestBody productInsertDto: ProductInsertDto,
        request: jakarta.servlet.http.HttpServletRequest
    ): ResponseEntity<String> {
        val user: Users = tokenExtractor.getUserFromToken(request)
            ?: return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body<String>("로그인이 필요합니다.")
        val latestUser: Users = productService.getCurrentUser(user.email)
        productService.insertProduct(productInsertDto, latestUser)
        return ResponseEntity.ok<String>("상품 등록 성공!")
    }

    // 다중 파일 업로드
    @PostMapping("/uploadMultiple")
    @org.springframework.web.bind.annotation.ResponseBody
    fun uploadMultipleFiles(@RequestParam("files") files: Array<MultipartFile?>): ResponseEntity<*> {
        // 최대 5장 제한 검증
        if (files.size > 5) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body<String>("최대 5장의 파일만 업로드 가능합니다.")
        }
        val fileUrls: MutableList<String> = java.util.ArrayList()
        for (file in files) {
            // 파일 크기 3MB 이하 검증
            if (file.getSize() > 3 * 1024 * 1024) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                    .body<String>("파일 크기는 3MB를 초과할 수 없습니다.")
            }
            try {
                // 고유 파일명 생성 (현재시간 접두사)
                val fileName = System.currentTimeMillis().toString() + "_" + file.getOriginalFilename()

                // 원본 이미지를 BufferedImage로 읽기
                val originalImage: BufferedImage = ImageIO.read(file.getInputStream())

                // Thumbnailator를 사용해 500x500 크기로 리사이징 (비율 유지 후 중앙 크롭)
                val os = java.io.ByteArrayOutputStream()
                Thumbnails.of(originalImage)
                    .size(500, 500)
                    .crop(Positions.CENTER)
                    .outputFormat("png") // 필요에 따라 변경 가능
                    .toOutputStream(os)

                val resizedImageBytes = os.toByteArray()
                val `is`: ByteArrayInputStream = ByteArrayInputStream(resizedImageBytes)

                // S3 업로드를 위한 메타데이터 설정
                val metadata: ObjectMetadata = ObjectMetadata()
                metadata.setContentLength(resizedImageBytes.size.toLong())
                metadata.setContentType("image/png") // outputFormat에 맞게 설정

                // S3에 파일 업로드
                amazonS3.putObject(bucketName, fileName, `is`, metadata)
                // S3 URL 생성
                val fileUrl: String = amazonS3.getUrl(bucketName, fileName).toString()
                fileUrls.add(fileUrl)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body<String>("파일 업로드 실패: " + e.message)
            }
        }
        // 여러 URL들을 콤마(,)로 구분하여 하나의 문자열로 결합
        val joinedUrls = java.lang.String.join(",", fileUrls)
        val response: MutableMap<String, String> = java.util.HashMap()
        response["urls"] = joinedUrls
        return ResponseEntity.ok<Map<String, String>>(response)
    }

    @GetMapping("/update")
    fun updatePage(
        @RequestParam("productId") productId: Long,
        request: jakarta.servlet.http.HttpServletRequest,
        model: org.springframework.ui.Model
    ): String {
        val user: Users = tokenExtractor.getUserFromToken(request) ?: return "redirect:/login"
        val latestUser: Users = productService.getCurrentUser(user.email)

        // 판매자 여부 체크 (product의 seller와 사용자 일치 여부)
        val product: com.example.ssauc.user.product.entity.Product = productService.getProductById(productId)
        if (product.seller!!.userId != latestUser.userId) {
            return "redirect:/bid?productId=$productId"
        }
        model.addAttribute("product", product)
        // 카테고리 리스트도 전달 (update.html에서 select 옵션용)
        model.addAttribute("categories", productService.getAllCategories())
        return "product/update"
    }

    @PostMapping("/update")
    @org.springframework.web.bind.annotation.ResponseBody
    fun updateProduct(
        @org.springframework.web.bind.annotation.RequestBody dto: ProductUpdateDto,
        request: jakarta.servlet.http.HttpServletRequest
    ): ResponseEntity<String> {
        val user: Users = tokenExtractor.getUserFromToken(request)
            ?: return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body<String>("로그인이 필요합니다.")
        val latestUser: Users = productService.getCurrentUser(user.email)
        // 입찰 중인 상품은 수정할 수 없음
        if (productService.hasBids(dto.getProductId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body<String>("이미 입찰중이라 수정이 불가능합니다.")
        }
        productService.updateProduct(dto, latestUser)
        return ResponseEntity.ok<String>("상품 수정 성공!")
    }

    @PostMapping("/delete")
    @org.springframework.web.bind.annotation.ResponseBody
    fun deleteProduct(
        @org.springframework.web.bind.annotation.RequestBody payload: Map<String?, Long?>,
        request: jakarta.servlet.http.HttpServletRequest
    ): ResponseEntity<String> {
        val user: Users = tokenExtractor.getUserFromToken(request)
            ?: return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body<String>("로그인이 필요합니다.")
        val latestUser: Users = productService.getCurrentUser(user.email)

        val productId = payload["productId"]
        if (productService.hasBids(productId)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body<String>("이미 입찰중이라 삭제가 불가능합니다.")
        }
        productService.deleteProduct(productId, latestUser)
        return ResponseEntity.ok<String>("상품 삭제 성공!")
    }
}
