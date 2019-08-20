/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.baseframework.initializr.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.baseframework.initializr.metadata.BfInitializrMetadataBuilder;
import io.baseframework.initializr.metadata.BfInitializrProperties;
import io.baseframework.initializr.web.project.BfMainController;
import io.baseframework.initializr.web.project.BfProjectGenerationInvoker;
import io.baseframework.initializr.web.project.BfProjectRequestToDescriptionConverter;
import io.baseframework.initializr.web.support.BfDependencyMetadataProvider;
import io.baseframework.initializr.web.support.BfInitializrMetadataProvider;
import io.baseframework.initializr.web.support.BfInitializrMetadataUpdateStrategy;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.io.SimpleIndentStrategy;
import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.io.template.TemplateRenderer;
import io.spring.initializr.generator.project.ProjectDirectoryFactory;
import io.spring.initializr.metadata.*;
import io.spring.initializr.web.autoconfigure.InitializrAutoConfiguration;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.nio.file.Files;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} to configure Spring initializr. In a web environment, configures
 * the necessary controller to serve the applications from the root context.
 *
 * @author Stephane Nicoll
 */
@Configuration
@EnableConfigurationProperties(BfInitializrProperties.class)
@AutoConfigureAfter({ JacksonAutoConfiguration.class, RestTemplateAutoConfiguration.class })
@AutoConfigureBefore(InitializrAutoConfiguration.class)
public class BfInitializrAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ProjectDirectoryFactory projectDirectoryFactory() {
		return (description) -> Files.createTempDirectory("project-");
	}

	@Bean
	@ConditionalOnMissingBean
	public IndentingWriterFactory indentingWriterFactory() {
		return IndentingWriterFactory.create(new SimpleIndentStrategy("\t"));
	}

	@Bean
	@ConditionalOnMissingBean(TemplateRenderer.class)
	public MustacheTemplateRenderer templateRenderer(Environment environment,
			ObjectProvider<CacheManager> cacheManager) {
		return new MustacheTemplateRenderer("classpath:/templates",
				determineCache(environment, cacheManager.getIfAvailable()));
	}

	private Cache determineCache(Environment environment, CacheManager cacheManager) {
		if (cacheManager != null) {
			Binder binder = Binder.get(environment);
			boolean cache = binder.bind("spring.mustache.cache", Boolean.class).orElse(true);
			if (cache) {
				return cacheManager.getCache("initializr.templates");
			}
		}
		return new NoOpCache("templates");
	}

	@Bean
	@ConditionalOnMissingBean
	public InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy(RestTemplateBuilder restTemplateBuilder,
			ObjectMapper objectMapper) {
		return new BfInitializrMetadataUpdateStrategy(restTemplateBuilder.build(), objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean(InitializrMetadataProvider.class)
	public InitializrMetadataProvider initializrMetadataProvider(BfInitializrProperties properties,
			InitializrMetadataUpdateStrategy initializrMetadataUpdateStrategy) {
		InitializrMetadata metadata = BfInitializrMetadataBuilder.fromBfInitializrProperties(properties).build();
		return new BfInitializrMetadataProvider(metadata, initializrMetadataUpdateStrategy);
	}

	@Bean
	@ConditionalOnMissingBean
	public DependencyMetadataProvider dependencyMetadataProvider() {
		return new BfDependencyMetadataProvider();
	}

	/**
	 * Initializr web configuration.
	 */
	@Configuration
	@ConditionalOnWebApplication
	static class InitializrWebConfiguration {

		/*
		 * @Bean InitializrWebConfig initializrWebConfig() { return new
		 * InitializrWebConfig(); }
		 */
		@Bean
		@ConditionalOnMissingBean
		BfMainController initializrBfMainController(InitializrMetadataProvider metadataProvider,
				TemplateRenderer templateRenderer, DependencyMetadataProvider dependencyMetadataProvider,
				BfProjectGenerationInvoker projectGenerationInvoker) {
			return new BfMainController(metadataProvider, templateRenderer, dependencyMetadataProvider,
					projectGenerationInvoker);
		}

		@Bean
		@ConditionalOnMissingBean
		BfProjectGenerationInvoker bfProjectGenerationInvoker(ApplicationContext applicationContext,
				ApplicationEventPublisher eventPublisher,
				BfProjectRequestToDescriptionConverter projectRequestToDescriptionConverter) {
			return new BfProjectGenerationInvoker(applicationContext, eventPublisher,
					projectRequestToDescriptionConverter);
		}

		@Bean
		BfProjectRequestToDescriptionConverter bfProjectRequestToDescriptionConverter() {
			return new BfProjectRequestToDescriptionConverter();
		}

		/*
		 * @Bean InitializrModule InitializrJacksonModule() { return new
		 * InitializrModule(); }
		 */

	}

	/**
	 * Initializr cache configuration.
	 */
	/*
	 * @Configuration
	 *
	 * @ConditionalOnClass(javax.cache.CacheManager.class) static class
	 * InitializrCacheConfiguration {
	 *
	 * @Bean JCacheManagerCustomizer initializrCacheManagerCustomizer() { return
	 * (cacheManager) -> { cacheManager.createCache("initializr.metadata",
	 * config().setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES)
	 * )); cacheManager.createCache("initializr.dependency-metadata", config());
	 * cacheManager.createCache("initializr.project-resources", config());
	 * cacheManager.createCache("initializr.templates", config()); }; }
	 *
	 * private MutableConfiguration<Object, Object> config() { return new
	 * MutableConfiguration<>().setStoreByValue(false).setManagementEnabled(true)
	 * .setStatisticsEnabled(true); }
	 *
	 * }
	 */

}
