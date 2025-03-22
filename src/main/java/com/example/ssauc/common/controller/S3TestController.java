package com.example.ssauc.common.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/s3")
public class S3TestController {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    // 파일 업로드 폼 화면
    @GetMapping("/upload")
    public String uploadForm() {
        return "/s3/uploadForm";
    }

    // 파일 업로드 처리
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        String fileName = file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            // S3에 파일 업로드
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
            // 업로드 후 S3에서 제공하는 URL 생성
            String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
            model.addAttribute("message", "업로드 성공! 파일 URL: " + fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "업로드 실패: " + e.getMessage());
        }
        return "/s3/uploadResult";
    }

    // 파일 표시를 위한 URL 입력 폼 화면
    @GetMapping("/display")
    public String displayForm() {
        return "/s3/displayForm";
    }

    // URL을 받아서 파일(예: 이미지) 출력 화면에 보여줌
    @PostMapping("/display")
    public String displayFile(@RequestParam("fileUrl") String fileUrl, Model model) {
        model.addAttribute("fileUrl", fileUrl);
        return "/s3/displayFile";
    }

    // S3 테스트 진행
    @GetMapping
    public String s3test() {
        return "/s3/s3";
    }

    // 파일 삭제 폼 화면 (삭제할 파일 URL 입력)
    @GetMapping("/delete")
    public String deleteForm() {
        return "/s3/deleteForm";
    }

    // 파일 삭제 처리: S3 URL에서 객체의 key 추출 후 삭제 실행
    @PostMapping("/delete")
    public String deleteFile(@RequestParam("fileUrl") String fileUrl, Model model) {
        String fileKey = extractKeyFromUrl(fileUrl);
        if (fileKey == null || fileKey.isEmpty()) {
            model.addAttribute("message", "유효하지 않은 파일 URL입니다.");
            return "/s3/deleteResult";
        }
        try {
            amazonS3.deleteObject(bucketName, fileKey);
            model.addAttribute("message", "삭제 성공! 파일 키: " + fileKey);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "삭제 실패: " + e.getMessage());
        }
        return "/s3/deleteResult";
    }

    /**
     * S3 URL에서 객체의 key(파일 이름)를 추출하는 메서드
     * 예시: https://bucket-name.s3.region.amazonaws.com/filename.jpg → filename.jpg
     */
    private String extractKeyFromUrl(String fileUrl) {
        try {
            // URL 객체 생성 후, 경로 추출 ("/filename.jpg")
            java.net.URL url = new java.net.URL(fileUrl);
            String path = url.getPath();
            // 앞의 '/' 제거하여 실제 key 반환
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
