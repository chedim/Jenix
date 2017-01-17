package com.onkiup.jendri.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PipedReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.onkiup.jendri.StaticServer;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.service.ServiceStub;
import com.onkiup.jendri.util.ChainReader;

public final class BuilderService {

    @Inject
    private static Implementation implementation;

    public static ClientApplication compile(String name) {
        try {
            URL folder = StaticServer.locateFile(name);
            File source = new File(folder.toURI());
            return compile(name, source);
        } catch (Exception e) {
            throw new RuntimeException("unable to locate and/or compile app " + name, e);
        }
    }

    public static ClientApplication compile(String name, File source) throws Exception {
        return implementation.compile(name, source);
    }

    public static void buildAndWrite(ClientApplication application, Writer writer) throws Exception {
        implementation.buildAndWrite(application, writer);
    }

    public static String getContentType() {
        return implementation.getContentType();
    }

    public interface Implementation extends ServiceStub {

        ClientApplication compile(String name, File source) throws Exception;

        void buildAndWrite(ClientApplication application, Writer writer) throws Exception;

        String getContentType();
    }

    public interface ClientApplication {
        List<? extends ClientApplicationFile> getFiles() throws Exception;
        List<BuilderService.ClientApplicationFile> getCompiledFiles();
        String getName();
    }

    public interface ClientApplicationFile {
        Reader getContents() throws Exception;
        String getType();
        String getName();
        String getLocation();
        ClientApplication getApplication();
        ApplicationSection getSection();
        File getFile();
        Date getModified();

        void setSection(ApplicationSection section);
    }

    public enum ApplicationSection {
        CODE, TEMPLATE, STYLE, RESOURCE;
    }
}
