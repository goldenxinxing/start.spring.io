package io.baseframework.initializr.metadata;

import io.spring.initializr.generator.version.Version;
import io.spring.initializr.generator.version.VersionParser;
import io.spring.initializr.metadata.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Package: io.baseframework.initializr.metadata<br>
 * @ClassName: BfInitializrMetadata.java<br>
 * @Description: TODO
 * @author: gaoxinxing
 */
public class BfInitializrMetadata extends InitializrMetadata {

	private final DependenciesCapability bfDependencies;

	private final SingleSelectCapability bfVersions;

	public BfInitializrMetadata() {
		this(new InitializrConfiguration());
	}

	public BfInitializrMetadata(InitializrConfiguration configuration) {
		super(configuration);
		this.bfDependencies = new DependenciesCapability();
		this.bfVersions = new SingleSelectCapability("baseFrameworkVersion", "baseFramework Version",
				"base framework version");
	}

	public DependenciesCapability getBfDependencies() {
		return bfDependencies;
	}

	public SingleSelectCapability getBfVersions() {
		return bfVersions;
	}

	public void updateBaseFrameworkVersions(List<DefaultMetadataElement> versionsMetadata) {
		this.bfVersions.getContent().clear();
		this.bfVersions.getContent().addAll(versionsMetadata);
		List<Version> bfVersions = this.bfVersions.getContent().stream().map((it) -> {
			return Version.parse(it.getId());
		}).collect(Collectors.toList());
		VersionParser parser = new VersionParser(bfVersions);
		this.bfDependencies.updateCompatibilityRange(parser);
		/*
		 * this.configuration.getEnv().getBoms().values().forEach((it) -> {
		 * it.updateCompatibilityRange(parser); });
		 * this.configuration.getEnv().getKotlin().updateCompatibilityRange(parser);
		 */
	}

}
