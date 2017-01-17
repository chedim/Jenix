package com.onkiup.jendri.client.compilers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.onkiup.jendri.AppServer;
import com.onkiup.jendri.StaticServer;
import com.onkiup.jendri.client.AbstractAppServer;
import com.onkiup.jendri.client.BuilderService;
import com.onkiup.jendri.client.Compiler;
import com.onkiup.jendri.client.JApplication;
import com.onkiup.jendri.client.JApplicationFile;
import com.onkiup.jendri.injection.Inject;
import com.onkiup.jendri.session.Cookies;
import com.onkiup.jendri.util.ChainReader;
import com.onkiup.jendri.util.DigestUtils;
import com.onkiup.jendri.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Less implements Compiler {
    private static LessCompiler compiler = new DefaultLessCompiler();
    private static Logger LOGGER = LogManager.getLogger(Less.class);
    public final String INTERFACE_KEY = "interface";

    @Inject("default")
    private static String defaultInterface;

    @Override
    public Set<String> getSupportedTypes() {
        return new HashSet<>(Arrays.asList("less", "inc.less"));
    }

    @Override
    public String getResultType() {
        return "css";
    }

    @Override
    public Reader compile(String code) throws Exception {
        LessCompiler.CompilationResult result = compiler.compile(code);
        return new StringReader(result.getCss());
    }

    @Override
    public Reader compile(URL code) throws Exception {
        LessCompiler.CompilationResult result = compiler.compile(code);
        return new StringReader(result.getCss());
    }

    @Override
    public Reader doCompile(JApplicationFile file) throws Exception {
        ApplicationStyleSource source = new ApplicationStyleSource(file);
        LessCompiler.CompilationResult result = compiler.compile(source);
        String code = result.getCss();
        LOGGER.info("compiled " + file.getLocation() + " into " + code.length() + " chars");
        return new StringReader(code);
    }

    @Override
    public String fileHash(JApplicationFile file) throws Exception {
        String currentInterface = Cookies.get(INTERFACE_KEY);
        if (currentInterface == null) {
            currentInterface = defaultInterface;
        }
        return DigestUtils.hash("SHA-512", file.getApplication().getName() + ":" + file.getLocation() + ":" + currentInterface);
    }

    @Override
    public BuilderService.ApplicationSection getApplicationSection() {
        return BuilderService.ApplicationSection.STYLE;
    }

    public static class ApplicationStyleSource extends LessSource {

        private BuilderService.ClientApplicationFile base;
        private boolean included;

        public ApplicationStyleSource(BuilderService.ClientApplicationFile base) {
            this(base, false);
        }

        public ApplicationStyleSource(BuilderService.ClientApplicationFile base, boolean included) {
            this.base = base;
            this.included = included;
        }

        @Override
        public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile, StringSourceException {
            try {
                String currentInterface = Cookies.get("interface");
                if (StringUtils.isBlank(currentInterface)) {
                    currentInterface = AppServer.getDefaultInterface();
                }

                BuilderService.ClientApplication app;
                if (!currentInterface.equals(base.getApplication().getName())) {
                    app = base.getApplication();
                } else {
                    app = BuilderService.compile(currentInterface);
                }

                if (filename.equals("interface.less")) {
                    filename = "/interface/exports.inc.less";
                }

                if (filename.startsWith("/interface/")){
                    String namepart = filename.substring(11);
                    if (app instanceof JApplication) {
                        JApplication jApp = (JApplication) app;
                        List<JApplicationFile> files = jApp.getFiles();
                        for (BuilderService.ClientApplicationFile file : files) {
                            if (file.getFile().getName().equals(namepart)) {
                                LOGGER.info("Included interface file: " + file.getFile().getPath() + " as " + namepart);
                                return new ApplicationStyleSource(file, true);
                            }
                        }
                    }
                } else if (filename.startsWith("/lib/")) {
                    URL found = StaticServer.locateFile(filename.replaceAll("\\.\\.", "\\."));
                    if (found != null) {
                        File target = new File(found.toURI());
                        if (target.exists()) {
                            return new LessSource.FileSource(target);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            throw new FileNotFound();
        }

        @Override
        public String getContent() throws FileNotFound, CannotReadFile {
            try {
                File source = new File(base.getLocation());
                if (!source.exists()) {
                    throw new FileNotFound();
                }
                try (FileReader reader = new FileReader(source); StringWriter writer = new StringWriter()) {
                    if (!included) {
                        IOUtils.copy(new ChainReader("*[component=\"" + base.getApplication().getName() + "\"] {\n", reader, "\n}"), writer);
                    } else {
                        IOUtils.copy(reader, writer);
                    }
                    return writer.toString();
                } catch (IOException e) {
                    throw new CannotReadFile();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] getBytes() throws FileNotFound, CannotReadFile {
            return getContent().getBytes();
        }
    }
}
