package vn.com.lcx.common.database.reflect;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.SubTable;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.constant.JavaSqlResultSetConstant;
import vn.com.lcx.common.database.pageable.Direction;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.database.type.SubTableEntry;
import vn.com.lcx.common.utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static vn.com.lcx.common.constant.CommonConstant.BUILDER_MAP;
import static vn.com.lcx.common.database.utils.EntityUtils.getTableShortenedName;
import static vn.com.lcx.common.utils.MyStringUtils.removeSuffixOfString;

public final class SelectStatementBuilder {

    private static final List<String> WHERE_STATEMENT_DELIMITER_KEYWORDS = Arrays.asList(
            "NotEqual",
            "NotIn",
            "NotLike",
            "NotBetween",
            "NotNull",
            "Not",
            "Equal",
            "In",
            "Like",
            "Between",
            "Null",
            "LessThan",
            "GreaterThan",
            "LessEqual",
            "GreaterEqual"
    );

    private static final Pattern WHERE_STATEMENT_SPLIT_PATTERN;

    static {
        // Sort by keyword's length to avoid mismatching
        WHERE_STATEMENT_DELIMITER_KEYWORDS.sort(Comparator.comparingInt(String::length).reversed());
        String joinedKeywords = String.join("|", WHERE_STATEMENT_DELIMITER_KEYWORDS);
        WHERE_STATEMENT_SPLIT_PATTERN = Pattern.compile(joinedKeywords);
    }

    private final Class<?> entityClass;

    // private static volatile SelectStatementBuilder INSTANCE;
    private final ArrayList<String> listOfColumnName;
    private final ArrayList<Field> listOfField;
    private final String tableName;
    private final String tableNameShortenedName;
    private final ArrayList<SubTableEntry> subTableStatementBuilders;
    private final String placeHolder;

