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

package io.baseframework.initializr.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.metadata.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link BfInitializrMetadata}. Allows to read metadata from any arbitrary
 * resource, including remote URLs.
 *
 * @author Stephane Nicoll
 * @see InitializrMetadataCustomizer
 */
public final class BfInitializrMetadataBuilder {

	private final List<InitializrMetadataCustomizer> customizers = new ArrayList<>();

	private final InitializrConfiguration configuration;

	private BfInitializrMetadataBuilder(InitializrConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Add a {@link BfInitializrProperties} to be merged with other content. Merges the
	 * settings only and not the configuration.
	 * @param properties the properties to use
	 * @return this instance
	 * @see #withBfInitializrProperties(BfInitializrProperties, boolean)
	 */
	public BfInitializrMetadataBuilder withBfInitializrProperties(BfInitializrProperties properties) {
		return withBfInitializrProperties(properties, false);
	}

	/**
	 * Add a {@link BfInitializrProperties} to be merged with other content.
	 * @param properties the settings to merge onto this instance
	 * @param mergeConfiguration specify if service configuration should be merged as well
	 * @return this instance
	 */
	public BfInitializrMetadataBuilder withBfInitializrProperties(BfInitializrProperties properties,
                                                              boolean mergeConfiguration) {
		if (mergeConfiguration) {
			this.configuration.merge(properties);
		}
		return withCustomizer(new InitializerPropertiesCustomizer(properties));
	}

	/**
	 * Add a {@link BfInitializrMetadata} to be merged with other content.
	 * @param resource a resource to a json document describing the metadata to include
	 * @return this instance
	 */
	public BfInitializrMetadataBuilder withInitializrMetadata(Resource resource) {
		return withCustomizer(new ResourceInitializrMetadataCustomizer(resource));
	}

	/**
	 * Add a {@link InitializrMetadataCustomizer}. customizers are invoked in their order
	 * of addition.
	 * @param customizer the customizer to add
	 * @return this instance
	 * @see InitializrMetadataCustomizer
	 */
	public BfInitializrMetadataBuilder withCustomizer(InitializrMetadataCustomizer customizer) {
		this.customizers.add(customizer);
		return this;
	}

	/**
	 * Build a {@link BfInitializrMetadata} based on the state of this builder.
	 * @return a new {@link BfInitializrMetadata} instance
	 */
	public BfInitializrMetadata build() {
		InitializrConfiguration config = (this.configuration != null) ? this.configuration
				: new InitializrConfiguration();
		BfInitializrMetadata metadata = createInstance(config);
		for (InitializrMetadataCustomizer customizer : this.customizers) {
			customizer.customize(metadata);
		}
		applyDefaults(metadata);
		metadata.validate();
		return metadata;
	}

	/**
	 * Creates an empty instance based on the specified {@link InitializrConfiguration}.
	 * @param configuration the configuration
	 * @return a new {@link InitializrMetadata} instance
	 */
	protected BfInitializrMetadata createInstance(InitializrConfiguration configuration) {
		return new BfInitializrMetadata(configuration);
	}

	/**
	 * Apply defaults to capabilities that have no value.
	 * @param metadata the initializr metadata
	 */
	protected void applyDefaults(InitializrMetadata metadata) {
		if (!StringUtils.hasText(metadata.getName().getContent())) {
			metadata.getName().setContent("demo");
		}
		if (!StringUtils.hasText(metadata.getDescription().getContent())) {
			metadata.getDescription().setContent("Demo project for Spring Boot");
		}
		if (!StringUtils.hasText(metadata.getGroupId().getContent())) {
			metadata.getGroupId().setContent("com.example");
		}
		if (!StringUtils.hasText(metadata.getVersion().getContent())) {
			metadata.getVersion().setContent("0.0.1-SNAPSHOT");
		}
	}

	/**
	 * Create a builder instance from the specified {@link BfInitializrProperties}.
	 * Initialize the configuration to use.
	 * @param configuration the configuration to use
	 * @return a new {@link InitializrMetadataBuilder} instance
	 * @see #withBfInitializrProperties(BfInitializrProperties)
	 */
	public static BfInitializrMetadataBuilder fromBfInitializrProperties(BfInitializrProperties configuration) {
		return new BfInitializrMetadataBuilder(configuration).withBfInitializrProperties(configuration);
	}

	/**
	 * Create an empty builder instance with a default {@link InitializrConfiguration}.
	 * @return a new {@link InitializrMetadataBuilder} instance
	 */
	public static BfInitializrMetadataBuilder create() {
		return new BfInitializrMetadataBuilder(new InitializrConfiguration());
	}

	private static class InitializerPropertiesCustomizer implements InitializrMetadataCustomizer {

		private final BfInitializrProperties properties;

		InitializerPropertiesCustomizer(BfInitializrProperties properties) {
			this.properties = properties;
		}

		@Override
		public void customize(InitializrMetadata metadata) {
			metadata.getDependencies().merge(this.properties.getDependencies());
			metadata.getTypes().merge(this.properties.getTypes());
			metadata.getBootVersions().merge(this.properties.getBootVersions());
			metadata.getPackagings().merge(this.properties.getPackagings());
			metadata.getJavaVersions().merge(this.properties.getJavaVersions());
			metadata.getLanguages().merge(this.properties.getLanguages());
			this.properties.getGroupId().apply(metadata.getGroupId());
			this.properties.getArtifactId().apply(metadata.getArtifactId());
			this.properties.getVersion().apply(metadata.getVersion());
			this.properties.getName().apply(metadata.getName());
			this.properties.getDescription().apply(metadata.getDescription());
			this.properties.getPackageName().apply(metadata.getPackageName());
		}

	}

	private static class ResourceInitializrMetadataCustomizer implements InitializrMetadataCustomizer {

		private static final Log logger = LogFactory.getLog(ResourceInitializrMetadataCustomizer.class);

		private static final Charset UTF_8 = Charset.forName("UTF-8");

		private final Resource resource;

		ResourceInitializrMetadataCustomizer(Resource resource) {
			this.resource = resource;
		}

		@Override
		public void customize(InitializrMetadata metadata) {
			logger.info("Loading initializr metadata from " + this.resource);
			try (InputStream in = this.resource.getInputStream()) {
				String content = StreamUtils.copyToString(in, UTF_8);
				ObjectMapper objectMapper = new ObjectMapper();
				BfInitializrMetadata anotherMetadata = objectMapper.readValue(content, BfInitializrMetadata.class);
				metadata.merge(anotherMetadata);
			}
			catch (Exception ex) {
				throw new IllegalStateException("Cannot merge", ex);
			}
		}

	}

}
