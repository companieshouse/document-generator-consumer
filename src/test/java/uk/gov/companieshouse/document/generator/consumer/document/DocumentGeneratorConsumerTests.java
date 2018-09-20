package uk.gov.companieshouse.document.generator.consumer.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DocumentGeneratorConsumerTests {

    @InjectMocks
    private DocumentGeneratorConsumer documentGeneratorConsumer;

    @Test
    @DisplayName("Test polling of Kafka messages to request document generation")
    void pollAndGenerateDocumentTest() {

    }
}