    private SelectStatementBuilder(Class<?> entityClass, boolean getSelfColumnOnly, String placeHolder, int... order) {
        this.entityClass = entityClass;
        this.placeHolder = placeHolder;

        final var tableNameAnnotation = entityClass.getAnnotation(TableName.class);

        if (tableNameAnnotation == null) {
            throw new IllegalArgumentException(String.format("%s must be annotated with @TableName", entityClass.getName()));
        }

        String tableNameValue = tableNameAnnotation.value();

        if (StringUtils.isNotBlank(tableNameAnnotation.schema())) {
            String schemaName = tableNameAnnotation.schema() + ".";
            if (tableNameValue.contains(".")) {
                final var tableNameValueArray = tableNameValue.split(JavaSqlResultSetConstant.DOT);
                this.tableName = schemaName + tableNameValueArray[tableNameValueArray.length - 1];
            } else {
                this.tableName = schemaName + tableNameValue;
            }
        } else {
            this.tableName = tableNameValue;
        }

        this.tableNameShortenedName = getTableShortenedName(tableName) + (order.length > 0 ? order[0] + "" : "");

        this.listOfColumnName = new ArrayList<>();
        this.listOfField = new ArrayList<>();
        this.subTableStatementBuilders = new ArrayList<>();

        final var listOfFields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));

        for (Field field : listOfFields) {
            listOfField.add(field);
            boolean isColumnNameAnnotationExisted = false;
            final var columnNameAnnotation = field.getAnnotation(ColumnName.class);
            if (columnNameAnnotation != null) {
                final var columnNameAnnotationValue = columnNameAnnotation.name();
                this.listOfColumnName.add(
                        String.format("%s.%s", this.tableNameShortenedName, columnNameAnnotation.name())
                        // String.format(
                        //         "%s.%s AS %s",
                        //         this.tableNameShortenedName,
                        //         columnNameAnnotationValue,
                        //         String.format("%s_%s", this.tableNameShortenedName.toUpperCase(), columnNameAnnotationValue)
                        // )
                );
                isColumnNameAnnotationExisted = true;
            }
            if (getSelfColumnOnly) {
                continue;
            }
            try {
                final var subTableAnnotation = field.getAnnotation(SubTable.class);
                if (subTableAnnotation != null) {
                    final SelectStatementBuilder selectStatementBuilderOfSubTable;
                    final Class<?> clz;
                    if (field.getType().isAssignableFrom(List.class)) {
                        final var genericType = (ParameterizedType) field.getGenericType();
                        final var type = genericType.getActualTypeArguments()[0];
                        clz = Class.forName(type.getTypeName());
                    } else {
                        clz = field.getType();
                    }
                    if (clz.isAssignableFrom(this.entityClass)) {
                        throw new IllegalArgumentException("1 - 1 relation is currently not supported");
                        // selectStatementBuilderOfSubTable = new SelectStatementBuilder(clz, true, ++order2);
                    } else {
                        // selectStatementBuilderOfSubTable = new SelectStatementBuilder(clz, true);
                        selectStatementBuilderOfSubTable = SelectStatementBuilder.of(clz);
                    }
                    final var optionalMatchedField = selectStatementBuilderOfSubTable
                            .getListOfField().stream().filter(f -> f.getName().equals(subTableAnnotation.mapField())).findFirst();
                    if (optionalMatchedField.isEmpty()) {
                        throw new RuntimeException("Cannot find appropriate field of sub class");
                    }
                    final var matchedField = optionalMatchedField.get();
                    if (!isColumnNameAnnotationExisted) {
                        this.listOfColumnName.add(
                                String.format(
                                        "%s.%s AS %s",
                                        this.tableNameShortenedName,
                                        subTableAnnotation.columnName(),
                                        String.format("%s_%s", this.tableNameShortenedName.toUpperCase(), subTableAnnotation.columnName())
                                )
                        );
                    }
                    final var subTableEntry = SubTableEntry.builder()
                            .field(field)
                            .joinType(subTableAnnotation.joinType())
                            .columnName(subTableAnnotation.columnName())
                            .matchField(matchedField)
                            .selectStatementBuilder(selectStatementBuilderOfSubTable)
                            .build();
                    this.subTableStatementBuilders.add(subTableEntry);
                }
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                LogUtils.writeLog(e.getMessage(), e);
            }
        }
    }

    private static List<String> splitByKeywords(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = WHERE_STATEMENT_SPLIT_PATTERN.matcher(input);

        int lastIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                result.add(input.substring(lastIndex, matcher.start()));
            }
            result.add(matcher.group());
            lastIndex = matcher.end();
        }

        if (lastIndex < input.length()) {
            result.add(input.substring(lastIndex));
        }

        return result;
    }

    private static SelectStatementBuilder of(Class<?> entityClass) {
        return of(entityClass, "?");
    }

    private static SelectStatementBuilder of(Class<?> entityClass, String placeHolder) {
        return BUILDER_MAP.computeIfAbsent(entityClass.getName(), key -> new SelectStatementBuilder(entityClass, false, placeHolder));
    }

    private Class<?> getEntityClass() {
        return entityClass;
    }

    private ArrayList<String> getListOfColumnName() {
        return listOfColumnName;
    }

    private ArrayList<Field> getListOfField() {
        return listOfField;
    }

    private String getTableName() {
        return tableName;
    }

    private String getTableNameShortenedName() {
        return tableNameShortenedName;
    }

    private ArrayList<SubTableEntry> getSubTableStatementBuilders() {
        return subTableStatementBuilders;
    }

    private String getPlaceHolder() {
        return placeHolder;
    }

    private String build(String methodName, Object... parameters) {
        String statement = CommonConstant.STATEMENT_OF_METHOD.get(entityClass.getName() + methodName);
        if (StringUtils.isBlank(statement)) {
            if (methodName.startsWith("count")) {
                statement = this.generateCountStatement();
            } else {
                statement = this.generateSqlStatement();
            }
            if (StringUtils.isNotBlank(methodName) && !methodName.equals("findAll") && !methodName.equals("countAll")) {
                statement = String.format(
                        "%s\nWHERE\n    %s",
                        statement,
                        this.parseMethodNameIntoConditionStatement(methodName, new ArrayList<>(Arrays.asList(parameters)))
                );
            } else {
                statement = statement + (
                        parameters.length != 0 && Pageable.class.isAssignableFrom(parameters[0].getClass()) ?
                                ((Pageable) parameters[0]).toSql() : CommonConstant.EMPTY_STRING
                );
            }
            CommonConstant.STATEMENT_OF_METHOD.put(entityClass.getName() + methodName, statement);
        }
        return statement;
    }

    private String buildFullJoin(String methodName, Object... parameters) {
        final String statement;
        if (methodName.startsWith("count")) {
            statement = this.generateCountStatement();
        } else {
            statement = this.generateSqlStatementFullJoin();
        }
        if (StringUtils.isNotBlank(methodName) && !methodName.equals("findAll")) {
            return String.format(
                    "%s\nWHERE\n    %s",
                    statement,
                    this.parseMethodNameIntoConditionStatement(methodName, new ArrayList<>(Arrays.asList(parameters)))
            );
        } else {
            return statement;
        }
    }

    private String generateSqlStatement() {
        final var tableNameWithShortenedName = String.format("%s %s", this.tableName, this.tableNameShortenedName);
        return String.format(
                "SELECT\n    %s\nFROM\n    %s",
                String.join(",\n    ", this.listOfColumnName),
                // String.join(", ", this.listOfColumnName),
                tableNameWithShortenedName
        );
    }

    private String generateCountStatement() {
        final var tableNameWithShortenedName = String.format("%s %s", this.tableName, this.tableNameShortenedName);
        return String.format(
                "SELECT\n    COUNT(1)\nFROM\n    %s",
                tableNameWithShortenedName
        );
    }

    private String generateSqlStatementFullJoin() {
        final var tableNameWithShortenedName = String.format("%s %s", this.tableName, this.tableNameShortenedName);
        final var listColumn = this.listOfColumnName;
        for (SubTableEntry subTableStatementBuilder : this.subTableStatementBuilders) {
            listColumn.addAll(subTableStatementBuilder.getSelectStatementBuilder().getListOfColumnName());
        }
        return String.format(
                "SELECT\n    %s\nFROM\n    %s%s",
                String.join(",\n    ", listColumn),
                tableNameWithShortenedName,
                subTableJoinStatementBuilder()
        );
    }

    private String subTableJoinStatementBuilder() {
        final List<String> result = new ArrayList<>();
        for (SubTableEntry subTableStatementBuilder : this.subTableStatementBuilders) {
            final var tableNameWithShortenedName = String.format("%s %s", subTableStatementBuilder.getSelectStatementBuilder().getTableName(), subTableStatementBuilder.getSelectStatementBuilder().getTableNameShortenedName());
            result.add(
                    String.format(
                            "%1$s %2$s ON %3$s.%4$s = %5$s.%6$s",
                            subTableStatementBuilder.getJoinType().getStatement(),
                            tableNameWithShortenedName,
                            subTableStatementBuilder.getSelectStatementBuilder().getTableNameShortenedName(),
                            subTableStatementBuilder.getMatchField().getAnnotation(ColumnName.class).name(),
                            this.tableNameShortenedName,
                            subTableStatementBuilder.getColumnName()
                    )
            );
        }
        return result.isEmpty() ? CommonConstant.EMPTY_STRING : "\n    " + String.join("\n    ", result);
    }

    private String parseMethodNameIntoConditionStatement(String methodName, ArrayList<Object> parameters) {
        if (
                !(methodName.startsWith("findBy")) &&
                        !(methodName.startsWith(String.format("find%sBy", this.entityClass.getSimpleName()))) &&
                        !(methodName.startsWith("countBy")) &&
                        !(methodName.startsWith(String.format("count%sBy", this.entityClass.getSimpleName())))
        ) {
            throw new IllegalArgumentException(String.format("%s must start with `findBy`", methodName));
        }
        final ArrayList<String> partsOfMethod;
        if (methodName.startsWith("find")) {
            partsOfMethod = new ArrayList<>(
                    Arrays.asList(
                            methodName.startsWith("findBy") ?
                                    methodName.substring(6).split("(?=And|OrderBy|Or)|(?<=And|OrderBy|Or)(?=[A-Z])") :
                                    methodName.substring(6 + this.entityClass.getSimpleName().length()).split("(?=And|OrderBy|Or)|(?<=And|OrderBy|Or)(?=[A-Z])")
                    )
            );
        } else {
            partsOfMethod = new ArrayList<>(
                    Arrays.asList(
                            methodName.startsWith("countBy") ?
                                    methodName.substring(7).split("(?=And|OrderBy|Or)|(?<=And|OrderBy|Or)(?=[A-Z])") :
                                    methodName.substring(7 + this.entityClass.getSimpleName().length()).split("(?=And|OrderBy|Or)|(?<=And|OrderBy|Or)(?=[A-Z])")
                    )
            );
        }

        final var conditionSQLStatement = new ArrayList<String>();
        final var listOfOrderByStatement = new ArrayList<String>();
        final var handledParameterIndexThatIsAList = new HashSet<Integer>();

        boolean meetOrderByStatement = false;

        int count = 0;

        for (String part : partsOfMethod) {

            if (!meetOrderByStatement) {

                if ("or".equalsIgnoreCase(part)) {
                    conditionSQLStatement.add("\n    OR ");
                } else if ("and".equalsIgnoreCase(part)) {
                    conditionSQLStatement.add("\n    AND ");
                } else if ("orderby".equalsIgnoreCase(part)) {
                    meetOrderByStatement = true;
                } else {
                    final var currentFieldParts = splitByKeywords(part);
                    final Optional<String> columnNameOptional;
                    final String columnName, tblShortName;

                    if (currentFieldParts.get(0).contains("_")) {
                        final var currentFieldParts2 = new ArrayList<String>(Arrays.asList(currentFieldParts.get(0).split("_")));
                        SelectStatementBuilder subTableClass = this;
                        for (int i = 0; i < currentFieldParts2.size() - 1; i++) {
                            int finalI = i;
                            final var optionalSubTableFieldDataType = subTableClass.getListOfField().stream().filter(f -> f.getName().equalsIgnoreCase(currentFieldParts2.get(finalI))).findFirst();
                            if (optionalSubTableFieldDataType.isEmpty()) {
                                throw new IllegalArgumentException("Unknown field");
                            }
                            subTableClass = SelectStatementBuilder.of(optionalSubTableFieldDataType.get().getType());
                        }
                        final var optionalSubTableFieldDataType = subTableClass.getListOfField().stream().filter(f -> f.getName().equalsIgnoreCase(currentFieldParts2.get(currentFieldParts2.size() - 1))).findFirst();
                        if (optionalSubTableFieldDataType.isEmpty()) {
                            throw new IllegalArgumentException("Unknown field");
                        }
                        columnNameOptional = subTableClass.getListOfField()
                                .stream()
                                .filter(f -> f.getName().equalsIgnoreCase(currentFieldParts2.get(1)))
                                .map(field -> field.getAnnotation(ColumnName.class).name())
                                .findFirst();
                        tblShortName = subTableClass.getTableNameShortenedName();

                    } else {
                        columnNameOptional = this.listOfField
                                .stream()
                                .filter(
                                        field ->
                                                field.getName().equalsIgnoreCase(currentFieldParts.get(0)) &&
                                                        field.getAnnotation(ColumnName.class) != null
                                )
                                .findAny()
                                .map(field -> field.getAnnotation(ColumnName.class).name());
                        tblShortName = this.tableNameShortenedName;
                    }

                    if (columnNameOptional.isEmpty()) {
                        throw new RuntimeException(
                                String.format(
                                        "Method part name %s can not found the matching column",
                                        currentFieldParts.get(0)
                                )
                        );
                    }

                    columnName = String.format("%s.%s", tblShortName, columnNameOptional.get());
                    if (currentFieldParts.size() == 2) {
                        final var condition = currentFieldParts.get(1).toLowerCase();
                        switch (condition) {
                            case "notnull":
                                conditionSQLStatement.add(String.format("%s IS NOT NULL", columnName));
                                break;
                            case "notequal":
                            case "not":
                                if (placeHolder.equals("?")) {
                                    conditionSQLStatement.add(String.format("%s <> ?", columnName));
                                } else {
                                    conditionSQLStatement.add(String.format("%s <> %s%s", columnName, placeHolder, ++count));
                                }
                                break;
                            case "notin":
                            case "in":
                                // final var parameterWithListDataType = (List<?>) parameters.stream().filter(o -> o instanceof List<?>).findFirst().orElse(null);
                                List<?> parameterWithListDataType = null;
                                for (int i = 0; i < parameters.size(); i++) {
                                    if (handledParameterIndexThatIsAList.contains(i)) {
                                        continue;
                                    }
                                    if (parameters.get(i) instanceof List<?>) {
                                        parameterWithListDataType = (List<?>) parameters.get(i);
                                        handledParameterIndexThatIsAList.add(i);
                                        break;
                                    }
                                }
                                if (parameterWithListDataType == null) {
                                    throw new RuntimeException("In condition statement must contain at least 1 collection parameter");
                                }
                                if (placeHolder.equals("?")) {
                                    if (parameterWithListDataType.isEmpty()) {
                                        conditionSQLStatement.add(
                                                String.format(
                                                        "%s %s (NULL)",
                                                        columnName,
                                                        condition.equals("in") ? "IN" : "NOT IN"
                                                )
                                        );
                                    } else {
                                        conditionSQLStatement.add(
                                                String.format(
                                                        "%s %s (%s)",
                                                        columnName,
                                                        condition.equals("in") ? "IN" : "NOT IN",
                                                        parameterWithListDataType.stream()
                                                                .map(a -> "?")
                                                                .collect(Collectors.joining(", "))
                                                )
                                        );
                                    }
                                } else {
                                    final var statement = new StringBuilder(columnName).append(" ")
                                            .append(condition.equals("in") ? "IN" : "NOT IN").append(" ");
                                    if (parameterWithListDataType.isEmpty()) {
                                        statement.append("(")
                                                .append("NULL")
                                                .append(")");
                                    } else {
                                        statement.append("(");
                                        final List<String> placeholderArr = new ArrayList<>();
                                        for (Object ignored : parameterWithListDataType) {
                                            placeholderArr.add(
                                                    String.format(
                                                            "%s%s",
                                                            placeHolder,
                                                            ++count
                                                    )
                                            );
                                        }
                                        statement.append(
                                                String.join(", ", placeholderArr)
                                        );
                                        statement.append(")");
                                    }
                                    conditionSQLStatement.add(statement.toString());
                                    // conditionSQLStatement.add(
                                    //         String.format(
                                    //                 "%s %s %s%s",
                                    //                 columnName,
                                    //                 condition.equals("in") ? "IN" : "NOT IN",
                                    //                 placeHolder,
                                    //                 ++count
                                    //         )
                                    // );
                                }
                                break;
                            case "notlike":
                            case "like":
                                if (condition.equals("like")) {
                                    // conditionSQLStatement.add(columnName + " LIKE '%' || ? || '%'");
                                    if (placeHolder.equals("?")) {
                                        conditionSQLStatement.add(columnName + " LIKE ?");
                                    } else {
                                        conditionSQLStatement.add(columnName + " LIKE " + placeHolder + (++count));
                                    }
                                } else {
                                    // conditionSQLStatement.add(columnName + " NOT LIKE '%' || ? || '%'");
                                    if (placeHolder.equals("?")) {
                                        conditionSQLStatement.add(columnName + " NOT LIKE ?");
                                    } else {
                                        conditionSQLStatement.add(columnName + " NOT LIKE " + placeHolder + (++count));
                                    }
                                }
                                break;
                            case "notbetween":
                            case "between":
                                if (condition.equals("between")) {
                                    if (placeHolder.equals("?")) {
                                        conditionSQLStatement.add(String.format("%s BETWEEN ? AND ?", columnName));
                                    } else {
                                        conditionSQLStatement.add(String.format("%s BETWEEN %s%s AND %s%s", columnName, placeHolder, ++count, placeHolder, ++count));
                                    }
                                } else {
                                    if (placeHolder.equals("?")) {
                                        conditionSQLStatement.add(String.format("%s NOT BETWEEN ? AND ?", columnName));
                                    } else {
                                        conditionSQLStatement.add(String.format("%s NOT BETWEEN %s%s AND %s%s", columnName, placeHolder, ++count, placeHolder, ++count));
                                    }
                                }
                                break;
                            case "lessthan":
                                if (placeHolder.equals("?")) {
                                    conditionSQLStatement.add(String.format("%s BETWEEN < ?", columnName));
                                } else {
                                    conditionSQLStatement.add(String.format("%s BETWEEN < %s%s", columnName, placeHolder, ++count));
                                }
                                break;
                            case "greaterthan":
                                if (placeHolder.equals("?")) {
                                    conditionSQLStatement.add(String.format("%s BETWEEN > ?", columnName));
                                } else {
                                    conditionSQLStatement.add(String.format("%s BETWEEN > %s%s", columnName, placeHolder, ++count));
                                }
                                break;
                            case "lessequal":
                                if (placeHolder.equals("?")) {
                                    conditionSQLStatement.add(String.format("%s BETWEEN <= ?", columnName));
                                } else {
                                    conditionSQLStatement.add(String.format("%s BETWEEN <= %s%s", columnName, placeHolder, ++count));
                                }
                                break;
                            case "greaterequal":
                                if (placeHolder.equals("?")) {
                                    conditionSQLStatement.add(String.format("%s BETWEEN >= ?", columnName));
                                } else {
                                    conditionSQLStatement.add(String.format("%s BETWEEN >= %s%s", columnName, placeHolder, ++count));
                                }
                                break;
                            case "null":
                                conditionSQLStatement.add(String.format("%s IS NULL", columnName));
                                break;
                            case "equal":
                            default:
                                if (placeHolder.equals("?")) {
                                    conditionSQLStatement.add(String.format("%s = ?", columnName));
                                } else {
                                    conditionSQLStatement.add(String.format("%s = %s%s", columnName, placeHolder, ++count));
                                }
                                break;
                        }
                    } else {
                        if (placeHolder.equals("?")) {
                            conditionSQLStatement.add(String.format("%s = ?", columnName));
                        } else {
                            conditionSQLStatement.add(String.format("%s = %s%s", columnName, placeHolder, ++count));
                        }
                    }

                }
            } else {

                if ("or".equalsIgnoreCase(part) || "and".equalsIgnoreCase(part)) {
                    continue;
                }

                final var direction = part.toLowerCase().endsWith("desc") ? Direction.DESC : Direction.ASC;
                final var columnNameOptional = this.listOfField
                        .stream()
                        .filter(
                                field ->
                                        (
                                                field.getName().equalsIgnoreCase(removeSuffixOfString(part.toLowerCase(), "desc")) ||
                                                        field.getName().equalsIgnoreCase(removeSuffixOfString(part.toLowerCase(), "asc"))
                                        ) &&
                                                field.getAnnotation(ColumnName.class) != null
                        )
                        .findAny()
                        .map(field -> field.getAnnotation(ColumnName.class).name());
                if (!columnNameOptional.isPresent()) {
                    throw new RuntimeException(
                            String.format(
                                    "Method part name %s can not found the matching column",
                                    part
                            )
                    );
                }
                final var columnName = String.format("%s.%s", this.tableNameShortenedName, columnNameOptional.get());
                listOfOrderByStatement.add(
                        String.format(
                                "%s %s",
                                columnName,
                                direction.name()
                        )
                );
            }
        }

        if (methodName.startsWith("count")) {
            return String.join("", conditionSQLStatement);
        }
        String format;
        if (!listOfOrderByStatement.isEmpty()) {
            format = String.format(
                    "%s\nORDER BY %s",
                    String.join("", conditionSQLStatement),
                    String.join(", ", listOfOrderByStatement)
            );
        } else {
            format = String.join("", conditionSQLStatement);
        }
        if (parameters.get(parameters.size() - 1) instanceof Pageable && !(methodName.startsWith("count"))) {
            final var page = (Pageable) parameters.get(parameters.size() - 1);
            if (listOfOrderByStatement.isEmpty()) {
                page.setEntityClass(this.entityClass);
                page.fieldToColumn();
                return String.join("", conditionSQLStatement) + "\n" + page.toSql().replaceFirst(" ", "");
            } else {
                page.setColumnNameAndDirectionMap(null);
                return format + page.toSql().replaceFirst(" ", "");
            }
        }
        return format;
    }

}
