package com.onkiup.jendri.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Set;

import com.github.sommeri.less4j.Less4jException;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.util.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface Compiler {
    static final Logger LOGGER = LogManager.getLogger(Compiler.class);
    Set<String> getSupportedTypes();
    String getResultType();
    default Reader compile(String code) throws Exception {
        return new StringReader(code);
    }

    default Reader compile(URL code) throws Exception {
        return new InputStreamReader(code.openStream());
    }

    default Reader compile(File code) throws Exception {
        return new FileReader(code);
    }

    default Reader doCompile(JApplicationFile file) throws Exception {
        return compile(new File(file.getLocation()));
    }

    default BuilderService.ApplicationSection getApplicationSection() {
        return BuilderService.ApplicationSection.RESOURCE;
    }

    default JApplicationCompiledFile compile(JApplicationFile file) throws Exception {
        LOGGER.info("Compile request for file " + file.getLocation());
        String hash = fileHash(file);
        JApplicationCompiledFile compiledFile = Query.from(JApplicationCompiledFile.class)
                .where("compilerHash = ?", hash).fetchOne();
        if ((file.getFile()!= null && file.getFile().exists())
                && (compiledFile == null || !compiledFile.isUpToDate()
                || compiledFile.getFile() == null
                || !compiledFile.getFile().exists())) {
            File compiledDir = new File(FileUtils.getTempDirectory(), hash.substring(0, 2));
            compiledDir.mkdirs();
            File target = new File(compiledDir, hash);
            Reader compiled = doCompile(file);
            FileWriter targetWriter = new FileWriter(target);
            IOUtils.copy(compiled, targetWriter);
            targetWriter.close();
            LOGGER.info("compiled " + file.getLocation() +  " into " + target.getPath());
            if (compiledFile == null) {
                compiledFile = new JApplicationCompiledFile(file, hash, target.getPath());
                compiledFile.save();
            } else {
                if (!compiledFile.getLocation().equals(target.getPath())) {
                    compiledFile.getFile().delete();
                    compiledFile.setLocation(target.getPath());
                    compiledFile.save();
                }
            }
        } else {
            LOGGER.info("compiled version of file " + file.getLocation() + " is up to date");
        }
        return compiledFile;
    }

    default String fileHash(JApplicationFile file) throws Exception {
        return DigestUtils.hash("MD5", file.getApplication().getName() + ":" + file.getLocation());
    }
}
