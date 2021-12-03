import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.apicurio.registry.utils.serde.AbstractKafkaStrategyAwareSerDe;
import io.apicurio.registry.utils.serde.AvroKafkaSerializer;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;


public class MessageConsumer {

    private static final String USERNAME = "REDACTED";
    private static final String PASSWORD = "REDACTED";

    private static final String TOPIC_NAME = "archdemo.public.demo";
    private static final String GROUP_ID = "dale";


    private static Properties prepareConfig() {
        Properties props = new Properties();

        props.put("bootstrap.servers", "es-kafka-bootstrap-eventstreams.clustername.eu-gb.containers.appdomain.cloud:443");
        props.put("group.id", GROUP_ID);
        props.put("client.id", "demoapp");
        props.put("ssl.truststore.location", "./es-cert.p12");
        props.put("ssl.truststore.password", "REDACTED");
        props.put(AbstractKafkaStrategyAwareSerDe.REGISTRY_REQUEST_TRUSTSTORE_LOCATION, "./es-cert.p12");
        props.put(AbstractKafkaStrategyAwareSerDe.REGISTRY_REQUEST_TRUSTSTORE_PASSWORD, "REDACTED");
        props.put("ssl.truststore.type", "PKCS12");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "SCRAM-SHA-512");
        props.put("ssl.endpoint.identification.algorithm", "");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put("sasl.jaas.config",
            "org.apache.kafka.common.security.scram.ScramLoginModule " +
            "required " +
            "username=\"" + USERNAME + "\" " +
            "password=\"" + PASSWORD + "\";");
        props.put(AbstractKafkaStrategyAwareSerDe.REGISTRY_URL_CONFIG_PARAM, "https://REDACTED:REDACTED@es-ibm-es-ac-reg-external-eventstreams.clustername.eu-gb.containers.appdomain.cloud");
        props.put(AbstractKafkaStrategyAwareSerDe.REGISTRY_ARTIFACT_ID_STRATEGY_CONFIG_PARAM, "io.apicurio.registry.utils.serde.strategy.GetOrCreateIdStrategy");
        props.put("key.deserializer", "io.apicurio.registry.utils.serde.AvroKafkaDeserializer");
        props.put("value.deserializer", "io.apicurio.registry.utils.serde.AvroKafkaDeserializer");
        return props;
    }

    public static void main(String[] args) {
        Properties props = prepareConfig();
        try {
            KafkaConsumer<GenericRecord, GenericRecord> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(TOPIC_NAME));
            System.out.println("==============================================");
            while (true) {
                ConsumerRecords<GenericRecord, GenericRecord> records = consumer.poll(Duration.ofSeconds(10));
                for (ConsumerRecord<GenericRecord, GenericRecord> record : records) {
                    GenericRecord genericRecord = record.value();
                    if (genericRecord != null) {
                        System.out.println("operation  :  " + genericRecord.get("op"));
                        System.out.println("source     :  " + genericRecord.get("source"));
                        System.out.println("");
                        if (genericRecord.get("op").toString().equals("d")) {
                            System.out.println(genericRecord.get("before"));
                        }
                        else {
                            System.out.println(genericRecord.get("after"));
                        }
                        System.out.println("==============================================");
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
