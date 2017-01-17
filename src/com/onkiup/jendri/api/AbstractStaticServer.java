package com.onkiup.jendri.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onkiup.jendri.AbstractServlet;
import com.onkiup.jendri.injection.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class AbstractStaticServer extends AbstractServlet{
    private static final Logger LOGGER = LogManager.getLogger(AbstractStaticServer.class);

    private static Map<String, String> paths = new HashMap<>();

    public AbstractStaticServer() {
        paths.put(getRoot(), getIndexFile());
    }

    private static final ClassLoader LOADER = Thread.currentThread().getContextClassLoader();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        URL resourse = locateFile(getRoot(), uri, getIndexFile());
        if (resourse != null) {
            LOGGER.info("GET " + uri + " => " + resourse.toString());
            InputStream in = resourse.openStream();
            IOUtils.copy(in, resp.getWriter());
        } else {
            LOGGER.info("GET " + uri + " => 404");
            resp.setStatus(404);
        }
    }

    private URL locateFile(String root, String path, String index) throws MalformedURLException {
        if (path == null || path.length() == 0 || path.matches("^.*\\/[^\\.]*$")) {
            path = index;
        }

        File file = new File(root + path);
        if (file.exists()) {
            return file.toURI().toURL();
        }

        URL resourse = LOADER.getResource(root + path);
        return resourse;
    }

    public static URL locateFile(String path) throws MalformedURLException {
        URL resourse = null;
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            String testPath = path;
            if (testPath == null || testPath.length() == 0 || testPath.equals("/")) {
                testPath = entry.getValue();
            }
            File file = new File(entry.getKey() + testPath);
            if (file.exists()) {
                return file.toURI().toURL();
            }

            resourse = LOADER.getResource(entry.getKey() + testPath);
            if (resourse != null) {
                return resourse;
            }
        }

        return null;
    }

    protected abstract String getRoot();
    protected abstract String getIndexFile();
}
