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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.spring.initializr.metadata.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of the initializr service.
 *
 * @author Stephane Nicoll
 */
@ConfigurationProperties(prefix = "initializr")
public class BfInitializrProperties extends InitializrProperties {

	/**
	 * Dependencies, organized in groups (i.e. themes).
	 */
	@JsonIgnore
	private final List<DependencyGroup> baseFrameworkDependencies = new ArrayList<>();

	/**
	 * Available Spring Boot versions.
	 */
	@JsonIgnore
	private final List<DefaultMetadataElement> baseFrameworkVersions = new ArrayList<>();

	public List<DependencyGroup> getBaseFrameworkDependencies() {
		return baseFrameworkDependencies;
	}

	public List<DefaultMetadataElement> getBaseFrameworkVersions() {
		return baseFrameworkVersions;
	}
}
