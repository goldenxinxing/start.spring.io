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

import io.baseframework.initializr.metadata.BfInitializrMetadata;
import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.version.Version;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Type;
import io.spring.initializr.metadata.support.MetadataBuildItemMapper;
import io.spring.initializr.web.project.InvalidProjectRequestException;
import io.spring.initializr.web.project.ProjectRequest;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Validates a {@link BfProjectRequest} and creates a {@link ProjectDescription} from it.
 *
 * @author Madhura Bhave
 * @author HaiTao Zhang
 */
public class BfProjectRequestToDescriptionConverter {

	private static final Version VERSION_1_5_0 = Version.parse("1.5.0.RELEASE");

	private static final char[] VALID_MAVEN_SPECIAL_CHARACTERS = new char[] { '_', '-', '.' };

	public ProjectDescription convert(BfProjectRequest request, BfInitializrMetadata metadata) {
		validate(request, metadata);
		String springBootVersion = getSpringBootVersion(request, metadata);
		String baseFrameworkVersion = getBaseFrameworkVersion(request, metadata);
		List<Dependency> resolvedDependencies = getResolvedDependencies(request, springBootVersion, metadata);
		validateDependencyRange(springBootVersion, resolvedDependencies);
		ProjectDescription description = new ProjectDescription();
		description.setApplicationName(getApplicationName(request, metadata));
		description.setArtifactId(getArtifactId(request, metadata));
		description.setBaseDirectory(getBaseDirectory(request.getBaseDir(), request.getArtifactId()));
		description.setBuildSystem(getBuildSystem(request, metadata));
		description
				.setDescription(determineValue(request.getDescription(), () -> metadata.getDescription().getContent()));
		description.setGroupId(getGroupId(request, metadata));
		description.setLanguage(Language.forId(request.getLanguage(), request.getJavaVersion()));
		description.setName(getName(request, metadata));
		description.setPackageName(getPackageName(request, metadata));
		description.setPackaging(Packaging.forId(request.getPackaging()));
		description.setParentVersion(Version.parse(baseFrameworkVersion));
		description.setPlatformVersion(Version.parse(springBootVersion));
		description.setVersion(determineValue(request.getVersion(), () -> metadata.getVersion().getContent()));
		resolvedDependencies.forEach((dependency) -> description.addDependency(dependency.getId(),
				MetadataBuildItemMapper.toDependency(dependency)));

		return description;
	}

	private String determineValue(String candidate, Supplier<String> fallback) {
		return (StringUtils.hasText(candidate)) ? candidate : fallback.get();
	}

	private String getBaseDirectory(String baseDir, String artifactId) {
		if (baseDir != null && baseDir.equals(artifactId)) {
			return cleanMavenCoordinate(baseDir, "-");
		}
		return baseDir;
	}

	private String getName(BfProjectRequest request, BfInitializrMetadata metadata) {
		String name = request.getName();
		if (!StringUtils.hasText(name)) {
			return metadata.getName().getContent();
		}
		if (name.equals(request.getArtifactId())) {
			return cleanMavenCoordinate(name, "-");
		}
		return name;
	}

	private String getGroupId(BfProjectRequest request, BfInitializrMetadata metadata) {
		if (!StringUtils.hasText(request.getGroupId())) {
			return metadata.getGroupId().getContent();
		}
		return cleanMavenCoordinate(request.getGroupId(), ".");
	}

	private String getArtifactId(BfProjectRequest request, BfInitializrMetadata metadata) {
		if (!StringUtils.hasText(request.getArtifactId())) {
			return metadata.getArtifactId().getContent();
		}
		return cleanMavenCoordinate(request.getArtifactId(), "-");
	}

	private String cleanMavenCoordinate(String coordinate, String delimiter) {
		String[] elements = coordinate.split("[^\\w\\-.]+");
		if (elements.length == 1) {
			return coordinate;
		}
		StringBuilder builder = new StringBuilder();
		for (String element : elements) {
			if (shouldAppendDelimiter(element, builder)) {
				builder.append(delimiter);
			}
			builder.append(element);
		}
		return builder.toString();
	}

	private boolean shouldAppendDelimiter(String element, StringBuilder builder) {
		if (builder.length() == 0) {
			return false;
		}
		for (char c : VALID_MAVEN_SPECIAL_CHARACTERS) {
			int prevIndex = builder.length() - 1;
			if (element.charAt(0) == c || builder.charAt(prevIndex) == c) {
				return false;
			}
		}
		return true;
	}

