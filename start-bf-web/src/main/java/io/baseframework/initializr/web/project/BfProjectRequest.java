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

package io.baseframework.initializr.web.project;

import io.spring.initializr.web.project.ProjectRequest;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The base settings of a project request. Only these can be bound by user's input.
 *
 * @author Stephane Nicoll
 */
public class BfProjectRequest extends ProjectRequest {

	public String getBaseFrameworkVersion() {
		return baseFrameworkVersion;
	}

	public void setBaseFrameworkVersion(String baseFrameworkVersion) {
		this.baseFrameworkVersion = baseFrameworkVersion;
	}

	private String baseFrameworkVersion;

}
