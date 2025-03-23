package com.example.ssauc.common.controller

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.net.URL

@Controller
@RequestMapping("/s3")
class S3TestController {
    @Autowired
    private val amazonS3: AmazonS3? = null

    @Value("\${aws.s3.bucket}")
    private val bucketName: String? = null

    // 파일 업로드 폼 화면
    @GetMapping("/upload")
    fun uploadForm(): String {
        return "/s3/uploadForm"
    }

    // 파일 업로드 처리
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, model: Model): String {
        val fileName = file.originalFilename
        try {
            val metadata = ObjectMetadata()
            metadata.contentLength = file.size
            // S3에 파일 업로드
            amazonS3!!.putObject(bucketName, fileName, file.inputStream, metadata)
            // 업로드 후 S3에서 제공하는 URL 생성
            val fileUrl = amazonS3.getUrl(bucketName, fileName).toString()
            model.addAttribute("message", "업로드 성공! 파일 URL: $fileUrl")
        } catch (e: Exception) {
            e.printStackTrace()
            model.addAttribute("message", "업로드 실패: " + e.message)
        }
        return "/s3/uploadResult"
    }

    // 파일 표시를 위한 URL 입력 폼 화면
    @GetMapping("/display")
    fun displayForm(): String {
        return "/s3/displayForm"
    }

    // URL을 받아서 파일(예: 이미지) 출력 화면에 보여줌
    @PostMapping("/display")
    fun displayFile(@RequestParam("fileUrl") fileUrl: String?, model: Model): String {
        model.addAttribute("fileUrl", fileUrl)
        return "/s3/displayFile"
    }

    // S3 테스트 진행
    @GetMapping
    fun s3test(): String {
        return "/s3/s3"
    }

    // 파일 삭제 폼 화면 (삭제할 파일 URL 입력)
    @GetMapping("/delete")
    fun deleteForm(): String {
        return "/s3/deleteForm"
    }

    // 파일 삭제 처리: S3 URL에서 객체의 key 추출 후 삭제 실행
    @PostMapping("/delete")
    fun deleteFile(@RequestParam("fileUrl") fileUrl: String, model: Model): String {
        val fileKey = extractKeyFromUrl(fileUrl)
        if (fileKey == null || fileKey.isEmpty()) {
            model.addAttribute("message", "유효하지 않은 파일 URL입니다.")
            return "/s3/deleteResult"
        }
        try {
            amazonS3!!.deleteObject(bucketName, fileKey)
            model.addAttribute("message", "삭제 성공! 파일 키: $fileKey")
        } catch (e: Exception) {
            e.printStackTrace()
            model.addAttribute("message", "삭제 실패: " + e.message)
        }
        return "/s3/deleteResult"
    }

    /**
     * S3 URL에서 객체의 key(파일 이름)를 추출하는 메서드
     * 예시: https://bucket-name.s3.region.amazonaws.com/filename.jpg → filename.jpg
     */
    private fun extractKeyFromUrl(fileUrl: String): String? {
        try {
            // URL 객체 생성 후, 경로 추출 ("/filename.jpg")
            val url = URL(fileUrl)
            val path = url.path
            // 앞의 '/' 제거하여 실제 key 반환
            return if (path.startsWith("/")) path.substring(1) else path
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
