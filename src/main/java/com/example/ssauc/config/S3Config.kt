package com.example.ssauc.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config {
    @Value("\${aws.s3.accessKey}")
    private val accessKey: String? = null

    @Value("\${aws.s3.secretKey}")
    private val secretKey: String? = null

    @Value("\${aws.s3.region}")
    private val region: String? = null

    @Bean
    fun amazonS3(): AmazonS3 {
        val creds = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(creds))
            .build()
    }
}
