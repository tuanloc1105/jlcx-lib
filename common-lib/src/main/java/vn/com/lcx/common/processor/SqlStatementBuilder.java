package vn.com.lcx.common.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.Clob;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.SecondaryIdColumn;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.DOT;
import static vn.com.lcx.common.utils.WordCaseUtils.capitalize;

public class SqlStatementBuilder {

    private SqlStatementBuilder() {
    }

    public static String insertStatementBuilder(TypeElement typeElement, List<Element> classElements) {

        String result = CommonConstant.EMPTY_STRING;

        TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            return result;
        }
        String tableName;
        String tableNameValue = tableNameAnnotation.value();

        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                final String[] tableNameValueArray = tableNameValue.split(DOT);
                tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                tableName = schemaName + tableNameValue;
            }
        } else {
            tableName = tableNameValue;
        }

        String insertStatement = "INSERT INTO\\n    " + tableName;
        ArrayList<String> columnList = new ArrayList<>();
        String entityFieldGetCode = CommonConstant.EMPTY_STRING;
        ArrayList<String> putEntityFieldIntoMapCodeLines = new ArrayList<String>();
        String idColumnFieldName = CommonConstant.EMPTY_STRING;
        boolean idColumnIsNumber = false;

        for (Element classElement : classElements) {
            ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
            if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                continue;
            }
            IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
            if (idColumnAnnotation != null) {
                idColumnFieldName = classElement.getSimpleName().toString();
                if (
                        classElement.asType().toString().equals("java.math.BigDecimal") ||
                                classElement.asType().toString().equals("java.lang.Long")
                ) {
                    idColumnIsNumber = true;
                    continue;
                }
            }
            columnList.add(columnNameAnnotation.name());
            putEntityFieldIntoMapCodeLines.add(
                    String.format(
                            "map.put(++startingPosition, entity.get%s())",
                            capitalize(classElement.getSimpleName().toString())
                    )
            );
        }
        if (StringUtils.isBlank(idColumnFieldName)) {
            return result;
        }
        insertStatement += "(\\n        " + String.join(",\\n        ", columnList) + "\\n    )";
        // insertStatement += "(" + String.join(", ", columnList) + ")";

        insertStatement += "\\nVALUES\\n    " + "(\\n        " + columnList.stream().map(v -> "?").collect(Collectors.joining(",\\n        ")) + "\\n    )";
        // insertStatement += "\\nVALUES\\n    " + "(" + String.join(", ", columnList.stream().map(v -> "?").toList()) + ")";
        if (idColumnIsNumber) {
            entityFieldGetCode += String.format(
                    "\n" +
                            "\n    public static java.util.Map<Integer, Object> insertMapInputParameter(%s entity) {" +
                            "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                            "\n        int startingPosition = 0;" +
                            "\n        %s;" +
                            "\n        return map;" +
                            "\n    }",
                    typeElement.getSimpleName(),
                    String.join(";\n        ", putEntityFieldIntoMapCodeLines)
            );
        } else {
            entityFieldGetCode += String.format(
                    "" +
                            "    public static java.util.Map<Integer, Object> insertMapInputParameter(%s entity) {" +
                            "\n        if (entity.get%s() == null) {" +
                            "\n            throw new RuntimeException(\"ID is null\");" +
                            "\n        }" +
                            "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                            "\n        int startingPosition = 0;" +
                            "\n        %s;" +
                            "\n        return map;" +
                            "\n    }",
                    typeElement.getSimpleName(),
                    capitalize(idColumnFieldName),
                    String.join(";\n        ", putEntityFieldIntoMapCodeLines)
            );
        }

        insertStatement = "\n    public static String insertSql() {\n        " + "return \"" + insertStatement + "\";" + "\n    }";

        // System.out.println(entityFieldGetCode + "\n" + insertStatement);
        result += entityFieldGetCode + "\n" + insertStatement + "\n";
        return result;
    }

    public static String insertStatementBuilderV2(TypeElement typeElement, List<Element> classElements) {

        TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            return "";
        }
        String tableName;
        String tableNameValue = tableNameAnnotation.value();

        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                final String[] tableNameValueArray = tableNameValue.split(DOT);
                tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                tableName = schemaName + tableNameValue;
            }
        } else {
            tableName = tableNameValue;
        }

        String insertStatement = "INSERT INTO\\n    " + tableName;
        String entityFieldGetCode = CommonConstant.EMPTY_STRING;
        ArrayList<String> codeLines = new ArrayList<>();
        ArrayList<String> putEntityFieldIntoMapCodeLines = new ArrayList<>();
        String idColumnFieldName = CommonConstant.EMPTY_STRING;
        boolean idColumnIsNumber = false;

        for (Element classElement : classElements) {
            ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
            if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                continue;
            }
            codeLines.add(String.format(
                    "        if (entity.get%s() != null) {\n            cols.add(\"\\n        %s\");\n        }\n",
                    capitalize(classElement.getSimpleName().toString()),
                    columnNameAnnotation.name()
            ));
            codeLines.add(String.format(
                    "        if (entity.get%s() != null) {\n            params.add(\"\\n        ?\");\n        }\n",
                    capitalize(classElement.getSimpleName().toString())
            ));
            if (classElement.getAnnotation(Clob.class) == null) {
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, entity.get%1$s());\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
            } else {
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, vn.com.lcx.common.database.handler.resultset.SqlClobReader.convertStringToClob(vn.com.lcx.common.database.context.ConnectionContext.get(), entity.get%1$s()));\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
            }
        }
        return String.format(
                "\n" +
                        "    public static String insertSqlV2(%s entity) {\n" +
                        "        java.util.List<String> cols = new java.util.ArrayList<>();\n" +
                        "        java.util.List<String> params = new java.util.ArrayList<>();\n" +
                        "%s\n" +
                        "        return \"INSERT INTO\\n    \" + \"%s\" + \"(\" + String.join(\",\", cols) + \"\\n)\\n\" + \"VALUES\\n    \" + \"(\" + String.join(\",\", params) + \"\\n    )\";\n" +
                        "    }\n",
                typeElement.getSimpleName(),
                String.join("", codeLines),
                tableName
        ) + String.format(
                "\n" +
                        "    public static java.util.Map<Integer, Object> insertMapInputParameterV2(%s entity) {" +
                        "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                        "\n        int startingPosition = 0;" +
                        "\n%s" +
                        "\n        return map;" +
                        "\n    }\n\n",
                typeElement.getSimpleName(),
                String.join("", putEntityFieldIntoMapCodeLines)
        );
    }

    public static String updateStatementBuilder(TypeElement typeElement, List<Element> classElements) {

        String result = CommonConstant.EMPTY_STRING;

        TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            return result;
        }

        String tableName;
        String tableNameValue = tableNameAnnotation.value();

        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                final String[] tableNameValueArray = tableNameValue.split(DOT);
                tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                tableName = schemaName + tableNameValue;
            }
        } else {
            tableName = tableNameValue;
        }

        String updateStatement = "UPDATE\\n    " + tableName + "\\nSET\\n    ";
        ArrayList<String> columnList = new ArrayList<String>();
        String entityFieldGetCode = CommonConstant.EMPTY_STRING;
        ArrayList<String> entityFieldList = new ArrayList<String>();

        boolean idColumnIsNumber = false;

        String idColumnColumnName = CommonConstant.EMPTY_STRING;
        String idColumnFieldName = CommonConstant.EMPTY_STRING;

        for (Element classElement : classElements) {
            ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
            if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                continue;
            }
            IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
            if (idColumnAnnotation != null) {
                idColumnColumnName = columnNameAnnotation.name();
                idColumnFieldName = classElement.getSimpleName().toString();
                if (
                        classElement.asType().toString().equals("java.math.BigDecimal") ||
                                classElement.asType().toString().equals("java.lang.Long")
                ) {
                    idColumnIsNumber = true;
                }
                continue;
            }
            SecondaryIdColumn secondaryIdColumnAnnotation = classElement.getAnnotation(SecondaryIdColumn.class);
            if (secondaryIdColumnAnnotation != null && idColumnIsNumber) {
                idColumnColumnName = columnNameAnnotation.name();
                idColumnFieldName = classElement.getSimpleName().toString();
                continue;
            }

            columnList.add(columnNameAnnotation.name() + " = ?");
            entityFieldList.add(
                    String.format(
                            "map.put(++startingPosition, entity.get%s())",
                            capitalize(classElement.getSimpleName().toString())
                    )
            );
        }

        if (StringUtils.isBlank(idColumnColumnName) || StringUtils.isBlank(idColumnFieldName)) {
            return result;
        }

        entityFieldList.add(
                String.format(
                        "map.put(++startingPosition, entity.get%s())",
                        capitalize(idColumnFieldName)
                )
        );

        updateStatement += String.join(",\\n    ", columnList) + "\\nWHERE\\n    " + idColumnColumnName + " = ?";
        // updateStatement += String.join(", ", columnList) + "\\nWHERE\\n    " + idColumnColumnName + " = ?";

        entityFieldGetCode += String.format(
                "" +
                        "\n    public static java.util.Map<Integer, Object> updateMapInputParameter(%s entity) {" +
                        "\n        if (entity.get%s() == null) {" +
                        "\n            throw new RuntimeException(\"ID is null\");" +
                        "\n        }" +
                        "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                        "\n        int startingPosition = 0;" +
                        "\n        %s;" +
                        "\n        return map;" +
                        "\n    }",
                typeElement.getSimpleName(),
                capitalize(idColumnFieldName),
                String.join(";\n        ", entityFieldList)
        );
        updateStatement = "\n    public static String updateSql() {\n        " + "return \"" + updateStatement + "\";" + "\n    }";

        // System.out.println(entityFieldGetCode + "\n" + updateStatement);
        result += entityFieldGetCode + "\n" + updateStatement + "\n";
        return result;
    }

    public static String updateStatementBuilderV2(TypeElement typeElement, List<Element> classElements) {

        TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            return "";
        }
        String tableName;
        String tableNameValue = tableNameAnnotation.value();

        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                final String[] tableNameValueArray = tableNameValue.split(DOT);
                tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                tableName = schemaName + tableNameValue;
            }
        } else {
            tableName = tableNameValue;
        }

        String insertStatement = "INSERT INTO\\n    " + tableName;
        String entityFieldGetCode = CommonConstant.EMPTY_STRING;
        ArrayList<String> codeLines = new ArrayList<>();
        ArrayList<String> putEntityFieldIntoMapCodeLines = new ArrayList<>();
        boolean idColumnIsNumber = false;

        String idColumnColumnName = CommonConstant.EMPTY_STRING;
        String idColumnFieldName = CommonConstant.EMPTY_STRING;

        for (Element classElement : classElements) {
            ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
            if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                continue;
            }
            IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
            if (idColumnAnnotation != null) {
                idColumnColumnName = columnNameAnnotation.name();
                idColumnFieldName = classElement.getSimpleName().toString();
                continue;
            }
            codeLines.add(String.format(
                    "        if (entity.get%s() != null) {\n            cols.add(\"\\n    %s = ?\");\n        }\n",
                    capitalize(classElement.getSimpleName().toString()),
                    columnNameAnnotation.name()
            ));
            if (classElement.getAnnotation(Clob.class) == null) {
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, entity.get%1$s());\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
            } else {
                putEntityFieldIntoMapCodeLines.add(String.format(
                        "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, vn.com.lcx.common.database.handler.resultset.SqlClobReader.convertStringToClob(vn.com.lcx.common.database.context.ConnectionContext.get(), entity.get%1$s()));\n        }\n",
                        capitalize(classElement.getSimpleName().toString())
                ));
            }
        }
        if (StringUtils.isNotBlank(idColumnFieldName) && StringUtils.isNotBlank(idColumnColumnName)) {
            putEntityFieldIntoMapCodeLines.add(String.format(
                    "        if (entity.get%1$s() != null) {\n            map.put(++startingPosition, entity.get%1$s());\n        }\n",
                    capitalize(idColumnFieldName)
            ));
            return String.format(
                    "" +
                            "    public static String updateSqlV2(%s entity) {\n" +
                            "        java.util.List<String> cols = new java.util.ArrayList<>();\n" +
                            "%s\n" +
                            "        return \"UPDATE\\n    \" + \"%s\" + \"\\nSET\" + String.join(\",\", cols) + \"\\nWHERE %s = ?\";\n" +
                            "    }",
                    typeElement.getSimpleName(),
                    String.join("", codeLines),
                    tableName,
                    idColumnColumnName
            ) + String.format(
                    "\n" +
                            "\n    public static java.util.Map<Integer, Object> updateMapInputParameterV2(%s entity) {" +
                            "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                            "\n        int startingPosition = 0;" +
                            "\n%s" +
                            "\n        return map;" +
                            "\n    }\n",
                    typeElement.getSimpleName(),
                    String.join("", putEntityFieldIntoMapCodeLines)
            );
        }
        return "// no id field to generate\n";
    }

    public static String deleteStatementBuilder(TypeElement typeElement, List<Element> classElements) {

        String result = CommonConstant.EMPTY_STRING;

        TableName tableNameAnnotation = typeElement.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            return result;
        }

        String tableName;
        String tableNameValue = tableNameAnnotation.value();

        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                final String[] tableNameValueArray = tableNameValue.split(DOT);
                tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                tableName = schemaName + tableNameValue;
            }
        } else {
            tableName = tableNameValue;
        }

        String deleteStatement = "DELETE FROM\\n    " + tableName + "\\nWHERE\\n    ";
        ArrayList<String> columnList = new ArrayList<String>();
        String entityFieldGetCode = CommonConstant.EMPTY_STRING;
        ArrayList<String> entityFieldList = new ArrayList<String>();

        boolean idColumnIsNumber = false;

        String idColumnColumnName = CommonConstant.EMPTY_STRING;
        String idColumnFieldName = CommonConstant.EMPTY_STRING;

        for (Element classElement : classElements) {
            ColumnName columnNameAnnotation = classElement.getAnnotation(ColumnName.class);
            if (columnNameAnnotation == null || StringUtils.isBlank(columnNameAnnotation.name())) {
                continue;
            }
            IdColumn idColumnAnnotation = classElement.getAnnotation(IdColumn.class);
            if (idColumnAnnotation != null) {
                idColumnColumnName = columnNameAnnotation.name();
                idColumnFieldName = classElement.getSimpleName().toString();
                if (
                        classElement.asType().toString().equals("java.math.BigDecimal") ||
                                classElement.asType().toString().equals("java.lang.Long")
                ) {
                    idColumnIsNumber = true;
                }
                continue;
            }
            SecondaryIdColumn secondaryIdColumnAnnotation = classElement.getAnnotation(SecondaryIdColumn.class);
            if (secondaryIdColumnAnnotation != null && idColumnIsNumber) {
                idColumnColumnName = columnNameAnnotation.name();
                idColumnFieldName = classElement.getSimpleName().toString();
            }
        }

        if (StringUtils.isBlank(idColumnColumnName) || StringUtils.isBlank(idColumnFieldName)) {
            return result;
        }

        entityFieldList.add(
                String.format(
                        "map.put(++startingPosition, entity.get%s())",
                        capitalize(idColumnFieldName)
                )
        );

        deleteStatement += idColumnColumnName + " = ?";

        entityFieldGetCode += String.format(
                "" +
                        "\n    public static java.util.Map<Integer, Object> deleteMapInputParameter(%s entity) {" +
                        "\n        if (entity.get%s() == null) {" +
                        "\n            throw new RuntimeException(\"ID is null\");" +
                        "\n        }" +
                        "\n        java.util.Map<Integer, Object> map = new java.util.HashMap<>();" +
                        "\n        int startingPosition = 0;" +
                        "\n        %s;" +
                        "\n        return map;" +
                        "\n    }",
                typeElement.getSimpleName(),
                capitalize(idColumnFieldName),
                String.join(";\n        ", entityFieldList)
        );
        deleteStatement = "\n    public static String deleteSql() {\n        " + "return \"" + deleteStatement + "\";" + "\n    }";

        // System.out.println(entityFieldGetCode + "\n" + deleteStatement);
        result += entityFieldGetCode + "\n" + deleteStatement + "\n";
        return result;
    }

}
