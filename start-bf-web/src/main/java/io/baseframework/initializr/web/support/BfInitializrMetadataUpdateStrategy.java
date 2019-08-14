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

package io.baseframework.initializr.web.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.baseframework.initializr.metadata.BfInitializrMetadata;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * A {@link InitializrMetadataUpdateStrategy} that refreshes the metadata with the status
 * of the main spring.io site.
 *
 * @author Stephane Nicoll
 */
public class BfInitializrMetadataUpdateStrategy implements InitializrMetadataUpdateStrategy {

	private static final Log logger = LogFactory.getLog(BfInitializrMetadataUpdateStrategy.class);

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	public BfInitializrMetadataUpdateStrategy(RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public InitializrMetadata update(InitializrMetadata current) {
		BfMetadataReader bfMetadataReader = fetchVersionsReader();
		List<DefaultMetadataElement> bootVersions = bfMetadataReader.getBootlist();
		if (bootVersions != null && !bootVersions.isEmpty()) {
			if (bootVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
				// No default specified
				bootVersions.get(0).setDefault(true);
			}
			current.updateSpringBootVersions(bootVersions);
		}
		if (current instanceof BfInitializrMetadata) {
			List<DefaultMetadataElement> bfVersions = bfMetadataReader.getBflist();
			if (bfVersions != null && !bfVersions.isEmpty()) {
				if (bfVersions.stream().noneMatch(DefaultMetadataElement::isDefault)) {
					// No default specified
					bfVersions.get(0).setDefault(true);
				}
				((BfInitializrMetadata) current).updateBaseFrameworkVersions(bfVersions);
			}
		}
		return current;
	}

	/**
	 * Fetch the available Spring Boot versions using the specified service url.
	 * @return the spring boot versions metadata or {@code null} if it could not be
	 * retrieved
	 */
	protected BfMetadataReader fetchVersionsReader() {
		try {
			logger.info("Fetching Spring Boot metadata from local");
			return new BfMetadataReader(this.objectMapper);
		}
		catch (Exception ex) {
			logger.warn("Failed to fetch Spring Boot metadata", ex);
		}
		return null;
	}

}
