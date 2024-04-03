package com.readagent.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.user.core.service.RealmService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.readagent.Readagent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component(
    name = "com.readagent",
    immediate = true
)
public class ReadagentServiceComponent {

    private static final Log log = LogFactory.getLog(Readagent.class);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(Readagent.class.getName(), new Readagent(), null);
        log.info("Readagent bundle is activated");
        log.info("-------------------------------------");
        log.info("-------------------------------------");
        
        Readagent.init();
        // Execute Readagent.read() in a separate thread
        executorService.execute(() -> {
            // sleep for 1 minutes
            try {
                Thread.sleep(45000);
            } catch (InterruptedException e) {
                log.error("Error while sleeping the thread", e);
            }
            Readagent.read();
        });
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        executorService.shutdown(); // Shutdown the executor service when the component is deactivated
    }
    
    @Reference(
        name = "user.realmservice.default",
        service = RealmService.class,
        cardinality = ReferenceCardinality.MANDATORY,
        policy = ReferencePolicy.DYNAMIC,
        unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {
        SyncToolServiceDataHolder.getInstance().setRealmService(realmService);;
    }

    protected void unsetRealmService(RealmService realmService) {
        SyncToolServiceDataHolder.getInstance().setRealmService(null);
    }
    
    public static RealmService getRealmService() {
        return SyncToolServiceDataHolder.getInstance().getRealmService();
    }
}
