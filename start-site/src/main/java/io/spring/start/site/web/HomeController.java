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
package io.spring.start.site.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Main Controller.
 *
 * @author Brian Clozel
 */
@Controller
public class HomeController {

	private static String LANG = "lang";

	@GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
	public String home(HttpServletRequest request, HttpServletResponse response) {
		String lang = request.getParameter(LANG);
		if (!StringUtils.isEmpty(lang)) {
			response.addCookie(new Cookie("lang", lang.toLowerCase()));
		}
		return "forward:index.html";
	}

}
