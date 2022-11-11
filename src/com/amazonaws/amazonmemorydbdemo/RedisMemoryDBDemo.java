/* 
 * 
 * Copyright 2022 Amazon.com, Inc. or its affiliates.  All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 * 
 */
package com.amazonaws.amazonmemorydbdemo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.json.Path;

/**
 * This class demonstrates how to use Amazon MemoryDB for Redis using the Jedis
 * client.
 */
public class RedisMemoryDBDemo {

	/** The properties object to hold the config. */
	private Properties properties = null;

	/** The Amazon MemoryDB for Redis cluster host name. */
	private String hostName = null;

	/** The Amazon MemoryDB for Redis cluster port. */
	private int port = 0;

	/** The client timeout value (in seconds). */
	private int clientTimeoutInSecs = 0;

	/** The connection timeout value (in seconds). */
	private int connectionTimeoutInSecs = 0;

	/** The socket timeout value (in seconds). */
	private int socketTimeoutInSecs = 0;

	/** The blocking socket timeout value (in seconds). */
	private int blockingSocketTimeoutInSecs = 0;

	/** The flag that specifies if SSL should be used in the connection. */
	private boolean useSSL = true;

	/** The name that identifies this specific instance of the Jedis client. */
	private String clientName = null;

	/**
	 * The username to connect to the Amazon MemoryDB for Redis cluster. This should
	 * be configured in the cluster's Access Control List (ACL). This username
	 * should be given access to the required keys and commands in order to perform
	 * the operations in this demo. This is specified in the access string when
	 * setting up this username in the ACL.
	 */
	private String userName = null;

	/**
	 * The password corresponding to the username to connect to the Amazon MemoryDB for
	 * Redis cluster. This is configured when setting up this username in the ACL.
	 */
	private String password = null;

	/** The maximum retry attempts in case of errors. */
	private int maxAttempts = 0;

	/** The Jedis Cluster object. */
	private JedisCluster jedisCluster = null;

	/**
	 * Constructor performing initialization.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public RedisMemoryDBDemo() throws IOException {
		initialize();
	}

	/**
	 * Loads the properties from the config file and starts the Jedis client.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void initialize() throws IOException {
		loadProperties();
		startClient();
	}

	/**
	 * Loads properties from the config file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void loadProperties() throws IOException {
		System.out.println("Reading config file...");
		properties = new Properties();
		FileInputStream fis = new FileInputStream(new File(System.getProperty("CONFIG_FILE_NAME")));
		properties.load(fis);
		// Load the cluster configuration
		hostName = properties.getProperty("MEMORYDB_CLUSTER_ENDPOINT_HOSTNAME");
		port = Integer.parseInt(properties.getProperty("MEMORYDB_CLUSTER_ENDPOINT_PORT"));
		userName = properties.getProperty("MEMORYDB_CLUSTER_ENDPOINT_USERNAME");
		password = properties.getProperty("MEMORYDB_CLUSTER_ENDPOINT_PASSWORD");
		// Load the client configuration
		clientName = properties.getProperty("MEMORYDB_CLIENT_NAME");
		useSSL = Boolean.parseBoolean(properties.getProperty("MEMORYDB_CLIENT_USE_SSL"));
		clientTimeoutInSecs = Integer.parseInt(properties.getProperty("MEMORYDB_CLIENT_TIMEOUT_IN_SECS"));
		connectionTimeoutInSecs = Integer
				.parseInt(properties.getProperty("MEMORYDB_CLIENT_CONNECTION_TIMEOUT_IN_SECS"));
		blockingSocketTimeoutInSecs = Integer
				.parseInt(properties.getProperty("MEMORYDB_CLIENT_BLOCKING_SOCKET_TIMEOUT_IN_SECS"));
		socketTimeoutInSecs = Integer.parseInt(properties.getProperty("MEMORYDB_CLIENT_SOCKET_TIMEOUT_IN_SECS"));
		maxAttempts = Integer.parseInt(properties.getProperty("MEMORYDB_CLIENT_MAX_ATTEMPTS"));
		fis.close();
		System.out.println("Completed reading config file.");
	}

	/**
	 * Starts the Jedis client for Amazon MemoryDB for Redis.
	 */
	private void startClient() {
		System.out.println("Initializing Jedis client for Amazon MemoryDB for Redis...");
		JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder().clientName(clientName)
				.timeoutMillis(clientTimeoutInSecs * 1000).connectionTimeoutMillis(connectionTimeoutInSecs * 1000)
				.blockingSocketTimeoutMillis(blockingSocketTimeoutInSecs * 1000)
				.socketTimeoutMillis(socketTimeoutInSecs * 1000).ssl(useSSL).user(userName).password(password).build();
		Set<HostAndPort> hostAndPortSet = new HashSet<HostAndPort>();
		hostAndPortSet.add(new HostAndPort(hostName, port));
		jedisCluster = new JedisCluster(hostAndPortSet, jedisClientConfig, maxAttempts);
		System.out.println("Completed initializing Jedis client for Amazon MemoryDB for Redis.");
	}

