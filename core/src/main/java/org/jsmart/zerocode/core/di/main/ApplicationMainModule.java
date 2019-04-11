package org.jsmart.zerocode.core.di.main;


import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.jsmart.zerocode.core.di.module.GsonModule;
import org.jsmart.zerocode.core.di.module.HttpClientModule;
import org.jsmart.zerocode.core.di.module.ObjectMapperModule;
import org.jsmart.zerocode.core.di.module.PropertiesInjectorModule;
import org.jsmart.zerocode.core.engine.executor.JavaExecutor;
import org.jsmart.zerocode.core.engine.executor.JavaExecutorImpl;
import org.jsmart.zerocode.core.engine.executor.JsonServiceExecutor;
import org.jsmart.zerocode.core.engine.executor.JsonServiceExecutorImpl;
import org.jsmart.zerocode.core.engine.preprocessor.ZeroCodeExternalFileProcessor;
import org.jsmart.zerocode.core.engine.preprocessor.ZeroCodeExternalFileProcessorImpl;
import org.jsmart.zerocode.core.engine.preprocessor.ZeroCodeJsonTestProcesor;
import org.jsmart.zerocode.core.engine.preprocessor.ZeroCodeJsonTestProcesorImpl;
import org.jsmart.zerocode.core.report.ZeroCodeReportGenerator;
import org.jsmart.zerocode.core.report.ZeroCodeReportGeneratorImpl;
import org.jsmart.zerocode.core.runner.ZeroCodeMultiStepsScenarioRunner;
import org.jsmart.zerocode.core.runner.ZeroCodeMultiStepsScenarioRunnerImpl;

import java.util.Properties;
import java.util.logging.Logger;

import static org.jsmart.zerocode.core.di.PropertyKeys.*;

public class ApplicationMainModule extends AbstractModule {
    private static final Logger LOGGER = Logger.getLogger(ApplicationMainModule.class.getName());

    private final String serverEnv;

    public ApplicationMainModule(String serverEnv) {
        this.serverEnv = serverEnv;
    }

    @Override
    public void configure() {
        /*
         * Install other guice modules
         */
        install(new ObjectMapperModule());
        install(new HttpClientModule());
        install(new GsonModule());
        install(new PropertiesInjectorModule(serverEnv));
        //install(new KafkaModule());

        /*
         * Bind Direct classes, classes to interfaces etc
         */
        bind(ZeroCodeMultiStepsScenarioRunner.class).to(ZeroCodeMultiStepsScenarioRunnerImpl.class);
        bind(JsonServiceExecutor.class).to(JsonServiceExecutorImpl.class);
        bind(JavaExecutor.class).to(JavaExecutorImpl.class);
        bind(ZeroCodeJsonTestProcesor.class).to(ZeroCodeJsonTestProcesorImpl.class);
        bind(ZeroCodeReportGenerator.class).to(ZeroCodeReportGeneratorImpl.class);
        bind(ZeroCodeExternalFileProcessor.class).to(ZeroCodeExternalFileProcessorImpl.class);

        // ------------------------------------------------
        // Bind properties for localhost, CI, DIT, SIT etc
        // ------------------------------------------------
        Names.bindProperties(binder(), getProperties(serverEnv));
    }

    public Properties getProperties(String host) {
        final Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(host));

            // ----------------------------------------------------
            // Below code is for backward compatibility,
            // remove this after publishing for withdrawing support
            // ----------------------------------------------------
            checkAndLoadOldProperties(properties);

        } catch (Exception e) {
            LOGGER.info("###Oops!Exception### while reading target env file: " + host + ". Have you mentioned env details?");
            throw new RuntimeException("could not read the target-env properties file --" + host + "-- from the classpath.");
        }

        return properties;
    }

    private void checkAndLoadOldProperties(Properties properties) {

        if(properties.get(WEB_APPLICATION_ENDPOINT_HOST) == null && properties.get(RESTFUL_APPLICATION_ENDPOINT_HOST) != null){
            Object oldPropertyValue = properties.get(RESTFUL_APPLICATION_ENDPOINT_HOST);
            properties.setProperty(WEB_APPLICATION_ENDPOINT_HOST, oldPropertyValue != null ? oldPropertyValue.toString() : null);
        }

        if(properties.get(WEB_APPLICATION_ENDPOINT_PORT) == null && properties.get(RESTFUL_APPLICATION_ENDPOINT_PORT) != null){
            Object oldPropertyValue = properties.get(RESTFUL_APPLICATION_ENDPOINT_PORT);
            properties.setProperty(WEB_APPLICATION_ENDPOINT_PORT, oldPropertyValue != null ? oldPropertyValue.toString() : null);
        }

        if(properties.get(WEB_APPLICATION_ENDPOINT_CONTEXT) == null && properties.get(RESTFUL_APPLICATION_ENDPOINT_CONTEXT) != null){
            Object oldPropertyValue = properties.get(RESTFUL_APPLICATION_ENDPOINT_CONTEXT);
            properties.setProperty(WEB_APPLICATION_ENDPOINT_CONTEXT, oldPropertyValue != null ? oldPropertyValue.toString() : null);
        }

    }

}
