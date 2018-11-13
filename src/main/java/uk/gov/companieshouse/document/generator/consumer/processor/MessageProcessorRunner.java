package uk.gov.companieshouse.document.generator.consumer.processor;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class MessageProcessorRunner implements Runnable {

    @Autowired
    private MessageProcessor messageProcessor;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private boolean active = true;
    private boolean processing = false;

    @Override
    public void run() {

        LOG.debug("Beginning processing of document generation requests");
        kafkaConsumerService.connect();

        try {
            while(isActive()) {
                try {
                    setProcessing(true);
                    messageProcessor.processKafkaMessage();
                } finally {
                    setProcessing(false);
                }
            }
        } catch(InterruptedException e) {
            LOG.error(e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Is message processor still processing
     *
     * @return boolean
     */
    public boolean isProcessing() {
        return processing;
    }

    /**
     * Set is processing flag
     *
     * @param processing
     */
    private void setProcessing(boolean processing) {
        this.processing = processing;
    }

    /**
     * Is message processor still active
     *
     * @return boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set is active flag
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
