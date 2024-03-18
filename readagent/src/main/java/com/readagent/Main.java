package com.readagent;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.io.File;
import java.net.InetSocketAddress;
import java.time.Duration;

public class Main {

    public static void read(){
        // Cassandra connection parameters
        String contactPoint = "127.0.0.1"; // Change this to your Cassandra node's IP
        int port = 9042; // Default Cassandra port
        String keyspace = "sync"; // Keyspace name
        String table = "user_data"; // Table name
        File file = new File("/home/isuru/Desktop/IAM/Repositories/wso2-IS-custom-listener/src/main/resources/reference.conf");
        
        DriverConfigLoader loader = DriverConfigLoader.fromFile(file);
        System.out.println("Connecting to Cassandra...");

        // Establishing connection to Cassandra
        try (CqlSession session = new CqlSessionBuilder()
                .addContactPoint(new InetSocketAddress(contactPoint, port))
                .withLocalDatacenter("datacenter1") // Adjust to your local datacenter name
                .withConfigLoader(loader)
                .build()) {
            System.out.println("Connected to Cassandra.");
            
            String query = String.format("SELECT * FROM %s.%s", keyspace, table);

            while(true){
                ResultSet resultSet = session.execute(query);

                // Log the data
                for (Row row : resultSet) {
                    // Access individual columns and print their values
                    String userId = row.getString("user_id");
                    String userName = row.getString("user_name");
                    System.out.printf("User ID: %s, User Name: %s", userId, userName);
                    System.out.println("");
                }
    
                // Sleep for 2 seconds
                System.out.println(".");
                System.out.println("..");
                Thread.sleep(2000);   
                System.out.println("Sleept for 2 seconds");  
                System.out.println("");

            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
    public static void main(String[] args) {
        // // Connect to Cassandra
        // try (CqlSession session = getSession()) {
        //     while (true) {
        //         // Execute the query
        //         ResultSet resultSet = session.execute(SELECT_QUERY);
                
        //         // Log the data
        //         for (Row row : resultSet) {
        //             System.out.println(row);
        //         }

        //         // Sleep for 2 seconds
        //         Thread.sleep(2000);
        //     }
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        read();
    }

    // private static CqlSession getSession() {
    //     // Connect to local Cassandra instance
    //     CqlSessionBuilder builder = CqlSession.builder();
    //     builder.addContactPoint(new InetSocketAddress("localhost", 9042));
    //     builder.withKeyspace(KEYSPACE_NAME);
    //     builder.withLocalDatacenter("datacenter1");
    //     builder.withConnectTimeout(Duration.ofSeconds(10));
    //     return builder.build();
    // }
}