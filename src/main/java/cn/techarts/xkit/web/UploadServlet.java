package cn.techarts.xkit.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;

import cn.techarts.xkit.app.UploadHandler;
import cn.techarts.xkit.ioc.Context;
import cn.techarts.xkit.util.Hotchpotch;

@WebServlet("/file/upload")
@MultipartConfig(maxFileSize = 1024 * 1024 * 128)
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CT_MULTIPART = "multipart/form-data";
	private static final Logger LOGGER = Hotchpotch.getLogger(UploadServlet.class);
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		var ct = request.getContentType();
        if (ct == null || !ct.startsWith(CT_MULTIPART)) {
            throw new ServletException("Unsupported content type: " + ct);
        }
		
		var ctx = Context.from(request.getServletContext());
		var handler = ctx.get("fileUploadHandler", UploadHandler.class);
		if(handler == null) {
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
			LOGGER.debug("Successfully uploaded the file: " + name);
		}
	}
}