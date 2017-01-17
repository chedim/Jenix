package com.onkiup.jendri.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Date;

import com.onkiup.jendri.db.Record;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;

public class JApplicationCompiledFile extends Record implements BuilderService.ClientApplicationFile {
    private JApplicationFile source;
    private String compilerHash;
    private String location;

    public JApplicationCompiledFile() {
    }

    public JApplicationCompiledFile(JApplicationFile source, String compilerHash, String location) {
        this.source = source;
        this.compilerHash = compilerHash;
        this.location = location;
    }

    public Reader getContents() throws FileNotFoundException {
        return new FileReader(location);
    }

    @Override
    public String getType() {
        if (source == null) {
            return null;
        }
        return source.getType();
    }

    @Override
    public String getName() {
        if (source == null) {
            return null;
        }
        return source.getName();
    }

    public JApplicationFile getSource() {
        return source;
    }

    public void setSource(JApplicationFile source) {
        this.source = source;
    }

    public String getCompilerHash() {
        return compilerHash;
    }

    public void setCompilerHash(String compilerHash) {
        this.compilerHash = compilerHash;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public BuilderService.ClientApplication getApplication() {
        return source.getApplication();
    }

    @Override
    public BuilderService.ApplicationSection getSection() {
        return null;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public File getFile() {
        return new File(location);
    }

    @Override
    public Date getModified() {
        return new Date(getFile().lastModified());
    }

    @Override
    public void setSection(BuilderService.ApplicationSection section) {
        source.setSection(section);
    }

    public boolean isUpToDate() {
        if (location == null || source == null) {
            return false;
        }

        return getModified().after(source.getModified());
    }
}
