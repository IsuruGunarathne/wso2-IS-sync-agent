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
    private static RealmService realmService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(Readagent.class.getName(), new Readagent(), null);
        log.info("Readagent bundle is activated");
        log.info("-------------------------------------");
        log.info("-------------------------------------");
        
        // Execute Readagent.read() in a separate thread
        executorService.execute(() -> {
            Readagent.read();
        });
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        executorService.shutdown(); // Shutdown the executor service when the component is deactivated
    }
    
}
