package com.onkiup.jendri.db.mysql.structure;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.onkiup.jendri.db.mysql.exceptions.UpdateFailedException;
import com.onkiup.jendri.db.structure.Table;
import org.apache.commons.lang3.StringUtils;

public class ReferenceImpl implements Table.Reference {

    private Table source;
    private Table target;

    private List<Table.Field> sourceFields = new ArrayList<>();
    private List<Table.Field> targetFields = new ArrayList<>();
    private boolean hard;

    private boolean existsInDb = false;
    private boolean matchesDb = false;

    // source modifies target
    public ReferenceImpl(Table source, Table target) {
        this(source, source.getPrimaryKey(), target, true);
    }

    public ReferenceImpl(Table source, Table.Field field, Table target) {
        this(source, Arrays.asList(field), target, Arrays.asList(target.getPrimaryKey()), true);
    }

    public ReferenceImpl(Table source, Table.Field field, Table target, boolean hard) {
        this(source, Arrays.asList(field), target, Arrays.asList(target.getPrimaryKey()), hard);
    }

    public ReferenceImpl(Table source, List<Table.Field> sourceFields, Table target, List<Table.Field> targetFields) {
        this(source, sourceFields, target, targetFields, false);
    }

    public ReferenceImpl(Table source, List<Table.Field> sourceFields, Table target, List<Table.Field> targetFields, boolean hard) {
        this.source = source;
        this.target = target;
        this.hard = hard;
        checkFields(source, sourceFields, false);
        checkFields(target, targetFields, true);
        this.sourceFields = sourceFields;
        this.targetFields = targetFields;

        if (source.existsInDatabase()) {
            String createScript = source.getCreateScript();
            String me = "CONSTRAINT `" + getName() + "`";
            existsInDb = createScript.contains(me);
            if (existsInDb) {
                matchesDb = createScript.contains(getDeclaration());
            }
        }
    }

    @Override
    public Table getTarget() {
        return target;
    }

    @Override
    public Table getSource() {
        return source;
    }

    @Override
    public List<Table.Field> getFields() {
        List<Table.Field> fields = new ArrayList<>();
        fields.add(target.getPrimaryKey());
        return fields;
    }

    @Override
    public void setHard(boolean b) {
        hard = b;
    }

    @Override
    public boolean isHard() {
        return hard;
    }

    @Override
    public String getName() {
        List<String> from = sourceFields.stream().map(Table.Field::getName).collect(Collectors.toList());
        List<String> to = targetFields.stream().map(Table.Field::getName).collect(Collectors.toList());
        String fromName = source.getName();
        String toName = target.getName();
        String result = fromName + "→" + toName + "_";

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            from.addAll(to);
            result += StringUtils.join(from);
            digest.update(result.getBytes());
            String fields_hash = String.format("%032x", new BigInteger(1, digest.digest()));
            result = fields_hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not available", e);
        }

        return "ref_" + result;
    }

    @Override
    public String getDeclaration() {
        String decl = "CONSTRAINT `" + getName() + "` FOREIGN KEY (`";
        List<String> fields = sourceFields.stream().map(Table.Field::getName).collect(Collectors.toList());
        decl += StringUtils.join(fields, "`, `");
        decl += "`) REFERENCES `" + target.getName() + "` (`";
        fields = targetFields.stream().map(Table.Field::getName).collect(Collectors.toList());
        decl += StringUtils.join(fields, "`, `");
        decl += "`)";

        if (hard) {
            decl += " ON DELETE CASCADE ON UPDATE CASCADE";
        } else {
            decl += " ON DELETE SET NULL ON UPDATE CASCADE";
        }

        return decl;
    }

    @Override
    public void update() throws UpdateFailedException {
        if (!matchesDb) {
            final String[] query = {null};
            try {
                source.getConnection(connection -> {
                    try (Statement statement = ((Connection) connection).createStatement()) {
                        if (existsInDatabase()) {
                            query[0] = "ALTER TABLE `" + source.getName() + "` DROP FOREIGN KEY `" + getName() + "`";
                            statement.execute(query[0]);
                        }

                        query[0] = "ALTER TABLE `" + source.getName() + "` ADD " + getDeclaration();
                        statement.execute(query[0]);
                        existsInDb = matchesDb = true;
                    }
                });
            } catch (Exception e) {
                throw new UpdateFailedException(this, query[0], e);
            }
        }
    }

    @Override
    public boolean existsInDatabase() {
        return existsInDb;
    }

    @Override
    public boolean matchesDatabase() {
        return existsInDb && matchesDb;
    }

    private void checkFields(Table table, List<Table.Field> fields, boolean checkIndexed) {
        List<Table.Index> indexes = table.getIndexes();
        for (Table.Field field : fields) {
            String err = "Can't use field `" + field.getTable().getName() + "." + field.getName() + "` on `" + table.getName() + "`: ";
            if (!table.equals(field.getTable())) {
                throw new RuntimeException(err + "field belongs to a different table");
            }
            if (checkIndexed && !indexes.contains(field) && !field.isPrimary()) {
                throw new RuntimeException(err + "field is not indexed");
            }
        }

    }
}
