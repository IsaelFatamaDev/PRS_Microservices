package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

     @Value("${rabbitmq.exchange.notifications:notifications.exchange}")
     private String notificationsExchange;

     @Value("${rabbitmq.queue.user-created:user.created.queue}")
     private String userCreatedQueue;

     @Value("${rabbitmq.queue.payment-completed:payment.completed.queue}")
     private String paymentCompletedQueue;

     @Value("${rabbitmq.queue.payment-overdue:payment.overdue.queue}")
     private String paymentOverdueQueue;

     // Exchange para publicar eventos de notificaciones
     @Bean
     public TopicExchange notificationsExchange() {
          return new TopicExchange(notificationsExchange);
     }

     // Colas para consumir eventos de otros microservicios
     @Bean
     public Queue userCreatedQueue() {
          return new Queue(userCreatedQueue, true);
     }

     @Bean
     public Queue paymentCompletedQueue() {
          return new Queue(paymentCompletedQueue, true);
     }

     @Bean
     public Queue paymentOverdueQueue() {
          return new Queue(paymentOverdueQueue, true);
     }

     // Bindings (asumir que hay exchange 'users.exchange' y 'payments.exchange' en
     // otros MS)
     @Bean
     public Binding userCreatedBinding() {
          return BindingBuilder
                    .bind(userCreatedQueue())
                    .to(new TopicExchange("users.exchange"))
                    .with("user.created");
     }

     @Bean
     public Binding paymentCompletedBinding() {
          return BindingBuilder
                    .bind(paymentCompletedQueue())
                    .to(new TopicExchange("payments.exchange"))
                    .with("payment.completed");
     }

     @Bean
     public Binding paymentOverdueBinding() {
          return BindingBuilder
                    .bind(paymentOverdueQueue())
                    .to(new TopicExchange("payments.exchange"))
                    .with("payment.overdue");
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
