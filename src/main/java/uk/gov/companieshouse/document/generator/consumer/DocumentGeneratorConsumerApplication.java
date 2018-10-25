package uk.gov.companieshouse.document.generator.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.document.generator.consumer.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@SpringBootApplication
public class DocumentGeneratorConsumerApplication implements WebMvcConfigurer {

    public static final String APPLICATION_NAME_SPACE = "document-generator-consumer";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @Autowired
    TaskExecutor taskExecutor;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    public static void main(String[] args) {
        Integer port = Integer.getInteger("server.port");

        if (port == null) {
            LOGGER.error("Failed to start service, no port has been configured");
            System.exit(0);
        }

        SpringApplication.run(DocumentGeneratorConsumerApplication.class, args);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {

        registry.addInterceptor(loggingInterceptor);
    }
}

