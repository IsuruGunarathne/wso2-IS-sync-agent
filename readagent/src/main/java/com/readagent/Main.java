package com.readagent;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.io.File;
import java.net.InetSocketAddress;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    public static CqlSession connectToCassandra(Dotenv dotenv) {
        // Load environment variables from .env file
        

        String contactPoint = dotenv.get("CASSANDRA_CONTACT_POINT");
        int port = Integer.parseInt(dotenv.get("CASSANDRA_PORT"));
        

        File file = new File("../resources/reference.conf");

        DriverConfigLoader loader = DriverConfigLoader.fromFile(file);

        return new CqlSessionBuilder()
                .addContactPoint(new InetSocketAddress(contactPoint, port))
                .withLocalDatacenter("datacenter1") // Adjust to your local datacenter name
                .withConfigLoader(loader)
                .build();
    }

    public static void printData(ResultSet resultSet) {
        for (Row row : resultSet) {
            // Access individual columns and print their values
            String userId = row.getString("user_id");
            String userName = row.getString("user_name");
            System.out.printf("User ID: %s, User Name: %s%n", userId, userName);
        }
    }

    public static void read() {
        Dotenv dotenv = Dotenv.configure().load();

        String keyspace = dotenv.get("CASSANDRA_KEYSPACE");
        String table = dotenv.get("CASSANDRA_TABLE");

        try (CqlSession session = connectToCassandra(dotenv)){
            System.out.println("Connected to Cassandra.");

            String query = String.format("SELECT * FROM %s.%s", keyspace, table);

            while (true) {
                ResultSet resultSet = session.execute(query);
                printData(resultSet);
                Thread.sleep(1000);
                System.out.println();
                System.out.println("Reading data from Cassandra...");
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
    public static void main(String[] args) {
        read();
    }

}