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

import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.MetadataElement;

/**
 * A {@link MetadataElement} that specifies if its the default for a given capability.
 *
 * @author Stephane Nicoll
 */
public class BfMetadataElement extends DefaultMetadataElement {

	private DefaultMetadataElement bindVersion;

	public BfMetadataElement() {
	}

	public BfMetadataElement(String id, String name, boolean defaultValue) {
		super(id, name, defaultValue);
	}

	public BfMetadataElement(String id, boolean defaultValue) {
		this(id, null, defaultValue);
	}

	public DefaultMetadataElement getBindVersion() {
		return bindVersion;
	}

	public void setBindVersion(DefaultMetadataElement bindVersion) {
		this.bindVersion = bindVersion;
	}

}
