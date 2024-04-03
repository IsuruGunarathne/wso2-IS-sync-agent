package com.readagent;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.internal.core.SystemBundleActivator;

import com.readagent.internal.SyncToolServiceDataHolder;

import groovy.transform.builder.InitializerStrategy.SET;

import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Map;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;

import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.inmemory.InMemoryClaimManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.UserRealm;

import com.readagent.CustomJDBCUserStoreManager;

public class Readagent {
    private static final Log log = LogFactory.getLog(Readagent.class);
    private static CustomJDBCUserStoreManager jdbcUserStoreManager;
    private static RealmService realmService;

    public static void init() {
        realmService = SyncToolServiceDataHolder.getInstance().getRealmService();
        try{

            if(jdbcUserStoreManager==null){
                // realmService.getTenantUserRealm(-1234).getRealmConfiguration().getUserStoreProperties().put("dataSource", "jdbc/SHARED_DB");

                RealmConfiguration realmConfig = realmService.getTenantUserRealm(-1234).getRealmConfiguration();
                Map<String, Object> properties = new HashMap<String, Object>();
                ClaimManager claimManager = new InMemoryClaimManager();
                UserRealm realm = (UserRealm) realmService.getTenantUserRealm(-1234);
                Integer tenantId = new Integer(realmService.getTenantManager().getTenantId("carbon.super"));

                jdbcUserStoreManager = new CustomJDBCUserStoreManager(realmConfig, properties, claimManager, null, realm, tenantId);
                System.out.println("Realm Service: "+realmService.getTenantManager().getTenantId("carbon.super"));
                System.out.println("Tenant User Realm: "+realmService.getTenantUserRealm(-1234).getRealmConfiguration());
                System.out.println(jdbcUserStoreManager.getClaimManager());
            }
        }catch(Exception e){
            System.out.println("Error creating JDBCUserStoreManager: "+e.getMessage());
            e.printStackTrace();
        }   
    }

    public static CqlSession connectToCassandra(Dotenv dotenv) {

        // Load environment variables from .env file
        String cassandraHost = dotenv.get("COSMOS_CONTACT_POINT");
        int cassandraPort = Integer.parseInt(dotenv.get("COSMOS_PORT"));

        String cassandraUsername = dotenv.get("COSMOS_USER_NAME");
        String cassandraPassword = dotenv.get("COSMOS_PASSWORD");   
        String region = dotenv.get("COSMOS_REGION");     
        String ref_path = dotenv.get("COSMOS_REF_PATH");

        // put the absolute path to the reference.conf file here
        File file = new File(ref_path);
        // print the content of the file
        System.out.println("File path: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        DriverConfigLoader loader = DriverConfigLoader.fromFile(file);

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
        .withConfigLoader(loader)
        .withAuthCredentials(cassandraUsername, cassandraPassword).build();

        System.out.println("Creating session: " + session.getName());
        return session;
    }

    public static void writeToDB(ResultSet resultSet) {
        for (Row row : resultSet) {

            String user_id = row.getString("user_id");
            String username = row.getString("username");
            String credential = row.getString("credential");
            String[] role_list = row.getSet("role_list", String.class).toArray(new String[0]);
            Map <String, String> claimsMap = row.getMap("claims", String.class, String.class);
            String claims = row.getMap("claims", String.class, String.class).toString();
            String profile = row.getString("profile");
            boolean central_us = row.getBoolean("central_us");
            boolean east_us = row.getBoolean("east_us");
            
            System.out.println();
            System.out.println();

            System.out.println("User ID: " + user_id);
            System.out.println("Username: " + username);
            System.out.println("Credential: " + credential);
            System.out.println("Role List: " + role_list);
            System.out.println("claimsMap: " + claimsMap);
            System.out.println("Claims: " + claims);
            System.out.println("Profile: " + profile);
            System.out.println("Central US: " + central_us);
            System.out.println("East US: " + east_us);
            
            System.out.println();

            try {
                if (!jdbcUserStoreManager.doCheckExistingUserWithID(user_id)) {
                    System.out.println("User does not exist in the system. Adding user...");
                    jdbcUserStoreManager.doAddUserWithCustomID(user_id, username, credential, role_list, claimsMap, profile, false);
                } else {
                    System.out.println("User already exists in the system...");
                }
            } catch (Exception e) {
                System.out.println("Error adding user: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void updateRoles(ResultSet resultSet) {
        for (Row row : resultSet) {
            String user_id = row.getString("user_id");
            // make role_list from the field role_name, which is a string
            String[] role_list = row.getString("role_name").split(",");


            // empty role list
            String [] empty_role_list = new String[0];
            System.out.println("User ID: " + user_id);
            System.out.println("Role List: " + role_list);
            try {
                jdbcUserStoreManager.doUpdateRoleListOfUserWithID(user_id, empty_role_list, role_list);
            } catch (Exception e) {
                System.out.println("Error updating roles: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void printRoles(ResultSet resultSet) {
        for (Row row : resultSet) {
            String user_id = row.getString("user_id");
            String[] role_list = row.getString("role_name").split(",");
            System.out.println("User ID: " + user_id);
            System.out.println("Role List: " + role_list[0]);          
        }
    }

    public static void read() {
        Dotenv dotenv = Dotenv.load();

        String keyspace = dotenv.get("CASSANDRA_KEYSPACE");
        String table = dotenv.get("CASSANDRA_TABLE");
        String region = dotenv.get("COSMOS_REGION");
        String role_table = dotenv.get("CASSANDRA_ROLE_TABLE");
        
        // set a variable to boolean false if region is central_us
        boolean central_us;
        if (region.equals("Central US")) {
            central_us = false;
        } else {
            central_us = true;
        }

        try (CqlSession session = connectToCassandra(dotenv)){
            log.info("Connected to Cassandra. Through Read Agent");

            String query = String.format("SELECT * FROM %s.%s WHERE central_us = %s ALLOW FILTERING;", keyspace, table, central_us);
            String role_query = String.format("SELECT * FROM %s.%s WHERE central_us = %s ALLOW FILTERING;", keyspace, role_table, true);
            // update the region to centra_us variable for production use
            while (true) {
                ResultSet resultSet = session.execute(query);
                writeToDB(resultSet);
                ResultSet roleResultSet = session.execute(role_query);
                // updateRoles(roleResultSet);
                printRoles(roleResultSet);
                Thread.sleep(1000);
                log.info("");
                log.info("Reading data from Cassandra...");
                log.info("");
                log.info("Read data from Cassandra");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }
   

    public static void main(String[] args) {
        System.out.println("Starting Read Agent...");
        read();
    }

}