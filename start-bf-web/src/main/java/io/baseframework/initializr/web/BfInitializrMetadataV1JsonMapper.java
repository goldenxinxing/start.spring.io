package io.baseframework.initializr.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.baseframework.initializr.metadata.BfInitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.web.mapper.InitializrMetadataV21JsonMapper;

/**
 * @Package: io.baseframework.initializr.web<br>
 * @ClassName: BfInitializrMetadataV1JsonMapper.java<br>
 * @Description: TODO
 * @author: gaoxinxing
 */
public class BfInitializrMetadataV1JsonMapper extends InitializrMetadataV21JsonMapper {

	public BfInitializrMetadataV1JsonMapper() {
	}

	@Override
	public String write(InitializrMetadata metadata, String appUrl) {
		ObjectNode delegate = nodeFactory().objectNode();
		this.links(delegate, metadata.getTypes().getContent(), appUrl);
		this.dependencies(delegate, metadata.getDependencies());
		this.type(delegate, metadata.getTypes());
		this.singleSelect(delegate, metadata.getPackagings());
		this.singleSelect(delegate, metadata.getJavaVersions());
		this.singleSelect(delegate, metadata.getLanguages());
		// 需要根据bfVersion决定bootversion
		if (metadata instanceof BfInitializrMetadata) {
			this.dependencies(delegate, ((BfInitializrMetadata) metadata).getBfDependencies());
			this.singleSelect(delegate, ((BfInitializrMetadata) metadata).getBfVersions());
		}
		this.singleSelect(delegate, metadata.getBootVersions());
		this.text(delegate, metadata.getGroupId());
		this.text(delegate, metadata.getArtifactId());
		this.text(delegate, metadata.getVersion());
		this.text(delegate, metadata.getName());
		this.text(delegate, metadata.getDescription());
		this.text(delegate, metadata.getPackageName());
		return delegate.toString();
	}

}
