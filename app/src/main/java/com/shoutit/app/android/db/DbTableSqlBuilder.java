
package com.shoutit.app.android.db;


import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class DbTableSqlBuilder {

    public enum Type {
        INTEGER("INTEGER"), TEXT("TEXT");

        private final String mType;

        Type(String type) {
            mType = type;
        }

        @Override
        public String toString() {
            return mType;
        }
    }

    private static final String PRIMARY_KEY = "PRIMARY KEY";
    private static final String NOT_NULL = "NOT NULL";
    private static final String CREATE_TABLE = "CREATE TABLE";
    private static final String DEFAULT = "DEFAULT";
    private static final String AUTOINCREMENT = "AUTOINCREMENT";
    private static final String ON_CONFLICT_REPLACE = "ON CONFLICT REPLACE";
    private static final String ON_CONFLICT_IGNORE = "ON CONFLICT IGNORE";

    public static class TableBuilder {

        public static class ColumnBuilder {

            private final TableBuilder mTableBuilder;
            private final String mName;
            private final Type mType;
            private boolean isNotNull;
            private boolean isPrimaryKey;
            private String defaultValue;
            private boolean isAutoIncrement;

            private ColumnBuilder(TableBuilder tableBuilder, String name, Type type) {
                mTableBuilder = tableBuilder;
                mName = name;
                mType = type;
            }

            public ColumnBuilder setNotNull(boolean notNull) {
                isNotNull = notNull;
                return this;
            }

            public ColumnBuilder isPrimaryKey(boolean primaryKey) {
                isPrimaryKey = primaryKey;
                return this;
            }

            public ColumnBuilder setDefaultValue(String value) {
                defaultValue = value;
                return this;
            }

            public ColumnBuilder setDefaultValue(int value) {
                return setDefaultValue(String.valueOf(value));
            }

            public ColumnBuilder isAutoincrement(boolean autoIncrement) {
                isAutoIncrement = autoIncrement;
                return this;
            }

            public TableBuilder buildColumn() {
                mTableBuilder.mColumns.add(createColumnSql());
                return mTableBuilder;
            }

            private String createColumnSql() {
                StringBuilder stringBuilder = new StringBuilder(mName).append(" ").append(mType);
                if (isNotNull) {
                    stringBuilder.append(" ").append(NOT_NULL);
                }
                if (isPrimaryKey) {
                    stringBuilder.append(" ").append(PRIMARY_KEY);
                }
                if (defaultValue != null) {
                    stringBuilder.append(" ").append(DEFAULT).append(" ").append(defaultValue);
                }
                if (isAutoIncrement) {
                    stringBuilder.append(" ").append(AUTOINCREMENT);
                }
                return stringBuilder.toString();
            }
        }

        private final String mName;
        private final List<String> mColumns = new ArrayList<>();
        private String uniqueColumn;
        private boolean onConflictIgnore;
        private boolean onConflictReplace;

        public TableBuilder(String name) {
            mName = name;
        }

        public ColumnBuilder addColumn(String name, Type type) {
            return new ColumnBuilder(this, name, type);
        }

        public TableBuilder setUniqueColumn(@Nonnull String uniqueColumn) {
            this.uniqueColumn = uniqueColumn;
        }

        public TableBuilder setOnConflictReplace(boolean onConflictReplace) {
            this.onConflictReplace = onConflictReplace;
            return this;
        }

        public TableBuilder setOnConflictIgnore(boolean onConflictIgnore) {
            this.onConflictIgnore = onConflictIgnore;
            return this;
        }

        public String build() {
            StringBuilder stringBuilder = new StringBuilder(CREATE_TABLE).append(" ").append(mName).append("(");
            String separator = "";
            for (String column : mColumns) {
                stringBuilder.append(separator).append(column);
                separator = ", ";
            }

            if (!Strings.isNullOrEmpty(uniqueColumn) && onConflictIgnore) {
                stringBuilder.append("UNIQUE").append("(").append(uniqueColumn).append(")").append(ON_CONFLICT_IGNORE);
            } else if (!Strings.isNullOrEmpty(uniqueColumn) && onConflictReplace) {
                stringBuilder.append("UNIQUE").append("(").append(uniqueColumn).append(")").append(ON_CONFLICT_REPLACE);
            }

            stringBuilder.append(");");
            return stringBuilder.toString();
        }
    }

    public TableBuilder createTable(String name) {
        return new TableBuilder(name);
    }
}