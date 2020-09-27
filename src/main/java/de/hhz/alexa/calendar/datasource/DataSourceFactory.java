package de.hhz.alexa.calendar.datasource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
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

import de.hhz.alexa.calendar.calender.HHZEvent;

public class DataSourceFactory {
	private String tableName = "Events";
	private Table table;
	private AmazonDynamoDB client;
	private static DataSourceFactory mDataSourceFactory;

	private DataSourceFactory() throws InterruptedException {
		client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
//				new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2")).build();
				new AwsClientBuilder.EndpointConfiguration("https://dynamodb.us-east-1.amazonaws.com", "us-east-1"))
				.build();
		DynamoDB dynamoDB = new DynamoDB(client);
		this.table = dynamoDB.getTable(tableName);
		List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("userEmail").withAttributeType("S"));

		List<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
		keySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("userEmail").withKeyType(KeyType.RANGE));

		CreateTableRequest request = new CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
				.withAttributeDefinitions(attributeDefinitions).withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(5L).withWriteCapacityUnits(5L));
		TableUtils.createTableIfNotExists(client, request);
		this.table.waitForActive();
		System.out.println("database running");
	}

	public void saveEvents(List<HHZEvent> courses, String user) {
		if (Objects.isNull(courses) || courses.size() < 1 || user == null) {
			return;
		}
		courses.forEach(c -> {
			this.table.putItem(new Item().withPrimaryKey("id", c.getId(), "userEmail", user)
					.withString("eTag", c.geteTag()).withString("startTime", c.getStartTime().getTime() + ""));
		});
	}

	public void updateEvent(HHZEvent course, String user) throws Exception {
		if (Objects.isNull(course)) {
			return;
		}
		if (course.isCancelled()) {
			deleteEvent(course, user);
			return;
		}
		UpdateItemSpec updateItemSpec = new UpdateItemSpec()
				.withPrimaryKey("id", course.getId(), "userEmail", user)
				.withUpdateExpression("set eTag = :e, startTime= :st").withValueMap(new ValueMap()
						.withString(":st", course.getStartTime().getTime() + "").withString(":e", course.geteTag()))
				.withReturnValues(ReturnValue.UPDATED_NEW);
		UpdateItemOutcome outcome= this.table.updateItem(updateItemSpec);
		System.out.println(outcome.getItem().toJSONPretty());
	}

	public void deleteEvent(HHZEvent course, String user) throws Exception {
		if (Objects.isNull(course) && !course.isCancelled()) {
			return;
		}

		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
				.withPrimaryKey("id", course.getId(), "userEmail", user).withReturnValues(ReturnValue.ALL_OLD);
		DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
		System.out.println("Deleted " + outcome.getItem());

	}

	public List<HHZEvent> loadEvents(int maxResult, String user) throws Exception {
		List<HHZEvent> eventList = new ArrayList<HHZEvent>();
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		map.put(":us", new AttributeValue().withS(user));
		ScanRequest scanRequest = new ScanRequest().withTableName(this.tableName)
				.withFilterExpression("userEmail=:us")
				.withExpressionAttributeValues(map);
		if (maxResult > 0) {
			scanRequest.setLimit(maxResult);
		}
		ScanResult result = this.client.scan(scanRequest);
		for (Map<String, AttributeValue> item : result.getItems()) {
			HHZEvent event = new HHZEvent();
			event.setId(item.get("id").getS());
			event.seteTag(item.get("eTag").getS());
			event.setStartTime(new Date(Long.parseLong(item.get("startTime").getS())));
			event.setUser(item.get("userEmail").getS());
			eventList.add(event);
		}
		eventList.forEach(e->{System.out.println("id="+e.getId()+"etag="+e.geteTag()+"email="+e.getUser());});
		return eventList;
	}



	public static DataSourceFactory getInstance() throws Exception {
		if (mDataSourceFactory != null) {
			return mDataSourceFactory;
		}

		mDataSourceFactory = new DataSourceFactory();

		return mDataSourceFactory;
	}

}
