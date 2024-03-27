package com.readagent;

import java.util.Map;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;
import org.wso2.carbon.user.api.RealmConfiguration;

public class CustomJDBCUserStoreManager extends UniqueIDJDBCUserStoreManager {
    
    public CustomJDBCUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {

        super(realmConfig, tenantId);
    }

    public User doAddUserWithCustomID(String UUID, String userName, Object credential, String[] roleList, Map<String, String> claims,
                        String profileName, boolean requirePasswordChange) throws UserStoreException {
            
                    // Assigning unique user ID of the user as the username in the system.
                    String userID = UUID;
                    // Assign username to the username claim.
                    claims = addUserNameAttribute(userName, claims);
                    // Assign userID to the userid claim.
                    claims = addUserIDAttribute(userID, claims);
                    persistUser(userID, userName, credential, roleList, claims, profileName, requirePasswordChange);
            
                    return getUser(userID, userName);
    }
}