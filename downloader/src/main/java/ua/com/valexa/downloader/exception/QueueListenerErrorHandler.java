package ua.com.valexa.downloader.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class QueueListenerErrorHandler implements RabbitListenerErrorHandler {


    @Override
    public Object handleError(Message message, org.springframework.messaging.Message<?> message1, ListenerExecutionFailedException e) throws Exception {
        log.error("Error in queue listener; Message = " + new String(message.getBody(), StandardCharsets.UTF_8).replaceAll("\n", " ").replaceAll("\r", " "));
        return null;
    }
}
