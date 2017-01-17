package com.onkiup.jendri.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.onkiup.jendri.api.SerializationService;
import com.onkiup.jendri.db.PersistantObject;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Storageable;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.util.DigestUtils;
import com.onkiup.jendri.util.StringUtils;
import com.sun.tools.javac.resources.compiler;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class JendriBuilder implements BuilderService.Implementation {

    @Inject
    private static CompilerService compiler;

    @Override
    public BuilderService.ClientApplication compile(String name, File source) throws Exception {
        if (!source.isDirectory()) {
            throw new Exception("Source should be a directory!");
        }

        JApplication application = Query.from(JApplication.class).where("source = ?", source.getPath()).fetchOne();
        if (application == null) {
            application = new JApplication(source.getPath());
            application.setName(StringUtils.trim(name, "/"));
            application.saveImmediately();
        }

        List<JApplicationFile> oldFiles = application.getFiles();

        for (File file : source.listFiles()) {
            if (file.isDirectory() || file.getName().startsWith(".")) {
                continue;
            }

            JApplicationFile appFile = null;
            for (JApplicationFile oldFile : oldFiles) {
                if (oldFile.getLocation().equals(file.getPath())) {
                    appFile = oldFile;
                    oldFiles.remove(appFile);
                    break;
                }
            }

            if (appFile == null) {
                appFile = new JApplicationFile(application, file.getPath());
                appFile.setSection(compiler.getSection(appFile.getType()));
            }
            if (compiler.isCompilable(appFile.getType())) {
                Date mod = new Date(file.lastModified());
                JApplicationCompiledFile compiled = compiler.compile(appFile);
                application.addCompiledFile(compiled);
            } else {
                appFile.setSection(BuilderService.ApplicationSection.RESOURCE);
            }
            appFile.saveImmediately();
        }
        for (JApplicationFile file : oldFiles) {
            file.delete();
        }
        return application;
    }

    @Override
    public void buildAndWrite(BuilderService.ClientApplication application, Writer writer) throws Exception {
        HashMap<BuilderService.ApplicationSection, HashMap<String, Object>> files = new HashMap<>();
        List<? extends BuilderService.ClientApplicationFile> sources = application.getCompiledFiles();
        for (BuilderService.ClientApplicationFile source : sources) {
            BuilderService.ApplicationSection section = source.getSection();
            if (section == null) {
                section = compiler.getSection(source.getType());
                source.setSection(section);
                if (source instanceof Storageable) {
                    ((Storageable) source).save();
                }
            }
            File compiled = source.getFile();
            files.putIfAbsent(section, new HashMap<>());
            FileReader reader = new FileReader(compiled);
            StringWriter codeWriter = new StringWriter();
            IOUtils.copy(reader, codeWriter);
            files.get(section).put(source.getName(), codeWriter.toString());
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("name", application.getName());
        result.put("components", files);

        SerializationService.serialize(null, result, writer);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
