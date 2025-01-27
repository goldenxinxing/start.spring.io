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

package io.spring.start.site.infrastructure;

import java.nio.file.Path;
import java.util.function.Function;

import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.generator.version.Version;
import io.spring.initializr.versionresolver.DependencyManagementVersionResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link ManagedDependenciesKotlinVersionResolver}.
 *
 * @author Andy Wilkinson
 */
class ManagedDependenciesKotlinVersionResolverTests {

	@Test
	@SuppressWarnings("unchecked")
	void kotlinVersionCanBeResolved(@TempDir Path temp) {
		ProjectDescription description = new ProjectDescription();
		description.setPlatformVersion(Version.parse("2.1.6.RELEASE"));
		Function<ResolvedProjectDescription, String> fallback = mock(Function.class);
		String version = new ManagedDependenciesKotlinVersionResolver(
				DependencyManagementVersionResolver.withCacheLocation(temp), fallback)
						.resolveKotlinVersion(new ResolvedProjectDescription(description));
		assertThat(version).isEqualTo("1.2.71");
		verifyZeroInteractions(fallback);
	}

}
