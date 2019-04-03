package net.brutus5000.deltaforge.server.config.elide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideSettingsBuilder;
import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.core.filter.dialect.CaseSensitivityStrategy;
import com.yahoo.elide.core.filter.dialect.RSQLFilterDialect;
import com.yahoo.elide.jsonapi.JsonApiMapper;
import com.yahoo.elide.security.checks.Check;
import com.yahoo.elide.utils.coerce.CoerceUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.hibernate.ScrollMode;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ElideConfig {

    @Bean
    public Elide elide(SpringHibernateDataStore springHibernateDataStore, ObjectMapper objectMapper, EntityDictionary entityDictionary) {
        RSQLFilterDialect rsqlFilterDialect = new RSQLFilterDialect(entityDictionary, new CaseSensitivityStrategy.UseColumnCollation());

        registerAdditionalConverters();

        return new Elide(new ElideSettingsBuilder(springHibernateDataStore)
                .withJsonApiMapper(new JsonApiMapper(objectMapper))
                .withEntityDictionary(entityDictionary)
                .withJoinFilterDialect(rsqlFilterDialect)
                .withSubqueryFilterDialect(rsqlFilterDialect)
                .build());
    }


    @Bean
    SpringHibernateDataStore springHibernateDataStore(PlatformTransactionManager txManager,
                                                      AutowireCapableBeanFactory beanFactory,
                                                      EntityManager entityManager) {
        return new SpringHibernateDataStore(txManager, beanFactory, entityManager, false, true, ScrollMode.FORWARD_ONLY);
    }

    /**
     * See https://github.com/yahoo/elide/issues/428.
     */
    private void registerAdditionalConverters() {
        CoerceUtil.coerce("", String.class);
        ConvertUtils.register(new Converter() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Class<T> type, Object value) {
                return (T) OffsetDateTime.parse(String.valueOf(value));
            }
        }, OffsetDateTime.class);
        ConvertUtils.register(new Converter() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Class<T> type, Object value) {
                return (T) Instant.parse(String.valueOf(value));
            }
        }, Instant.class);
        ConvertUtils.register(new Converter() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T convert(Class<T> type, Object value) {
                return (T) Duration.parse(String.valueOf(value));
            }
        }, Duration.class);
    }

    @Bean
    public EntityDictionary entityDictionary() {
        ConcurrentHashMap<String, Class<? extends Check>> checks = new ConcurrentHashMap<>();
//        checks.put(IsAuthenticated.EXPRESSION, IsAuthenticated.Inline.class);
        return new EntityDictionary(checks);
    }
}
