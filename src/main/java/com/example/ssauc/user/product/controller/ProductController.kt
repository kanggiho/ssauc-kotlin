package com.example.ssauc.user.product.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.util.TokenExtractor;
import com.example.ssauc.user.product.dto.ProductInsertDto;
import com.example.ssauc.user.product.dto.ProductUpdateDto;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final AmazonS3 amazonS3;

    private final TokenExtractor tokenExtractor;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // GET: 상품 등록 페이지
    @GetMapping("/insert")
    public String insertPage(HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = productService.getCurrentUser(user.getEmail());
        model.addAttribute("user", latestUser);
        return "product/insert";
    }

    // POST: 상품 등록 처리 (AJAX로 호출)
    @PostMapping("/insert")
    @ResponseBody
    public ResponseEntity<String> insertProduct(@RequestBody ProductInsertDto productInsertDto, HttpServletRequest request) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Users latestUser = productService.getCurrentUser(user.getEmail());
        productService.insertProduct(productInsertDto, latestUser);
        return ResponseEntity.ok("상품 등록 성공!");
    }

    // 다중 파일 업로드
    @PostMapping("/uploadMultiple")
    @ResponseBody
    public ResponseEntity<?> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        // 최대 5장 제한 검증
        if (files.length > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("최대 5장의 파일만 업로드 가능합니다.");
        }
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            // 파일 크기 3MB 이하 검증
            if (file.getSize() > 3 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 크기는 3MB를 초과할 수 없습니다.");
            }
            try {
                // 고유 파일명 생성 (현재시간 접두사)
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                // 원본 이미지를 BufferedImage로 읽기
                BufferedImage originalImage = ImageIO.read(file.getInputStream());

                // Thumbnailator를 사용해 500x500 크기로 리사이징 (비율 유지 후 중앙 크롭)
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Thumbnails.of(originalImage)
                        .size(500, 500)
                        .crop(Positions.CENTER)
                        .outputFormat("png")  // 필요에 따라 변경 가능
                        .toOutputStream(os);

                byte[] resizedImageBytes = os.toByteArray();
                ByteArrayInputStream is = new ByteArrayInputStream(resizedImageBytes);

                // S3 업로드를 위한 메타데이터 설정
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(resizedImageBytes.length);
                metadata.setContentType("image/png"); // outputFormat에 맞게 설정

                // S3에 파일 업로드
                amazonS3.putObject(bucketName, fileName, is, metadata);
                // S3 URL 생성
                String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패: " + e.getMessage());
            }
        }
        // 여러 URL들을 콤마(,)로 구분하여 하나의 문자열로 결합
        String joinedUrls = String.join(",", fileUrls);
        Map<String, String> response = new HashMap<>();
        response.put("urls", joinedUrls);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/update")
    public String updatePage(@RequestParam("productId") Long productId, HttpServletRequest request, Model model) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return "redirect:/login";
        }
        Users latestUser = productService.getCurrentUser(user.getEmail());

        // 판매자 여부 체크 (product의 seller와 사용자 일치 여부)
        Product product = productService.getProductById(productId);
        if (!product.getSeller().getUserId().equals(latestUser.getUserId())) {
            return "redirect:/bid?productId=" + productId;
        }
        model.addAttribute("product", product);
        // 카테고리 리스트도 전달 (update.html에서 select 옵션용)
        model.addAttribute("categories", productService.getAllCategories());
        return "product/update";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateProduct(@RequestBody ProductUpdateDto dto, HttpServletRequest request) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Users latestUser = productService.getCurrentUser(user.getEmail());
        // 입찰 중인 상품은 수정할 수 없음
        if (productService.hasBids(dto.getProductId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 입찰중이라 수정이 불가능합니다.");
        }
        productService.updateProduct(dto, latestUser);
        return ResponseEntity.ok("상품 수정 성공!");
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteProduct(@RequestBody Map<String, Long> payload, HttpServletRequest request) {
        Users user = tokenExtractor.getUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Users latestUser = productService.getCurrentUser(user.getEmail());

        Long productId = payload.get("productId");
        if (productService.hasBids(productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 입찰중이라 삭제가 불가능합니다.");
        }
        productService.deleteProduct(productId, latestUser);
        return ResponseEntity.ok("상품 삭제 성공!");
    }

}
