package ua.com.valexa.downloader.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${QUEUE_DOWNLOADER_NAME}")
    private String queueDownloaderName;

    @Value("${QUEUE_CPMS_NAME}")
    private String queueCpmsName;

    @Value("${QUEUE_SCHEDULER_RESPONSE_NAME}")
    private String queueSchedulerResponseName;



    @Bean(name = "queueDownloaderName")
    public String queueDownloaderName() {
        return queueDownloaderName;
    }

    @Bean(name = "queueCpmsName")
    public String queueCpmsName() {
        return queueCpmsName;
    }

    @Bean(name = "queueSchedulerResponseName")
    public String queueSchedulerResponseName() {
        return queueSchedulerResponseName;
    }

    @Bean
    public Queue requestQueue() {
        return new Queue(queueDownloaderName(), true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory){
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
