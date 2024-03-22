package com.readagent;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import groovy.transform.builder.InitializerStrategy.SET;

import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    public static CqlSession connectToCassandra(Dotenv dotenv) {

        // Load environment variables from .env file
        String cassandraHost = dotenv.get("COSMOS_CONTACT_POINT");
        int cassandraPort = Integer.parseInt(dotenv.get("COSMOS_PORT"));

        String cassandraUsername = dotenv.get("COSMOS_USER_NAME");
        String cassandraPassword = dotenv.get("COSMOS_PASSWORD");   
        String region = dotenv.get("COSMOS_REGION");     

        SSLContext sc = null;
        try{

            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(null, null);

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);

            sc = SSLContext.getInstance("TLSv1.2");
            sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        }
        catch (Exception e) {
            System.out.println("Error creating keystore");
            e.printStackTrace();
        } 

        CqlSession session = CqlSession.builder().withSslContext(sc)
        .addContactPoint(new InetSocketAddress(cassandraHost, cassandraPort)).withLocalDatacenter(region)
        .withAuthCredentials(cassandraUsername, cassandraPassword).build();

        System.out.println("Creating session: " + session.getName());
        return session;
    }

    public static void printData(ResultSet resultSet) {
        for (Row row : resultSet) {
            // // Access individual columns and print their values
            // CREATE TABLE IF NOT EXISTS users (
            // user_id TEXT PRIMARY KEY, 
            // username TEXT,
            // credential TEXT,
            // role_list SET<TEXT>,
            // claims MAP<TEXT, TEXT>,
            // profile TEXT,
            // central_us BOOLEAN,
            // east_us BOOLEAN,);
            // above is the schema of the table

            String user_id = row.getString("user_id");
            String username = row.getString("username");
            String credential = row.getString("credential");
            String role_list = row.getSet("role_list", String.class).toString();
            String claims = row.getMap("claims", String.class, String.class).toString();
            String profile = row.getString("profile");
            boolean central_us = row.getBoolean("central_us");
            boolean east_us = row.getBoolean("east_us");

            System.out.printf("User ID: %s, Username: %s, Credential: %s, Role List: %s, Claims: %s, Profile: %s, Central US: %s, East US: %s\n",
                    user_id, username, credential, role_list, claims, profile, central_us, east_us);

            System.out.println();
        }
    }

    public static void read() {
        Dotenv dotenv = Dotenv.load();

        String keyspace = dotenv.get("CASSANDRA_KEYSPACE");
        String table = dotenv.get("CASSANDRA_TABLE");

        try (CqlSession session = connectToCassandra(dotenv)){
            System.out.println("Connected to Cassandra.");

            String query = String.format("SELECT * FROM %s.%s WHERE central_us = false ALLOW FILTERING;", keyspace, table);

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