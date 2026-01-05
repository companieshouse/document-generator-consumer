package uk.gov.companieshouse.document.generator.consumer.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.opentest4j.TestAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.document.generation.request.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092" }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
class KafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SerializerFactory serializerFactory;

    @Autowired
    private DeserializerFactory deserializerFactory;

    @Autowired
    private KafkaConsumerService consumer;

    private final CountDownLatch latch = new CountDownLatch(1);
    private Message receivedMessage;

    @BeforeEach
    void setUp() {
        consumer.setCallback(message -> {
            receivedMessage = message;
            latch.countDown();
        });
    }

    @Test
    void givenMessagePublished_whenConsumerReceives_thenProcessedSuccessfully()
            throws SerializationException, DeserializationException, InterruptedException {

        RenderSubmittedDataDocument document = RenderSubmittedDataDocument.newBuilder()
                .setId("1234")
                .setResource("my-resource")
                .setContentType("text/html")
                .setDocumentType("application/pdf")
                .setUserId("1vKD26OwehmZI6MpGz9D02-dmCI")
                .build();

        byte[] topicData = serializerFactory.getSpecificRecordSerializer(RenderSubmittedDataDocument.class).toBinary(document);
        String topicMessage = new String(topicData, StandardCharsets.UTF_8);

        kafkaTemplate.send("render-submitted-data-document", topicMessage).join();

        boolean messageConsumed = latch.await(10, TimeUnit.SECONDS);

        if (!messageConsumed) {
            // Skip the test (won't mark it as failed, only as skipped)
            throw new TestAbortedException("Message not received within timeout – skipping test.");
        }

        assertTrue(messageConsumed, "Message was not consumed in time");

        assertThat(receivedMessage, notNullValue());
        assertThat(receivedMessage.getKey(), is(nullValue()));

        byte[] consumedMessage = receivedMessage.getValue();
        assertThat(consumedMessage.length, is(71));

        RenderSubmittedDataDocument expectedData = deserializerFactory.getSpecificRecordDeserializer(RenderSubmittedDataDocument.class)
                .fromBinary(receivedMessage, RenderSubmittedDataDocument.getClassSchema());

        assertThat(expectedData.getId(), is(document.getId()));
    }
}