	private void validate(BfProjectRequest request, BfInitializrMetadata metadata) {
		validateSpringBootVersion(request);
		validateType(request.getType(), metadata);
		validateLanguage(request.getLanguage(), metadata);
		validatePackaging(request.getPackaging(), metadata);
		validateDependencies(request, metadata);
	}

	private void validateSpringBootVersion(BfProjectRequest request) {
		Version bootVersion = Version.safeParse(request.getBootVersion());
		if (bootVersion != null && bootVersion.compareTo(VERSION_1_5_0) < 0) {
			throw new InvalidProjectRequestException(
					"Invalid Spring Boot version " + bootVersion + " must be 1.5.0 or higher");
		}
	}

	private void validateType(String type, BfInitializrMetadata metadata) {
		if (type != null) {
			Type typeFromMetadata = metadata.getTypes().get(type);
			if (typeFromMetadata == null) {
				throw new InvalidProjectRequestException("Unknown type '" + type + "' check project metadata");
			}
			if (!typeFromMetadata.getTags().containsKey("build")) {
				throw new InvalidProjectRequestException(
						"Invalid type '" + type + "' (missing build tag) check project metadata");
			}
		}
	}

	private void validateLanguage(String language, BfInitializrMetadata metadata) {
		if (language != null) {
			DefaultMetadataElement languageFromMetadata = metadata.getLanguages().get(language);
			if (languageFromMetadata == null) {
				throw new InvalidProjectRequestException("Unknown language '" + language + "' check project metadata");
			}
		}
	}

	private void validatePackaging(String packaging, BfInitializrMetadata metadata) {
		if (packaging != null) {
			DefaultMetadataElement packagingFromMetadata = metadata.getPackagings().get(packaging);
			if (packagingFromMetadata == null) {
				throw new InvalidProjectRequestException(
						"Unknown packaging '" + packaging + "' check project metadata");
			}
		}
	}

	private void validateDependencies(BfProjectRequest request, BfInitializrMetadata metadata) {
		List<String> dependencies = (!request.getStyle().isEmpty() ? request.getStyle() : request.getDependencies());
		dependencies.forEach((dep) -> {
			Dependency dependency = metadata.getDependencies().get(dep);
			if (dependency == null) {
				throw new InvalidProjectRequestException("Unknown dependency '" + dep + "' check project metadata");
			}
		});
	}

	private void validateDependencyRange(String springBootVersion, List<Dependency> resolvedDependencies) {
		resolvedDependencies.forEach((dep) -> {
			if (!dep.match(Version.parse(springBootVersion))) {
				throw new InvalidProjectRequestException("Dependency '" + dep.getId() + "' is not compatible "
						+ "with Spring Boot " + springBootVersion);
			}
		});
	}

	private BuildSystem getBuildSystem(BfProjectRequest request, BfInitializrMetadata metadata) {
		Type typeFromMetadata = metadata.getTypes().get(request.getType());
		return BuildSystem.forId(typeFromMetadata.getTags().get("build"));
	}

	private String getPackageName(BfProjectRequest request, BfInitializrMetadata metadata) {
		return metadata.getConfiguration().cleanPackageName(request.getPackageName(),
				metadata.getPackageName().getContent());
	}

	private String getApplicationName(BfProjectRequest request, BfInitializrMetadata metadata) {
		if (!StringUtils.hasText(request.getApplicationName())) {
			return metadata.getConfiguration().generateApplicationName(request.getName());
		}
		return request.getApplicationName();
	}

	private String getBaseFrameworkVersion(BfProjectRequest request, BfInitializrMetadata metadata) {
		return (request.getBaseFrameworkVersion() != null) ? request.getBaseFrameworkVersion()
				: metadata.getBfVersions().getDefault().getId();
	}

	private String getSpringBootVersion(BfProjectRequest request, BfInitializrMetadata metadata) {
		return (request.getBootVersion() != null) ? request.getBootVersion()
				: metadata.getBootVersions().getDefault().getId();
	}

	private List<Dependency> getResolvedDependencies(BfProjectRequest request, String springBootVersion,
			BfInitializrMetadata metadata) {
		List<String> depIds = (!request.getStyle().isEmpty() ? request.getStyle() : request.getDependencies());
		Version requestedVersion = Version.parse(springBootVersion);
		return depIds.stream().map((it) -> {
			Dependency dependency = metadata.getDependencies().get(it);
			return dependency.resolve(requestedVersion);
		}).collect(Collectors.toList());
	}

}
