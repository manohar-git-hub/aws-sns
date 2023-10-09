package com.notification.sns;

import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import software.amazon.awssdk.services.sns.model.DeleteTopicResponse;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsRequest;
import software.amazon.awssdk.services.sns.model.ListSubscriptionsResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Subscription;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import software.amazon.awssdk.services.sns.model.UnsubscribeResponse;

@RestController()
public class NotificationController {
	@Autowired
	public AwsSNSClientConfig awsSNSClientConfig;
	public SnsClient snsClient;

	public static final String EMAIL = "email";

	@PostConstruct
	public void getSnsClient() {
		snsClient = awsSNSClientConfig.getSnsClient();
	}

	@GetMapping("/health")
	public String insideMe() {
		return "Application is up and running!";
	}

	@GetMapping("/subscribers")
	public String listSubscribers() {
		ListSubscriptionsRequest request = ListSubscriptionsRequest.builder().build();

		ListSubscriptionsResponse result = snsClient.listSubscriptions(request);
		for (Subscription sub : result.subscriptions()) {
			System.out.println(sub.sdkFields());
		}
		return "Subscribers";
	}

	@PostMapping("/create/topic/{topicName}")
	public ResponseEntity<String> createTopic(@PathVariable String topicName) {
		final CreateTopicRequest topicCreateRequest = CreateTopicRequest.builder().name(topicName).build();

		final CreateTopicResponse topicCreateResponse = snsClient.createTopic(topicCreateRequest);

		if (topicCreateResponse.sdkHttpResponse().isSuccessful()) {
			System.out.println("Topic creation successful");
			System.out.println("Topic ARN: " + topicCreateResponse.topicArn());
			System.out.println("Topics: " + snsClient.listTopics());
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					topicCreateResponse.sdkHttpResponse().statusText().get());
		}
		return new ResponseEntity<String>("Topic ARN: " + topicCreateResponse.topicArn(), HttpStatus.CREATED);
	}

	@PostMapping("/email/subscriber/{arn}/{subscriberEndPoint}")
	private ResponseEntity<String> addSubscriberToTopic(@PathVariable("arn") String arn,
			@PathVariable("subscriberEndPoint") String subscriberEndPoint) throws URISyntaxException {

		final SubscribeRequest subscribeRequest = SubscribeRequest.builder().topicArn(arn).protocol(EMAIL)
				.endpoint(subscriberEndPoint).build();

		SubscribeResponse subscribeResponse = snsClient.subscribe(subscribeRequest);

		if (subscribeResponse.sdkHttpResponse().isSuccessful()) {
			System.out.println("Subscriber added successfully");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					subscribeResponse.sdkHttpResponse().statusText().get());
		}

		return new ResponseEntity<String>(
				"Subscription ARN request is pending. To confirm the subscription, check your email." + "\n Topic ARN: "
						+ subscribeResponse.subscriptionArn(),
				HttpStatus.CREATED);
	}

	@PostMapping("/email/publish/{arn}/{message}")
	private ResponseEntity<String> publishMessage(@PathVariable("arn") String arn,
			@PathVariable("message") String message) throws URISyntaxException {

		final PublishRequest publishRequest = PublishRequest.builder().topicArn(arn)
				.subject("Message from AWS SNS Service").message(message).build();

		PublishResponse publishResponse = snsClient.publish(publishRequest);

		if (publishResponse.sdkHttpResponse().isSuccessful()) {
			System.out.println("Message publishing successful");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					publishResponse.sdkHttpResponse().statusText().get());
		}

		return new ResponseEntity<String>("Email sent to subscribers. Message-ID: " + publishResponse.messageId(),
				HttpStatus.CREATED);
	}

	@DeleteMapping("/email/subscriber/delete/{arn}")
	private ResponseEntity<String> deleteSubsciber(@PathVariable("arn") String subscriptionArn)
			throws URISyntaxException {

		UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.builder().subscriptionArn(subscriptionArn).build();

		UnsubscribeResponse unsubscribeResponse = snsClient.unsubscribe(unsubscribeRequest);

		if (unsubscribeResponse.sdkHttpResponse().isSuccessful()) {
			System.out.println("Subscriber has been deleted successfully");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					unsubscribeResponse.sdkHttpResponse().statusText().get());
		}

		return new ResponseEntity<String>("Status was " + unsubscribeResponse.sdkHttpResponse().statusCode(),
				HttpStatus.OK);
	}

	@DeleteMapping("/email/topic/delete/{arn}")
	private ResponseEntity<String> deleteTopic(@PathVariable("arn") String topicArn) throws URISyntaxException {

		DeleteTopicRequest deleteTopicRequest = DeleteTopicRequest.builder().topicArn(topicArn).build();

		DeleteTopicResponse deleteTopicResponse = snsClient.deleteTopic(deleteTopicRequest);

		if (deleteTopicResponse.sdkHttpResponse().isSuccessful()) {
			System.out.println("Topic has been deleted successfully");
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					deleteTopicResponse.sdkHttpResponse().statusText().get());
		}

		return new ResponseEntity<String>(
				"Topic deleted successfully " + deleteTopicResponse.sdkHttpResponse().statusCode(), HttpStatus.OK);
	}

}
