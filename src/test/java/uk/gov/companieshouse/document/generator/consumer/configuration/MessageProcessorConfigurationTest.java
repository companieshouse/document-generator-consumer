package uk.gov.companieshouse.document.generator.consumer.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessorRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MessageProcessorConfigurationTest {

    @Test
    @DisplayName("Get the bean for MessageProcessorRunner")
    void getBeanForMessageProcessorRunner() {
        MessageProcessorConfiguration messageProcessorConfiguration = new MessageProcessorConfiguration();
        MessageProcessorRunner messageProcessorBean = messageProcessorConfiguration.messageProcessorRunner();

        assertNotNull(messageProcessorBean);
    }
}
