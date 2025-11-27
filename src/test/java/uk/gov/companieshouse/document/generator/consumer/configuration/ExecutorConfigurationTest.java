package uk.gov.companieshouse.document.generator.consumer.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessorRunner;

@ExtendWith(MockitoExtension.class)
public class ExecutorConfigurationTest {

    @Mock
    MessageProcessorRunner messageProcessorRunner;

    private ExecutorConfiguration executorConfiguration;

    @BeforeEach
    void setup() {
        executorConfiguration = new ExecutorConfiguration(messageProcessorRunner);
    }

    @Test
    @DisplayName("Get the bean for Task Executor")
    void getBeanForTaskExecutor() {
        TaskExecutor taskExecutorBean = executorConfiguration.taskExecutor();
        assertNotNull(taskExecutorBean);
    }

    @Test
    @DisplayName("Get the bean for CommandLineRunner")
    void getBeanForCommandLineRunner() {
        TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        CommandLineRunner commandLineRunnerBean = executorConfiguration.schedulingRunner(taskExecutor);
        assertNotNull(commandLineRunnerBean);
    }
}