	/**
	 * Stops the Jedis client for Amazon MemoryDB for Redis.
	 */
	private void stopClient() {
		System.out.println("Shutting down Jedis client for Amazon MemoryDB for Redis...");
		jedisCluster.close();
		System.out.println("Completed shutting down Jedis client for Amazon MemoryDB for Redis.");
	}

	/**
	 * Gets a record from MemoryDB based on the key.
	 *
	 * @param key the key
	 * 
	 * @return the value for the specified key
	 */
	private String getRecord(String key) {
		System.out.println("Retrieving record for key '" + key + "'...");
		Object value = jedisCluster.get(key);
		if (value == null) {
			System.out.println("No record found.");
			return null;
		} else {
			return value.toString();
		}
	}

	/**
	 * Inserts or updates a JSON record to MemoryDB based on the key.
	 *
	 * @param key   the key
	 * @param value the value
	 * 
	 * @return the response from the set operation
	 */
	private String upsertJSONRecord(String key, String value) {
		System.out.println("Upserting JSON record for key '" + key + "'...");
		return jedisCluster.jsonSet(key, value);
	}

	/**
	 * Gets data from a JSON record in MemoryDB based on the key and optionally a
	 * path.
	 *
	 * @param key  the key
	 * @param path the JSON path
	 * 
	 * @return the value for the specified key
	 */
	private String getJSONRecord(String key, String path) {
		Object value = null;
		if (path == null) {
			System.out.println("Retrieving JSON record for key '" + key + "'...");
			value = jedisCluster.jsonGet(key);
		} else {
			System.out.println("Retrieving JSON record for key '" + key + "' and path '" + path + "'...");
			value = jedisCluster.jsonGet(key, new Path(path));
		}
		if (value == null) {
			System.out.println("No record found.");
			return null;
		} else {
			return value.toString();
		}
	}

	/**
	 * Inserts or updates a Hash record to MemoryDB based on the key.
	 *
	 * @param key   the key
	 * @param value the value
	 * 
	 * @return the response from the set operation
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	private String upsertHashRecord(String key, String value) throws JsonMappingException, JsonProcessingException {
		System.out.println("Upserting Hash record for key '" + key + "'...");
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> map = mapper.readValue(value, new TypeReference<Map<String, String>>() {
		});
		return Long.toString(jedisCluster.hset(key, map));
	}

	/**
	 * Gets data from a Hash record in MemoryDB based on the key and optionally a
	 * field.
	 *
	 * @param key   the key
	 * @param field the field
	 * 
	 * @return the value for the specified key
	 */
	private String getHashRecord(String key, String field) {
		Object value = null;
		if (field == null) {
			System.out.println("Retrieving Hash record for key '" + key + "'...");
			value = jedisCluster.hgetAll(key);
		} else {
			System.out.println("Retrieving Hash record for key '" + key + "' and field '" + field + "'...");
			value = jedisCluster.hget(key, field);
		}
		if (value == null) {
			System.out.println("No record found.");
			return null;
		} else {
			return value.toString();
		}
	}

