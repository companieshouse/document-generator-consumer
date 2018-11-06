package uk.gov.companieshouse.document.generator.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.document.generator.consumer.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessorRunner;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DocumentGeneratorConsumerApplication implements WebMvcConfigurer {

    public static final String APPLICATION_NAME_SPACE = "document-generator-consumer";

    public static final String RESOURCE_URI = "resource_uri";

    public static final String RESOURCE_ID = "resource_id";

    public static final String CONSUMER_TOPIC = "CONSUMER_TOPIC";

    public static final String GROUP_NAME = "GROUP_NAME";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    private static EnvironmentReader reader;

    @Autowired
    private MessageProcessorRunner messageProcessorRunner;

    public static void main(String[] args) {

        reader = new EnvironmentReaderImpl();

        checkEnvironmentParams();

        Integer port = Integer.getInteger("server.port");

        if (port == null) {
            LOGGER.error("Failed to start service, no port has been configured");
            System.exit(0);
        }

        SpringApplication.run(DocumentGeneratorConsumerApplication.class, args);
    }

    /**
     * Check all expected environment variables are set
     */
    public static void checkEnvironmentParams() {

        checkParam(CONSUMER_TOPIC);
        checkParam(GROUP_NAME);
    }

    public static void checkParam(String param) {

        String paramValue = reader.getMandatoryString(param);

        if (paramValue != null && !paramValue.isEmpty()) {
            LOGGER.info("Environment variable " + param + " has value " + paramValue);
        } else {
            throw new RuntimeException("Environment variable " + param + " is not set - application will exit");
        }
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

        registry.addInterceptor(loggingInterceptor);
    }

    /**
     * Called when application shutdown is requested to ensure that the current processes are completed before exit.
     */
    @PreDestroy
    public void onExit() {

        LOGGER.info("Setting active flag in " + messageProcessorRunner.getClass() + " to trigger a graceful shutdown");
        messageProcessorRunner.setActive(false);

        while(messageProcessorRunner.isProcessing()) {
            LOGGER.info("Waiting for current processing to complete before shutting down...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", "InterruptionException");

                LOGGER.error(e, data);
            }
        }
        LOGGER.info("Closing Document Generator Consumer message processor");
    }
}

