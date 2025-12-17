package uk.gov.companieshouse.document.generator.consumer.integration;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

    @Test
    void givenMessagePublished_whenConsumerReceives_thenProcessedSuccessfully() throws SerializationException, DeserializationException {
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

        await().atMost(Duration.ofSeconds(10))
                .until(() -> consumer.getLastMessage().isPresent());

        Optional<Message> lastMessage = consumer.getLastMessage();
        assertThat(lastMessage, notNullValue());
        assertThat(lastMessage.isPresent(), is(true));

        byte[] consumedMessage = lastMessage.get().getValue();
        assertThat(consumedMessage.length, is(71));

        RenderSubmittedDataDocument expectedData = deserializerFactory.getSpecificRecordDeserializer(RenderSubmittedDataDocument.class)
                .fromBinary(lastMessage.get(), RenderSubmittedDataDocument.getClassSchema());

        assertThat(expectedData.getId(), is(document.getId()));
    }
}
