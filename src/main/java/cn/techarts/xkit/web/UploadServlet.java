/*
 * Copyright (C) 2024 techarts.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.techarts.xkit.web;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;

import cn.techarts.whale.Context;
import cn.techarts.xkit.util.Hotpot;

/**
 * <p>javax & jakarta</p>
 * @author rocwon@gmail.com
 */
@WebServlet("/file/upload")
@MultipartConfig(maxFileSize = 1024 * 1024 * 128)
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CT_MULTIPART = "multipart/form-data";
	private static final Logger LOGGER = Hotpot.getLogger();
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var ct = request.getContentType();
        if (Objects.isNull(ct) || !ct.startsWith(CT_MULTIPART)) {
            throw new ServletException("Unsupported content type: " + ct);
        }
		var ctx = Context.from(request.getServletContext());
		var handler = ctx.get(UploadHandler.class);
		if(Objects.isNull(handler)) {
			throw new ServletException("Can not find file upload handler.");
		}
		
		var parts = request.getParts();
		if(parts == null || parts.isEmpty()) return;
		
		for(var part : parts) {
			var size = part.getSize();
			if(size == 0) continue;
			var type = part.getContentType();
			var name = part.getSubmittedFileName();
			handler.handle(name, type, size, part.getInputStream());
			LOGGER.info("Successfully uploaded the file: " + name);
		}
	}
}