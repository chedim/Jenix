package com.onkiup.jendri.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.FieldNotNull;
import com.onkiup.jendri.util.StringUtils;
import com.sun.istack.internal.NotNull;
import org.apache.commons.io.FilenameUtils;

public class JApplicationFile extends Record implements BuilderService.ClientApplicationFile {
    @FieldNotNull
    private JApplication application;
    @FieldNotNull
    private String type;
    @FieldNotNull
    private String location;
    @FieldNotNull
    private BuilderService.ApplicationSection section;

    public JApplicationFile() {
    }

    public JApplicationFile(JApplication application, String location) {
        this.application = application;
        this.location = location;

        List<String> parts = Arrays.asList(FilenameUtils.getName(location).split("\\."));
        if (parts.size() > 1) {
            type = StringUtils.join(parts.subList(1, parts.size()), ".");
        }
    }

    @Override
    public Reader getContents() throws FileNotFoundException {
        return new FileReader(location);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        String[] parts = FilenameUtils.getName(location).split("\\.");
        return parts[0];
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public BuilderService.ClientApplication getApplication() {
        return application;
    }

    @Override
    public BuilderService.ApplicationSection getSection() {
        return section;
    }

    public void setSection(BuilderService.ApplicationSection section) {
        this.section = section;
    }

    public File getFile() {
        return new File(location);
    }

    @Override
    public Date getModified() {
        return new Date(getFile().lastModified());
    }
}