package de.hhz.alexa.calendar.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.fasterxml.jackson.core.JsonParseException;

import de.hhz.alexa.calendar.utils.Course;

public class DataSourceFactory {
	private String tableName = "Events";
	private Table table;
	private AmazonDynamoDB client;
	private static DataSourceFactory mDataSourceFactory;

	private DataSourceFactory() throws InterruptedException {
		client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
//				new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2")).build();
				new AwsClientBuilder.EndpointConfiguration("https://dynamodb.us-east-1.amazonaws.com", "us-east-1")).build();
		DynamoDB dynamoDB = new DynamoDB(client);
		this.table = dynamoDB.getTable(tableName);
//		this.table.delete();
//		List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
//		attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));
//
//		List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
//		keySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH));
//
//		CreateTableRequest request = new CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
//				.withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
//						new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(5L));
//		TableUtils.createTableIfNotExists(client, request);
//		this.table.waitForActive();
	}

	public void saveEvents(List<Course> courses) {
		if (Objects.isNull(courses) || courses.size() < 1) {
			return;
		}
		courses.forEach(c -> {
			this.table.putItem(new Item().withPrimaryKey("id", c.getId()).withString("eTag", c.geteTag()));
		});
	}

	public void updateEvent(Course course) {
		if (Objects.isNull(course) || course.isCancelled()) {
			return;
		}
		UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", course.getId())
				.withUpdateExpression("set eTag = :e").withValueMap(new ValueMap().withString(":e", course.geteTag()))
				.withReturnValues(ReturnValue.UPDATED_NEW);

		System.out.println("Updating the item...");
		this.table.updateItem(updateItemSpec);
	}

	public void deleteEvent(Course course) {
		if (Objects.isNull(course) || !course.isCancelled()) {
			return;
		}
		UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", course.getId())
				.withUpdateExpression("set eTag = :e").withValueMap(new ValueMap().withString(":e", course.geteTag()))
				.withReturnValues(ReturnValue.UPDATED_NEW);

		System.out.println("Updating the item...");
		this.table.updateItem(updateItemSpec);
	}

	public HashMap<String, String> loadEvents() throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		ScanRequest scanRequest = new ScanRequest().withTableName(this.tableName);
		ScanResult result = this.client.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			map.put(item.get("id").getS(), item.get("eTag").getS());
		}
		return map;
	}

	public static DataSourceFactory getInstance() throws Exception {
		if (mDataSourceFactory != null) {
			return mDataSourceFactory;
		}
	
			mDataSourceFactory = new DataSourceFactory();
		
		return mDataSourceFactory;
	}

}
