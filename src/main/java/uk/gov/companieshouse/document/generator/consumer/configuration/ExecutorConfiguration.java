package uk.gov.companieshouse.document.generator.consumer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessorRunner;

/**
 * Configuration for executor class - called once application is
 * running, to start the message processor
 */
@Configuration
public class ExecutorConfiguration {

    private final MessageProcessorRunner messageProcessorRunner;

    public ExecutorConfiguration(final MessageProcessorRunner newRunner) {
        this.messageProcessorRunner = newRunner;
    }

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public CommandLineRunner schedulingRunner(@Qualifier("taskExecutor") TaskExecutor executor) {
        return args -> executor.execute(messageProcessorRunner);
    }

}