	/**
	 * Deletes a record from MemoryDB based on the key.
	 *
	 * @param key the key
	 * 
	 * @return the response from the delete operation
	 */
	private String deleteRecord(String key) {
		System.out.println("Deleting record for key '" + key + "'...");
		if (jedisCluster.del(key) == 0) {
			return "No record found.";
		} else {
			return "Record for key '" + key + "' deleted.";
		}
	}

	/**
	 * Perform shutdown - stops the Jedis client for Amazon MemoryDB for Redis.
	 */
	private void shutdown() {
		stopClient();
	}

	/**
	 * The main method performs the following,
	 * 
	 * <pre>
	 * 1. Reads the config file.
	 * 2. Instantiates the Jedis client to connect to the Amazon MemoryDB for Redis cluster.
	 * 3. Upsert operations.
	 * 4. Retrieve operations.
	 * 5. Delete operations.
	 * 6. Shuts down this demo program.
	 * </pre>
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		// Sample data
		String record1Key = "customer:111";
		String record1Value = "{\"name\":{\"first\": \"First1\", \"last\": \"Last1\"}, \"address\": \"111 Test Street, Test City, Test State, Test Country\", \"phone\": \"+1 111-111-1111\", \"email\": \"first1last1@test.com\"}";
		String record2Key = "customer:222";
		String record2Value = "{\"name\":{\"first\": \"First2\", \"last\": \"Last2\"}, \"address\": \"222 Test Street, Test City, Test State, Test Country\", \"phone\": \"+1 222-222-2222\", \"email\": \"first2last2@test.com\"}";
		String record3Key = "customer:333";
		String record3Value = "{\"first_name\": \"First3\", \"last_name\": \"Last3\", \"address\": \"333 Test Street, Test City, Test State, Test Country\", \"phone\": \"+1 333-333-3333\", \"email\": \"first3last3@test.com\"}";
		String record4Key = "customer:444";

		// Instantiate
		RedisMemoryDBDemo redisMemoryDBDemo = new RedisMemoryDBDemo();

		// JSON record operations:
		// Upsert JSON records
		System.out.println(redisMemoryDBDemo.upsertJSONRecord(record1Key, record1Value));
		System.out.println(redisMemoryDBDemo.upsertJSONRecord(record2Key, record2Value));
		// Retrieve from JSON records
		System.out.println("Value = " + redisMemoryDBDemo.getJSONRecord(record1Key, null));
		System.out.println("Value = " + redisMemoryDBDemo.getJSONRecord(record1Key, ".name.first"));
		System.out.println("Value = " + redisMemoryDBDemo.getJSONRecord(record1Key, ".address"));

		// Hash record operations:
		// Upsert Hash records
		System.out.println(redisMemoryDBDemo.upsertHashRecord(record3Key, record3Value));
		// Retrieve from Hash records
		System.out.println("Value = " + redisMemoryDBDemo.getHashRecord(record3Key, null));
		System.out.println("Value = " + redisMemoryDBDemo.getHashRecord(record3Key, "first_name"));
		System.out.println("Value = " + redisMemoryDBDemo.getHashRecord(record3Key, "address"));

		// Delete operations:
		// Delete an existing record
		System.out.println(redisMemoryDBDemo.deleteRecord(record1Key));
		// Try retrieving a deleted record
		System.out.println("Value = " + redisMemoryDBDemo.getRecord(record1Key));
		// Try deleting a non-existent record
		System.out.println(redisMemoryDBDemo.deleteRecord(record4Key));

		// Shutdown
		redisMemoryDBDemo.shutdown();
	}

}
