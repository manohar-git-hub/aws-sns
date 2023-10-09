package com.notification.sns;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Component
public class AwsSNSClientConfig {
	public static final String ACCESS_KEY = "AKIAYJ5CXYLR2AV5YMCV";
	public static final String SECRET_KEY = "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";

	@Primary
	@Bean
	public SnsClient getSnsClient() {
		return SnsClient.builder().credentialsProvider(getAwsCredentials(ACCESS_KEY, SECRET_KEY))
				.region(Region.AP_SOUTH_1).build();
	}

	private AwsCredentialsProvider getAwsCredentials(String accessKeyID, String secretAccessKey) {
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyID, secretAccessKey);
		AwsCredentialsProvider awsCredentialsProvider = () -> awsBasicCredentials;
		return awsCredentialsProvider;
	}
}
