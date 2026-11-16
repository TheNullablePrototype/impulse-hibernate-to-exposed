package ru.prototype.meetup.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager

@Configuration(proxyBeanMethods = false)
class JpaConfig {

    /**
     * Exposed-стартер регистрирует свой `springTransactionManager`, и из-за этого автоконфигурация JPA
     * не создаёт `transactionManager` (она срабатывает только если менеджеров транзакций ещё нет).
     * Объявляем JPA-менеджер явно и делаем его основным: обычный `@Transactional` и Spring Data
     * репозитории используют его, а Exposed-код указывает `@Transactional(transactionManager = "springTransactionManager")`.
     */
    @Bean
    @Primary
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager(entityManagerFactory)
}
