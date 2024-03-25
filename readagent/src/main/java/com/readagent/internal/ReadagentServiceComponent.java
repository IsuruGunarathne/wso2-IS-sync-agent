package com.readagent.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.user.core.service.RealmService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.readagent.Readagent;

@Component(
    name = "com.readagent",
    immediate = true
)
public class ReadagentServiceComponent {

    private static final Log log = LogFactory.getLog(Readagent.class);
    private static RealmService realmService;

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(Readagent.class.getName(), new Readagent(), null);
        log.info("Readagent bundle is activated");
        log.info("-------------------------------------");
        log.info("-------------------------------------");
    }
    
}
