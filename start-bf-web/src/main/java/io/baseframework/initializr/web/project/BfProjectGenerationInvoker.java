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
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.BuildWriter;
import io.spring.initializr.generator.project.*;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.support.MetadataBuildItemResolver;
import io.spring.initializr.web.project.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Invokes the project generation API. This is an intermediate layer that can consume a
 * {@link ProjectRequest} and trigger project generation based on the request.
 *
 * @author Madhura Bhave
 */
public class BfProjectGenerationInvoker {

	private final ApplicationContext parentApplicationContext;

	private final ApplicationEventPublisher eventPublisher;

	private final BfProjectRequestToDescriptionConverter converter;

	private transient Map<Path, List<Path>> temporaryFiles = new LinkedHashMap<>();

	public BfProjectGenerationInvoker(ApplicationContext parentApplicationContext,
                                      ApplicationEventPublisher eventPublisher, BfProjectRequestToDescriptionConverter converter) {
		this.parentApplicationContext = parentApplicationContext;
		this.eventPublisher = eventPublisher;
		this.converter = converter;
	}

	/**
	 * Invokes the project generation API that generates the entire project structure for
	 * the specified {@link WebProjectRequest}.
	 * @param request the project request
	 * @return the {@link ProjectGenerationResult}
	 */
	public BfProjectGenerationResult invokeProjectStructureGeneration(BfProjectRequest request) {
		BfInitializrMetadata metadata = (BfInitializrMetadata) this.parentApplicationContext.getBean(InitializrMetadataProvider.class).get();
		try {
			ProjectDescription projectDescription = this.converter.convert(request, metadata);
			ProjectGenerator projectGenerator = new ProjectGenerator((
					projectGenerationContext) -> customizeProjectGenerationContext(projectGenerationContext, metadata));
			BfProjectGenerationResult result = projectGenerator.generate(projectDescription, generateProject(request));
			addTempFile(result.getRootDirectory(), result.getRootDirectory());
			return result;
		}
		catch (ProjectGenerationException ex) {
			publishProjectFailedEvent(request, metadata, ex);
			throw ex;
		}
	}

	private ProjectAssetGenerator<BfProjectGenerationResult> generateProject(BfProjectRequest request) {
		return (context) -> {
			Path projectDir = new DefaultProjectAssetGenerator().generate(context);
			publishProjectGeneratedEvent(request, context);
			return new BfProjectGenerationResult(context.getBean(ResolvedProjectDescription.class), projectDir);
		};
	}

	/**
	 * Invokes the project generation API that knows how to just write the build file.
	 * Returns a directory containing the project for the specified
	 * {@link WebProjectRequest}.
	 * @param request the project request
	 * @return the generated build content
	 */
	public byte[] invokeBuildGeneration(BfProjectRequest request) {
		BfInitializrMetadata metadata = (BfInitializrMetadata) this.parentApplicationContext.getBean(InitializrMetadataProvider.class).get();
		try {
			ProjectDescription projectDescription = this.converter.convert(request, metadata);
			ProjectGenerator projectGenerator = new ProjectGenerator((
					projectGenerationContext) -> customizeProjectGenerationContext(projectGenerationContext, metadata));
			return projectGenerator.generate(projectDescription, generateBuild(request));
		}
		catch (ProjectGenerationException ex) {
			publishProjectFailedEvent(request, metadata, ex);
			throw ex;
		}
	}

	private ProjectAssetGenerator<byte[]> generateBuild(BfProjectRequest request) {
		return (context) -> {
			byte[] content = generateBuild(context);
			publishProjectGeneratedEvent(request, context);
			return content;
		};
	}

	/**
	 * Create a file in the same directory as the given directory using the directory name
	 * and extension.
	 * @param dir the directory used to determine the path and name of the new file
	 * @param extension the extension to use for the new file
	 * @return the newly created file
	 */
	public Path createDistributionFile(Path dir, String extension) {
		Path download = dir.resolveSibling(dir.getFileName() + extension);
		addTempFile(dir, download);
		return download;
	}

	private void addTempFile(Path group, Path file) {
		this.temporaryFiles.computeIfAbsent(group, (key) -> new ArrayList<>()).add(file);
	}

	/**
	 * Clean all the temporary files that are related to this root directory.
	 * @param dir the directory to clean
	 * @see #createDistributionFile
	 */
	public void cleanTempFiles(Path dir) {
		List<Path> tempFiles = this.temporaryFiles.remove(dir);
		if (!tempFiles.isEmpty()) {
			tempFiles.forEach((path) -> {
				try {
					FileSystemUtils.deleteRecursively(path);
				}
				catch (IOException ex) {
					// Continue
				}
			});
		}
	}

	private byte[] generateBuild(ProjectGenerationContext context) throws IOException {
		ResolvedProjectDescription projectDescription = context.getBean(ResolvedProjectDescription.class);
		StringWriter out = new StringWriter();
		BuildWriter buildWriter = context.getBeanProvider(BuildWriter.class).getIfAvailable();
		if (buildWriter != null) {
			buildWriter.writeBuild(out);
			return out.toString().getBytes();
		}
		else {
			throw new IllegalStateException(
					"No BuildWriter implementation found for " + projectDescription.getLanguage());
		}
	}

	private void customizeProjectGenerationContext(AnnotationConfigApplicationContext context,
			InitializrMetadata metadata) {
		context.setParent(this.parentApplicationContext);
		context.registerBean(InitializrMetadata.class, () -> metadata);
		context.registerBean(BuildItemResolver.class, () -> new MetadataBuildItemResolver(metadata,
				context.getBean(ResolvedProjectDescription.class).getPlatformVersion()));
	}

	private void publishProjectGeneratedEvent(BfProjectRequest request, ProjectGenerationContext context) {
		InitializrMetadata metadata = context.getBean(InitializrMetadata.class);
		ProjectGeneratedEvent event = new ProjectGeneratedEvent(request, metadata);
		this.eventPublisher.publishEvent(event);
	}

	private void publishProjectFailedEvent(BfProjectRequest request, InitializrMetadata metadata, Exception cause) {
		ProjectFailedEvent event = new ProjectFailedEvent(request, metadata, cause);
		this.eventPublisher.publishEvent(event);
	}

}
