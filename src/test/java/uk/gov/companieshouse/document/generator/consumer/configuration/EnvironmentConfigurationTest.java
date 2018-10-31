package uk.gov.companieshouse.document.generator.consumer.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.environment.EnvironmentReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnvironmentConfigurationTest {

    private EnvironmentConfiguration environmentConfiguration;

    @BeforeEach
    void setup() {
        environmentConfiguration = new EnvironmentConfiguration();
    }

    @Test
    @DisplayName("Get the bean for EnvironmentReader")
    void getBeanForEnvironmentReader() {
        EnvironmentReader environmentReaderBean = environmentConfiguration.environmentReader();
        assertNotNull(environmentReaderBean);
    }

    @Test
    @DisplayName("Get the bean for RestTemplate")
    void getBeanForRestTemplate() {
        RestTemplate restTemplateBean = environmentConfiguration.restTemplate();
        assertNotNull(restTemplateBean);
    }
}