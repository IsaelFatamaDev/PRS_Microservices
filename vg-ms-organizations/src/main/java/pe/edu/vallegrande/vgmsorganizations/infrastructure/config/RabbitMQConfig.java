package pe.edu.vallegrande.vgmsorganizations.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "organization.events";
    public static final String QUEUE_ORGANIZATION_CREATED = "organization.created.queue";
    public static final String QUEUE_ORGANIZATION_UPDATED = "organization.updated.queue";
    public static final String QUEUE_ORGANIZATION_DELETED = "organization.deleted.queue";
    public static final String QUEUE_ZONE_CREATED = "zone.created.queue";
    public static final String QUEUE_ZONE_FEE_CHANGED = "zone.fee.changed.queue";

    @Bean
    public Exchange organizationExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    @Bean
    public Queue organizationCreatedQueue() {
        return QueueBuilder
            .durable(QUEUE_ORGANIZATION_CREATED)
            .build();
    }

    @Bean
    public Queue organizationUpdatedQueue() {
        return QueueBuilder
            .durable(QUEUE_ORGANIZATION_UPDATED)
            .build();
    }

    @Bean
    public Queue organizationDeletedQueue() {
        return QueueBuilder
            .durable(QUEUE_ORGANIZATION_DELETED)
            .build();
    }

    @Bean
    public Queue zoneCreatedQueue() {
        return QueueBuilder
            .durable(QUEUE_ZONE_CREATED)
            .build();
    }

    @Bean
    public Queue zoneFeeChangedQueue() {
        return QueueBuilder
            .durable(QUEUE_ZONE_FEE_CHANGED)
            .build();
    }

    @Bean
    public Binding bindingOrganizationCreated() {
        return BindingBuilder
            .bind(organizationCreatedQueue())
            .to(organizationExchange())
            .with("organization.created")
            .noargs();
    }

    @Bean
    public Binding bindingOrganizationUpdated() {
        return BindingBuilder
            .bind(organizationUpdatedQueue())
            .to(organizationExchange())
            .with("organization.updated")
            .noargs();
    }

    @Bean
    public Binding bindingOrganizationDeleted() {
        return BindingBuilder
            .bind(organizationDeletedQueue())
            .to(organizationExchange())
            .with("organization.deleted")
            .noargs();
    }

    @Bean
    public Binding bindingZoneCreated() {
        return BindingBuilder
            .bind(zoneCreatedQueue())
            .to(organizationExchange())
            .with("zone.created")
            .noargs();
    }

    @Bean
    public Binding bindingZoneFeeChanged() {
        return BindingBuilder
            .bind(zoneFeeChangedQueue())
            .to(organizationExchange())
            .with("zone.fee.changed")
            .noargs();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
