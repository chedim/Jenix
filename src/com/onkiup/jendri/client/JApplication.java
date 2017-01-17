package com.onkiup.jendri.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onkiup.jendri.db.Column;
import com.onkiup.jendri.db.Query;
import com.onkiup.jendri.db.Record;
import com.onkiup.jendri.db.annotations.FieldNotNull;
import com.onkiup.jendri.db.annotations.Unique;

public class JApplication extends Record implements BuilderService.ClientApplication {
    @FieldNotNull
    @Unique
    private String name;
    @FieldNotNull
    @Unique
    private String source;
    private transient List<JApplicationFile> files;
    private transient Map<JApplicationFile, JApplicationCompiledFile> compiledFiles = new HashMap<>();

    public JApplication() {
    }

    public JApplication(String source) {
        this.source = source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public List<JApplicationFile> getFiles() throws Exception {
        if (files == null) {
            files = Query.from(JApplicationFile.class).where("application = ?", this).fetch();
        }
        return new ArrayList<>(files);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCompiledFile(JApplicationCompiledFile file) {
        compiledFiles.put(file.getSource(), file);
    }

    public List<BuilderService.ClientApplicationFile> getCompiledFiles() {
        List<BuilderService.ClientApplicationFile> files = new ArrayList<>();
        for (JApplicationFile file : this.files) {
            if (compiledFiles.containsKey(file)) {
                files.add(compiledFiles.get(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }
}
