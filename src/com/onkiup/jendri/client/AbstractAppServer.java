package com.onkiup.jendri.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.api.AbstractStaticServer;
import com.onkiup.jendri.injection.Inject;

public abstract class AbstractAppServer extends AbstractServlet {

    @Inject
    private static BuilderService builder;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getRequestURI().substring(req.getServletPath().length());
        URL resource = AbstractStaticServer.locateFile(name);
        File appDir;
        try {
            if (resource == null) {
                resp.setStatus(404);
                return;
            } else {
                appDir = new File(resource.toURI());
            }
            if (!(appDir.exists() && appDir.isDirectory())) {
                resp.setStatus(404);
                return;
            }
            resp.setContentType(builder.getContentType());
            BuilderService.ClientApplication app = builder.compile(name, appDir);
            builder.buildAndWrite(app, resp.getWriter());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
