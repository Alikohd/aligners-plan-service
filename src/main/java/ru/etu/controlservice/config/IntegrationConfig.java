package ru.etu.controlservice.config;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.PostgresChannelMessageStoreQueryProvider;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.store.MessageGroupQueue;
import org.springframework.integration.transaction.TransactionInterceptorBuilder;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
@EnableIntegration
public class IntegrationConfig {

    @Bean
    public MessageChannel tasksQueue(JdbcChannelMessageStore taskMessageStore) {
        return new QueueChannel(new MessageGroupQueue(taskMessageStore, "taskEventsQueue"));
    }

    @Bean
    public MessageChannel dlqChannel(JdbcChannelMessageStore dlqMessageStore) {
        return new QueueChannel(new MessageGroupQueue(dlqMessageStore, "dlqEventsQueue"));
    }

    @Bean
    public JdbcChannelMessageStore taskMessageStore(DataSource dataSource) {
        JdbcChannelMessageStore store = new JdbcChannelMessageStore(dataSource);
        store.setRegion("tasks");
        store.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return store;
    }

    @Bean
    public JdbcChannelMessageStore dlqMessageStore(DataSource dataSource) {
        JdbcChannelMessageStore store = new JdbcChannelMessageStore(dataSource);
        store.setRegion("dlq");
        store.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return store;
    }

    @Bean
    public Advice retryAdvice(MessageChannel dlqChannel) {
        RequestHandlerRetryAdvice advice = new RequestHandlerRetryAdvice();
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(5000); // 5 секунд между попытками
        retryTemplate.setBackOffPolicy(backOffPolicy);
        advice.setRetryTemplate(retryTemplate);
        advice.setRecoveryCallback(context -> {
            Message<?> failedMessage = ((MessagingException) context.getLastThrowable()).getFailedMessage();
            log.error("Retries exhausted, sending to DLQ: {}", failedMessage);
            assert failedMessage != null;
            dlqChannel.send(failedMessage); // Отправка в DLQ
            return null;
        });
        return advice;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PollerMetadata tasksPoller(PlatformTransactionManager transactionManager, TaskExecutor tasksExecutor) {
        PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(Duration.ofSeconds(3)));
        pollerMetadata.setTaskExecutor(tasksExecutor);
        TransactionInterceptor transactionInterceptor = new TransactionInterceptorBuilder()
                .transactionManager(transactionManager)
                .build();
        pollerMetadata.setAdviceChain(List.of(transactionInterceptor));
        return pollerMetadata;
    }
}
