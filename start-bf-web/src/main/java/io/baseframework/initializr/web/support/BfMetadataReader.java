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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.spring.initializr.metadata.DefaultMetadataElement;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads metadata from the main spring.io website. This is a stateful service: create a
 * new instance whenever you need to refresh the content.
 *
 * @author Stephane Nicoll
 */
class BfMetadataReader {

	private final JsonNode content;

	private List<DefaultMetadataElement> bootlist = new ArrayList<>();

	private List<DefaultMetadataElement> bflist = new ArrayList<>();

	public List<DefaultMetadataElement> getBootlist() {
		return bootlist;
	}

	public List<DefaultMetadataElement> getBflist() {
		return bflist;
	}

	/**
	 * Parse the content of the metadata at the specified url.
	 * @param objectMapper the object mapper
	 * @throws IOException on load error
	 */
	BfMetadataReader(ObjectMapper objectMapper) throws IOException {
		this.content = objectMapper.readTree(new File("version.xml"));
		ArrayNode releases = (ArrayNode) this.content.get("projectReleases");
		List<DefaultMetadataElement> list = new ArrayList<>();
		for (JsonNode node : releases) {
			// bf Version
			DefaultMetadataElement version = new DefaultMetadataElement();
			version.setId(node.get("version").textValue());
			String name = node.get("versionDisplayName").textValue();
			version.setName(node.get("snapshot").booleanValue() ? name + " (SNAPSHOT)" : name);
			version.setDefault(node.get("current").booleanValue());
			bflist.add(version);

			JsonNode bootNode = node.get("bootInfo");
			version.setId(bootNode.get("version").textValue());
			String bootname = bootNode.get("versionDisplayName").textValue();
			version.setName(bootNode.get("snapshot").booleanValue() ? bootname + " (SNAPSHOT)" : bootname);
			version.setDefault(bootNode.get("current").booleanValue());
			bootlist.add(version);
		}
	}

}
