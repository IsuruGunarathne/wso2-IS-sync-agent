package com.readagent.internal;

import org.wso2.carbon.user.core.service.RealmService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SyncToolServiceDataHolder {

    private static final Log log = LogFactory.getLog(SyncToolServiceDataHolder.class);
    private static final SyncToolServiceDataHolder instance = new SyncToolServiceDataHolder();
    private RealmService realmService = null;

    private SyncToolServiceDataHolder() {
    }
    
    public static SyncToolServiceDataHolder getInstance() {
        return instance;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        log.info("Setting the Realm Service");
        this.realmService = realmService;
    }

    public void unsetRealmService(RealmService realmService) {
        log.info("Unsetting the Realm Service");
        this.realmService = null;
    }
}
