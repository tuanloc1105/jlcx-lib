package vn.com.lcx.common.processor;

import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.ColumnName;
import vn.com.lcx.common.annotation.IdColumn;
import vn.com.lcx.common.annotation.Modifying;
import vn.com.lcx.common.annotation.Query;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.constant.CommonConstant;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static vn.com.lcx.common.constant.JavaSqlResultSetConstant.DOT;
import static vn.com.lcx.common.utils.WordCaseUtils.convertCamelToConstant;

public class SQLParser {

    private final Types typeUtils;
    private final Elements elementUtils;
    private final ArrayList<MethodInfo> allMethods;
    private final Map<String, HashSet<Element>> entitiesClass;
    private TypeElement genericEntityClass;
    private ArrayList<MethodInfo> allCountMethod;

    public SQLParser(TypeElement clazz, Map<String, HashSet<Element>> entitiesClass, Types typeUtils, Elements elementUtils) {

        boolean classIsNotInterface = !(clazz.getKind() == ElementKind.INTERFACE);

        if (classIsNotInterface) {
            throw new IllegalArgumentException("Invalid class " + clazz.getSimpleName() + ". Only apply for Interface");
        }

        this.allMethods = new ArrayList<>();

        // Get interface class methods
        List<ExecutableElement> allMethodsOfClass = elementUtils.getAllMembers(clazz).stream().filter(e -> {
            boolean elementIsAMethod = e.getKind() == ElementKind.METHOD;
            boolean isNotStaticAndFinal = !(e.getModifiers().contains(Modifier.FINAL) || e.getModifiers().contains(Modifier.STATIC));
            boolean notHashCodeMethod = !"hashCode".equalsIgnoreCase(e.getSimpleName().toString());
            boolean notEqualsMethod = !"equals".equalsIgnoreCase(e.getSimpleName().toString());
            boolean notToStringMethod = !"toString".equalsIgnoreCase(e.getSimpleName().toString());
            return elementIsAMethod && isNotStaticAndFinal && notHashCodeMethod && notEqualsMethod && notToStringMethod;
        }).map(member -> (ExecutableElement) member).collect(Collectors.toList());
        // Traverse superclass
        TypeElement typeElement = clazz;
        TypeMirror superclass = typeElement.getSuperclass();
        while (superclass != null && superclass.getKind().equals(TypeKind.DECLARED)) {
            Element superElement = typeUtils.asElement(superclass);
            if (superElement instanceof TypeElement) {
                TypeElement superTypeElement = (TypeElement) superElement;
                allMethodsOfClass.addAll(elementUtils.getAllMembers(clazz).stream().filter(e -> {
                    boolean elementIsAMethod = e.getKind() == ElementKind.METHOD;
                    boolean isNotStaticAndFinal = !(e.getModifiers().contains(Modifier.FINAL) || e.getModifiers().contains(Modifier.STATIC));
                    boolean notHashCodeMethod = "hashCode".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notEqualsMethod = "equals".equalsIgnoreCase(e.getSimpleName().toString());
                    boolean notToStringMethod = "toString".equalsIgnoreCase(e.getSimpleName().toString());
                    return elementIsAMethod && isNotStaticAndFinal && notHashCodeMethod && notEqualsMethod && notToStringMethod;
                }).map(member -> (ExecutableElement) member).collect(Collectors.toList()));
                typeElement = superTypeElement;
                superclass = typeElement.getSuperclass();
            } else {
                break;
            }
        }
        // get generic type of interface
        TypeElement baseRepositoryElement = elementUtils.getTypeElement("vn.com.lcx.common.database.repository.LCXRepository");
        TypeMirror baseRepositoryType = baseRepositoryElement.asType();
        TypeMirror mirror = clazz.asType();
        List<? extends TypeMirror> mirrors = typeUtils.directSupertypes(mirror);
        for (TypeMirror it : mirrors) {
            if (it.getKind() == TypeKind.DECLARED) {
                // this element is super class's element, do anything in here
                Element element = ((DeclaredType) it).asElement();
                if (typeUtils.isAssignable(element.asType(), baseRepositoryType)) {
                    List<? extends TypeMirror> typeArguments = ((DeclaredType) it).getTypeArguments();
                    if (typeArguments.isEmpty()) {
                        throw new RuntimeException("base repository must have a entity class");
                    }
                    this.genericEntityClass = elementUtils.getTypeElement(typeArguments.get(0).toString());
                    TableName myRepository = this.genericEntityClass.getAnnotation(TableName.class);
                    if (myRepository == null) {
                        throw new RuntimeException("base repository must be annotated by TableName");
                    }
                }
            }
        }

        for (ExecutableElement method : allMethodsOfClass) {
            String methodName = method.getSimpleName() + CommonConstant.EMPTY_STRING;
            List<? extends VariableElement> parameters = method.getParameters();
            TypeMirror returnType = method.getReturnType();

            if (returnType.toString().contains("java.util.Map<")) {
                continue;
            }

            Query queryAnnotation = method.getAnnotation(Query.class);
            if (queryAnnotation != null) {
                Modifying modifying = method.getAnnotation(Modifying.class);
                this.allMethods.add(new MethodInfo(methodName, parameters, returnType, queryAnnotation.nativeQuery(), modifying != null));
            } else {
                this.allMethods.add(new MethodInfo(methodName, parameters, returnType, null, false));
            }
        }
        this.entitiesClass = entitiesClass;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
    }

    private String lowerCaseFirstChar(String str) {
        if (str == null || str.isEmpty()) {
            return str;  // Return the string as is if it's null or empty
        }

        // Convert the first character to lowercase and concatenate with the rest of the string
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public String parseRepositoryMethods() {
        List<String> result = new ArrayList<>();

        final List<MethodInfo> methodsToParse = allMethods.stream().filter(method -> (method.getMethodName().startsWith("find") || method.getMethodName().startsWith("count")) && (!method.isModifying() && StringUtils.isBlank(method.getNativeQueryValue())) && !method.getMethodName().equals("find")).collect(Collectors.toList());

        for (MethodInfo methodInfo : methodsToParse) {
            final String returnClass = methodInfo.getOutputParameter() + CommonConstant.EMPTY_STRING;
            final String builderClassOfReturnClass;
            final String returnDatatype;
            if (methodInfo.getOutputParameter() instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) methodInfo.getOutputParameter();
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    TypeMirror typeArgument = typeArguments.get(0);
                    builderClassOfReturnClass = typeArgument + "Builder";
                    returnDatatype = typeArgument + CommonConstant.EMPTY_STRING;
                } else {
                    builderClassOfReturnClass = methodInfo.getOutputParameter() + "Builder";
                    returnDatatype = methodInfo.getOutputParameter() + CommonConstant.EMPTY_STRING;
                }
            } else {
                HashSet<Element> tempElements = entitiesClass.get(returnClass);
                if (tempElements == null || tempElements.isEmpty()) {
                    tempElements = entitiesClass.get(genericEntityClass.toString());
                }
                builderClassOfReturnClass = returnClass + "Builder";
                returnDatatype = methodInfo.getOutputParameter() + CommonConstant.EMPTY_STRING;
            }
            final List<String> asd = methodInfo.getInputParameters().stream().filter(e -> !"vn.com.lcx.common.database.pool.entry.ConnectionEntry".equals(e.asType().toString())).map(e -> String.format("%s", e.getSimpleName())).collect(Collectors.toList());
            String sqlStatement = String.format("vn.com.lcx.common.database.reflect.SelectStatementBuilder.of(%s.class).build(\"%s\"%s);", this.genericEntityClass.getQualifiedName(), methodInfo.getMethodName(), asd.isEmpty() ? "" : ", " + asd.stream().collect(Collectors.joining(", ")));
            // if (methodInfo.getInputParameters().isEmpty()) {
            //     throw new RuntimeException("Invalid method input parameters");
            // }
            String databaseExecutorCode;
            String mapPutCode;

            boolean havePageableParameter = false;
            String pageableParameterName = "";

            if (methodInfo.getInputParameters().isEmpty()) {
                mapPutCode = CommonConstant.EMPTY_STRING;
                if (methodInfo.getMethodName().startsWith("count")) {
                    databaseExecutorCode = "" + "executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                null,\n" + "                resultSet -> {\n" + "                    try {\n" + "                        return resultSet.getInt(1);\n" + "                    } catch (java.sql.SQLException sqlException) {\n" + "                        return 0;\n" + "                    }\n" + "                }\n" + "        )";
                } else {
                    databaseExecutorCode = String.format("" + "executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                null,\n" + "                %s::resultSetMapping\n" + "        )", builderClassOfReturnClass);
                }
            } else {
                mapPutCode = !methodInfo.getInputParameters().isEmpty() ? "" + "java.util.Map<Integer, Object> map = new java.util.HashMap<>();\n" + "        int startingPosition = 0;" : CommonConstant.EMPTY_STRING;
                for (int i = 0; i < methodInfo.getInputParameters().size(); i++) {
                    // skip pageable input parameter
                    if (this.typeUtils.isAssignable(methodInfo.getInputParameters().get(i).asType(), elementUtils.getTypeElement("vn.com.lcx.common.database.pageable.Pageable").asType())) {
                        havePageableParameter = true;
                        pageableParameterName = methodInfo.getInputParameters().get(i).getSimpleName() + "";
                        continue;
                    }
                    if (methodInfo.getInputParameters().get(i).asType().toString().contains("List<")) {

                        TypeMirror typeMirror = methodInfo.getInputParameters().get(i).asType();

                        if (typeMirror instanceof DeclaredType) {
                            DeclaredType declaredType = (DeclaredType) typeMirror;
                            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                            mapPutCode += String.format("\n" + "        for (%s o : %s) {\n" + "            map.put(++startingPosition, o);\n" + "        }", typeArguments.get(0).toString(), methodInfo.getInputParameters().get(i).getSimpleName());
                        } else {
                            mapPutCode += String.format("\n" + "        for (val o : %s) {\n" + "            map.put(++startingPosition, o);\n" + "        }", methodInfo.getInputParameters().get(i).getSimpleName());
                        }

                    } else {
                        mapPutCode += String.format("\n        map.put(++startingPosition, %s);", methodInfo.getInputParameters().get(i).getSimpleName());
                    }
                }
                if (methodInfo.getMethodName().startsWith("count")) {
                    databaseExecutorCode = "" + "executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                map,\n" + "                resultSet -> {\n" + "                    try {\n" + "                        return resultSet.getInt(1);\n" + "                    } catch (java.sql.SQLException sqlException) {\n" + "                        return 0;\n" + "                    }\n" + "                }\n" + "        )";
                } else {
                    databaseExecutorCode = String.format("" + "executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                map,\n" + "                %s::resultSetMapping\n" + "        )", builderClassOfReturnClass);
                }
            }
            String implementMethod;
            if (methodInfo.getMethodName().startsWith("find")) {

                String returnCodeLine;
                boolean isReturnPage = false;

                if (returnClass.contains("vn.com.lcx.common.database.pageable.Page")) {
                    isReturnPage = true;
                    returnCodeLine = String.format("return vn.com.lcx.common.database.pageable.Page.<%s>create(\n" + "            sqlResult,\n" + "            count,\n" + "            %s.getPageNumber(),\n" + "            %s.getPageSize()\n" + "        )", this.genericEntityClass.getQualifiedName(), pageableParameterName, pageableParameterName);
                } else {
                    returnCodeLine = returnClass.contains(".List<") ? "return sqlResult.isEmpty() ? new ArrayList<>() : sqlResult" : String.format("return sqlResult.isEmpty() ? new %s() : sqlResult.get(0)", returnClass);
                }

                implementMethod = String.format("\n" + "    @Override\n" + "    public %s %s(%s) {\n" + "        String sql = %s;\n" + "        %s\n" + "        java.util.List<%s> sqlResult = %s;\n" + "        if (sqlResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "        %s\n" + "        %s;\n" + "        %s;\n" + "    }", returnClass, methodInfo.getMethodName(), methodInfo.getInputParameters().stream().map(e -> String.format("%s %s", "T".equals(e.asType().toString()) ? this.genericEntityClass.getQualifiedName() : e.asType(), e.getSimpleName())).collect(Collectors.joining(", ")), sqlStatement, mapPutCode, returnDatatype, databaseExecutorCode, !returnClass.contains(".List<") && !returnClass.contains(".Page<") ? "\n" + "        if (sqlResult != null && sqlResult.size() > 1) {\n" + "            throw new java.lang.RuntimeException(\"Result return more than 1\");\n" + "        }" : "", havePageableParameter && isReturnPage ? String.format("java.util.List<Integer> countResult = executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                vn.com.lcx.common.database.reflect.SelectStatementBuilder.of(%s.class).build(\"%s\"%s),\n" + "                map,\n" + "                resultSet -> {\n" + "                    try {\n" + "                        return resultSet.getInt(1);\n" + "                    } catch (java.sql.SQLException sqlException) {\n" + "                        return 0;\n" + "                    }\n" + "                }\n" + "        );\n" + "        if (countResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "        int count = countResult.isEmpty() ? 0 : countResult.get(0)", this.genericEntityClass.getQualifiedName(), methodInfo.getMethodName().replaceFirst("find", "count"), asd.isEmpty() ? "" : ", " + asd.stream().collect(Collectors.joining(", "))) : "", returnCodeLine);
            } else {
                implementMethod = String.format("\n" + "    @Override\n" + "    public %s %s(%s) {\n" + "        String sql = %s;\n" + "        %s\n" + "        java.util.List<Integer> sqlResult = %s;\n" + "        if (sqlResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "        return sqlResult.isEmpty() ? 0 : sqlResult.get(0);\n" + "    }", returnClass, methodInfo.getMethodName(), methodInfo.getInputParameters().stream().map(e -> String.format("%s %s", "T".equals(e.asType().toString()) ? this.genericEntityClass.getQualifiedName() : e.asType(), e.getSimpleName())).collect(Collectors.joining(", ")), sqlStatement, mapPutCode, databaseExecutorCode);
            }
            result.add(implementMethod);
        }
        return String.join(System.lineSeparator(), result);
    }

    public String parseExecutionMethodCode() {
        String save = String.format("\n" + "    @Override\n" + "    public int save(%1$s entity) {\n" + "        String sql = %1$sBuilder.insertSqlV2(entity);\n" + "        int rowAffected = executor.executeMutation(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %1$sBuilder.insertMapInputParameterV2(entity)\n" + "        );\n" + "        return rowAffected;\n" + "    }\n", this.genericEntityClass.getQualifiedName());
        String update = String.format("\n" + "    @Override\n" + "    public int update(%1$s entity) {\n" + "        String sql = %1$sBuilder.updateSqlV2(entity);\n" + "        int rowAffected = executor.executeMutation(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %1$sBuilder.updateMapInputParameterV2(entity)\n" + "        );\n" + "        return rowAffected;\n" + "    }\n", this.genericEntityClass.getQualifiedName());
        String delete = String.format("\n" + "    @Override\n" + "    public int delete(%1$s entity) {\n" + "        String sql = %1$sBuilder.deleteSql();\n" + "        int rowAffected = executor.executeMutation(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %1$sBuilder.deleteMapInputParameter(entity)\n" + "        );\n" + "        return rowAffected;\n" + "    }\n", this.genericEntityClass.getQualifiedName());
        return save + update + delete;
    }

    public String parseReturnIdSaveMethod() {
        String code = "";

        final HashSet<Element> classFields = entitiesClass.get(genericEntityClass.getQualifiedName().toString());

        if (classFields == null) {
            code = String.format("\n    // classFields is null\n    public void save2(%s entity) {\n        save(entity);\n    }\n", genericEntityClass.getQualifiedName().toString());
            return code;
        }

        final Optional<Element> idField = classFields.stream().filter(element -> element.getAnnotation(IdColumn.class) != null && element.getAnnotation(ColumnName.class) != null).findFirst();

        if (!idField.isPresent()) {
            throw new RuntimeException("Cannot find id column");
        }

        final String idClassFieldDataType = idField.get().asType().toString();

        final String idClassFieldName = idField.get().getSimpleName().toString();


        String dataTypeSimpleName;
        if (idClassFieldDataType.matches(".*\\..*")) {
            List<String> fieldTypeSplitDot = new ArrayList<>(Arrays.asList(idClassFieldDataType.split(DOT)));
            dataTypeSimpleName = fieldTypeSplitDot.get(fieldTypeSplitDot.size() - 1);
        } else {
            dataTypeSimpleName = idClassFieldDataType;
        }

        if (dataTypeSimpleName.equals("Long")) {
            code = String.format("\n" + "    public void save2(%1$s entity) {\n" + "        try {\n" + "            String sql = %1$sBuilder.insertSqlV2(entity);\n" + "\n" + "            vn.com.lcx.common.database.type.DBTypeEnum dbTypeEnum = ((vn.com.lcx.common.database.pool.wrapper.LCXConnection) vn.com.lcx.common.database.context.ConnectionContext.get()).getDBType();\n" + "\n" + "            Long id = 0L;\n" + "\n" + "            if (dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MSSQL) || dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MYSQL)) {\n" + "                int rowAffected = executor.executeMutation(\n" + "                        vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                        sql,\n" + "                        %1$sBuilder.insertMapInputParameterV2(entity)\n" + "                );\n" + "                if (rowAffected == 0) {\n" + "                    throw new RuntimeException(\"Can not insert\");\n" + "                }\n" + "                try (Statement stmt = vn.com.lcx.common.database.context.ConnectionContext.get().createStatement()) {\n" + "                    // final String getLatestIdStatement = dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MSSQL) ? \"SELECT SCOPE_IDENTITY()\" : \"SELECT LAST_INSERT_ID()\";\n" + "                    final String getLatestIdStatement = dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MSSQL) ? \"SELECT @@IDENTITY\" : \"SELECT LAST_INSERT_ID()\";\n" + "                    try (ResultSet rs = stmt.executeQuery(getLatestIdStatement)) {\n" + "                        if (rs.next()) {\n" + "                            id = rs.getLong(1);\n" + "                        }\n" + "                    }\n" + "                } catch (SQLException e) {\n" + "                    throw new RuntimeException(e);\n" + "                }\n" + "            } else {\n" + "                BigDecimal newId = executor.executeInsertReturnId(\n" + "                        vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                        dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.ORACLE) ? sql + \" RETURNING %3$s INTO ?\" : sql + \" RETURNING %3$s\",\n" + "                        %1$sBuilder.insertMapInputParameterV2(entity),\n" + "                        dbTypeEnum\n" + "                );\n" + "                id = newId.longValue();\n" + "            }\n" + "            entity.set%2$s(id);\n" + "        } catch (Exception e) {\n" + "            throw new RuntimeException(\"Can not insert\");\n" + "        }\n" + "    }", genericEntityClass.getQualifiedName().toString(), Character.toUpperCase(idClassFieldName.charAt(0)) + idClassFieldName.substring(1), idField.get().getAnnotation(ColumnName.class).name());
        } else if (dataTypeSimpleName.equals("BigDecimal")) {
            code = String.format("\n" + "    public void save2(%1$s entity) {\n" + "        try {\n" + "            String sql = %1$sBuilder.insertSqlV2(entity);\n" + "\n" + "            vn.com.lcx.common.database.type.DBTypeEnum dbTypeEnum = ((vn.com.lcx.common.database.pool.wrapper.LCXConnection) vn.com.lcx.common.database.context.ConnectionContext.get()).getDBType();\n" + "\n" + "            BigDecimal id = BigDecimal.ZERO;\n" + "\n" + "            if (dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MSSQL) || dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MYSQL)) {\n" + "                int rowAffected = executor.executeMutation(\n" + "                        vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                        sql,\n" + "                        %1$sBuilder.insertMapInputParameterV2(entity)\n" + "                );\n" + "                if (rowAffected == 0) {\n" + "                    throw new RuntimeException(\"Can not insert\");\n" + "                }\n" + "                try (Statement stmt = vn.com.lcx.common.database.context.ConnectionContext.get().createStatement()) {\n" + "                    // final String getLatestIdStatement = dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MSSQL) ? \"SELECT SCOPE_IDENTITY()\" : \"SELECT LAST_INSERT_ID()\";\n" + "                    final String getLatestIdStatement = dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.MSSQL) ? \"SELECT @@IDENTITY\" : \"SELECT LAST_INSERT_ID()\";\n" + "                    try (ResultSet rs = stmt.executeQuery(getLatestIdStatement)) {\n" + "                        if (rs.next()) {\n" + "                            id = rs.getBigDecimal(1);\n" + "                        }\n" + "                    }\n" + "                } catch (SQLException e) {\n" + "                    throw new RuntimeException(e);\n" + "                }\n" + "            } else {\n" + "                id = executor.executeInsertReturnId(\n" + "                        vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                        dbTypeEnum.equals(vn.com.lcx.common.database.type.DBTypeEnum.ORACLE) ? sql + \" RETURNING %3$s INTO ?\" : sql + \" RETURNING %3$s\"," + "                        %1$sBuilder.insertMapInputParameterV2(entity),\n" + "                        dbTypeEnum\n" + "                );\n" + "            }\n" + "            entity.set%2$s(id);\n" + "        } catch (Exception e) {\n" + "            throw new RuntimeException(\"Can not insert\");\n" + "        }\n" + "    }", genericEntityClass.getQualifiedName().toString(), Character.toUpperCase(idClassFieldName.charAt(0)) + idClassFieldName.substring(1), idField.get().getAnnotation(ColumnName.class).name());
        } else {
            code = String.format("\n    public void save2(%s entity) {\n        save(entity);\n    }\n", genericEntityClass.getQualifiedName().toString());
        }

        return code;
    }

    public String generateBatchExecutionMethodCode() {
        String save = String.format("\n" + "    @Override\n" + "    public Map<String, Integer> save(List<%1$s> entities) {\n" + "        String sql = %1$sBuilder.insertSql();\n" + "        Map<String, Integer> batchExecutationResult = executor.executeMutationBatch(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                entities.stream().map(entity -> %1$sBuilder.insertMapInputParameter(entity)).collect(Collectors.toList())\n" + "        );\n" + "        if (batchExecutationResult == null) {\n" + "            throw new RuntimeException(\"Datasource exception\");\n" + "        }\n" + "        if (batchExecutationResult.isEmpty()) {\n" + "            throw new RuntimeException(\"Batch failed\");\n" + "        }\n" + "        return batchExecutationResult;\n" + "    }\n", this.genericEntityClass.getQualifiedName());
        String update = String.format("\n" + "    @Override\n" + "    public Map<String, Integer> update(List<%1$s> entities) {\n" + "        String sql = %1$sBuilder.updateSql();\n" + "        Map<String, Integer> batchExecutationResult = executor.executeMutationBatch(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                entities.stream().map(entity -> %1$sBuilder.updateMapInputParameter(entity)).collect(Collectors.toList())\n" + "        );\n" + "        if (batchExecutationResult == null) {\n" + "            throw new RuntimeException(\"Datasource exception\");\n" + "        }\n" + "        if (batchExecutationResult.isEmpty()) {\n" + "            throw new RuntimeException(\"Batch failed\");\n" + "        }\n" + "        return batchExecutationResult;\n" + "    }\n", this.genericEntityClass.getQualifiedName());
        String delete = String.format("\n" + "    @Override\n" + "    public Map<String, Integer> delete(List<%1$s> entities) {\n" + "        String sql = %1$sBuilder.deleteSql();\n" + "        Map<String, Integer> batchExecutationResult = executor.executeMutationBatch(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                entities.stream().map(entity -> %1$sBuilder.deleteMapInputParameter(entity)).collect(Collectors.toList())\n" + "        );\n" + "        if (batchExecutationResult == null) {\n" + "            throw new RuntimeException(\"Datasource exception\");\n" + "        }\n" + "        if (batchExecutationResult.isEmpty()) {\n" + "            throw new RuntimeException(\"Batch failed\");\n" + "        }\n" + "        return batchExecutationResult;\n" + "    }\n", this.genericEntityClass.getQualifiedName());
        return save + update + delete;
    }

    public String parseQueryAnnotationMethod() {
        final ArrayList<String> listOfCodeLine = new ArrayList<>();
        List<MethodInfo> listOfMethodsHaveQueryAnnotation = this.allMethods.stream().filter(methodInfo -> StringUtils.isNotBlank(methodInfo.getNativeQueryValue()) && !methodInfo.isModifying()).collect(Collectors.toList());
        if (!listOfMethodsHaveQueryAnnotation.isEmpty()) {
            for (MethodInfo methodInfo : listOfMethodsHaveQueryAnnotation) {
                // boolean firstParameterIsNotConnectionType = !(this.typeUtils.isAssignable(
                //         methodInfo.getInputParameters().get(0).asType(),
                //         elementUtils.getTypeElement("vn.com.lcx.common.database.pool.entry.ConnectionEntry").asType()
                // ));
                // if (firstParameterIsNotConnectionType) {
                //     throw new RuntimeException("Invalid method, first parameter is not vn.com.lcx.common.database.pool.entry.ConnectionEntry");
                // }

                final String currentMethodOutputString = methodInfo.getOutputParameter().toString();
                final boolean currentMethodOutputIsList = currentMethodOutputString.contains("List<");
                String returnClass;
                if (currentMethodOutputIsList) {
                    returnClass = currentMethodOutputString.substring(15, currentMethodOutputString.length() - 1);
                } else {
                    returnClass = currentMethodOutputString;
                }

                String mapPutCode = "";
                if (!methodInfo.getInputParameters().isEmpty()) {
                    mapPutCode += "java.util.Map<Integer, Object> map = new java.util.HashMap<>();\n" + "        int startingPosition = 0;";
                }
                String sqlStatementDeclaration = methodInfo.getNativeQueryValue().replace("\n", "\\n");
                for (int i = 0; i < methodInfo.getInputParameters().size(); i++) {
                    final VariableElement currentInputParameter = methodInfo.getInputParameters().get(i);
                    if (this.typeUtils.isAssignable(currentInputParameter.asType(), elementUtils.getTypeElement("vn.com.lcx.common.database.pageable.Pageable").asType())) {
                        continue;
                    }
                    if (currentInputParameter.asType().toString().contains("vn.com.lcx.common.database.ResultSetHandler")) {
                        continue;
                    }
                    final boolean currentInputParameterIsList = methodInfo.getInputParameters().get(i).asType().toString().contains("List<");
                    if (sqlStatementDeclaration.contains(":" + currentInputParameter.getSimpleName().toString())) {
                        if (currentInputParameterIsList) {
                            sqlStatementDeclaration = sqlStatementDeclaration.replace(":" + currentInputParameter.getSimpleName().toString(), String.format("\" + %s.stream().map(a -> \"?\").collect(java.util.stream.Collectors.joining(\", \")) + \"", currentInputParameter.getSimpleName().toString()));
                        } else {
                            sqlStatementDeclaration = sqlStatementDeclaration.replace(":" + currentInputParameter.getSimpleName().toString(), "?");
                        }
                    }
                    if (currentInputParameterIsList) {
                        TypeMirror typeMirror = methodInfo.getInputParameters().get(i).asType();

                        if (typeMirror instanceof DeclaredType) {
                            DeclaredType declaredType = (DeclaredType) typeMirror;
                            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                            mapPutCode += String.format("\n" + "        for (%s o : %s) {\n" + "            map.put(++startingPosition, o);\n" + "        }", typeArguments.get(0).toString(), methodInfo.getInputParameters().get(i).getSimpleName());
                        } else {
                            mapPutCode += String.format("\n" + "        for (Object o : %s) {\n" + "            map.put(++startingPosition, o);\n" + "        }", methodInfo.getInputParameters().get(i).getSimpleName());
                        }
                    } else {
                        mapPutCode += String.format("\n        map.put(++startingPosition, %s);", methodInfo.getInputParameters().get(i).getSimpleName());
                    }
                }
                String pageableCode = "";
                String databaseExecutionCode = "";
                if (!methodInfo.getInputParameters().isEmpty()) {
                    VariableElement lastInputParameter = methodInfo.getInputParameters().get(methodInfo.getInputParameters().size() - 1);
                    if (this.typeUtils.isAssignable(lastInputParameter.asType(), elementUtils.getTypeElement("vn.com.lcx.common.database.pageable.Pageable").asType())) {
                        pageableCode = String.format("+ \" \" + %s.toSql()", lastInputParameter.getSimpleName().toString());
                    }
                    if (methodInfo.getInputParameters().size() >= 2) {
                        VariableElement secondLastInputParameter = methodInfo.getInputParameters().get(methodInfo.getInputParameters().size() - 2);
                        if (this.typeUtils.isAssignable(secondLastInputParameter.asType(), elementUtils.getTypeElement("vn.com.lcx.common.database.pageable.Pageable").asType())) {
                            pageableCode = String.format("+ \" \" + %s.toSql()", secondLastInputParameter.getSimpleName().toString());
                        }
                    }

                    if (lastInputParameter.asType().toString().contains("vn.com.lcx.common.database.ResultSetHandler")) {
                        databaseExecutionCode = String.format("executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %s,\n" + "                %s\n" + "        );", StringUtils.isNotBlank(mapPutCode) ? "map" : "null", lastInputParameter.getSimpleName());
                    } else {
                        databaseExecutionCode = String.format("executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %s,\n" + "                %sBuilder::resultSetMapping\n" + "        );", StringUtils.isNotBlank(mapPutCode) ? "map" : "null", returnClass);
                    }
                } else {
                    databaseExecutionCode = String.format("executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %s,\n" + "                %sBuilder::resultSetMapping\n" + "        );", StringUtils.isNotBlank(mapPutCode) ? "map" : "null", returnClass);
                }
                final String code;
                if (currentMethodOutputIsList) {
                    code = String.format("\n" + "    @Override\n" + "    public %s %s(%s) {\n" + "        String sql = \"%s\" %s;\n" + "        %s\n" + "        java.util.List<%s> sqlResult = %s\n" + "        if (sqlResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "        return sqlResult.isEmpty() ? new ArrayList<>() : sqlResult;\n" + "    }\n", currentMethodOutputString, methodInfo.getMethodName(), methodInfo.getInputParameters().stream().map(e -> String.format("%s %s", "T".equals(e.asType().toString()) ? this.genericEntityClass.getQualifiedName() : e.asType(), e.getSimpleName())).collect(Collectors.joining(", ")), sqlStatementDeclaration, pageableCode, mapPutCode, returnClass, databaseExecutionCode);
                } else {
                    code = String.format("\n" + "    @Override\n" + "    public %s %s(%s) {\n" + "        String sql = \"%s\" %s;\n" + "        %s\n" + "        java.util.List<%s> sqlResult = %s\n" + "        if (sqlResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "        if (sqlResult != null && sqlResult.size() > 1) {\n" + "            throw new java.lang.RuntimeException(\"Result return more than 1\");\n" + "        }\n" + "        return sqlResult.isEmpty() ? new %s() : sqlResult.get(0);\n" + "    }\n", currentMethodOutputString, methodInfo.getMethodName(), methodInfo.getInputParameters().stream().map(e -> String.format("%s %s", "T".equals(e.asType().toString()) ? this.genericEntityClass.getQualifiedName() : e.asType(), e.getSimpleName())).collect(Collectors.joining(", ")), sqlStatementDeclaration, pageableCode, mapPutCode, returnClass, databaseExecutionCode, returnClass);
                }
                listOfCodeLine.add(code);
            }
        }
        return listOfCodeLine.isEmpty() ? CommonConstant.EMPTY_STRING : String.join(System.lineSeparator(), listOfCodeLine);
    }

    public String parseQueryAnnotationMethodWithModifying() {
        final ArrayList<String> listOfCodeLine = new ArrayList<>();
        List<MethodInfo> listOfMethodsHaveQueryAnnotation = this.allMethods.stream().filter(methodInfo -> StringUtils.isNotBlank(methodInfo.getNativeQueryValue()) && methodInfo.isModifying()).collect(Collectors.toList());

        if (!listOfMethodsHaveQueryAnnotation.isEmpty()) {
            for (MethodInfo methodInfo : listOfMethodsHaveQueryAnnotation) {
                String code = "";
                final String currentMethodOutputString = methodInfo.getOutputParameter().toString();
                final boolean currentMethodOutputIsVoid = currentMethodOutputString.equals("void");
                String mapPutCode = "";
                if (!methodInfo.getInputParameters().isEmpty()) {
                    mapPutCode += "java.util.Map<Integer, Object> map = new java.util.HashMap<>();\n" + "        int startingPosition = 0;";
                }
                String sqlStatementDeclaration = methodInfo.getNativeQueryValue().replace("\n", "\\n");
                for (int i = 0; i < methodInfo.getInputParameters().size(); i++) {
                    final VariableElement currentInputParameter = methodInfo.getInputParameters().get(i);
                    final boolean currentInputParameterIsList = methodInfo.getInputParameters().get(i).asType().toString().contains("List<");
                    if (sqlStatementDeclaration.contains(":" + currentInputParameter.getSimpleName().toString())) {
                        if (currentInputParameterIsList) {
                            sqlStatementDeclaration = sqlStatementDeclaration.replace(":" + currentInputParameter.getSimpleName().toString(), String.format("\" + %s.stream().map(a -> \"?\").collect(java.util.stream.Collectors.joining(\", \")) + \"", currentInputParameter.getSimpleName().toString()));
                        } else {
                            sqlStatementDeclaration = sqlStatementDeclaration.replace(":" + currentInputParameter.getSimpleName().toString(), "?");
                        }
                    }
                    if (currentInputParameterIsList) {
                        TypeMirror typeMirror = methodInfo.getInputParameters().get(i).asType();

                        if (typeMirror instanceof DeclaredType) {
                            DeclaredType declaredType = (DeclaredType) typeMirror;
                            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                            mapPutCode += String.format("\n" + "        for (%s o : %s) {\n" + "            map.put(++startingPosition, o);\n" + "        }", typeArguments.get(0).toString(), methodInfo.getInputParameters().get(i).getSimpleName());
                        } else {
                            mapPutCode += String.format("\n" + "        for (Object o : %s) {\n" + "            map.put(++startingPosition, o);\n" + "        }", methodInfo.getInputParameters().get(i).getSimpleName());
                        }
                    } else {
                        mapPutCode += String.format("\n        map.put(++startingPosition, %s);", methodInfo.getInputParameters().get(i).getSimpleName());
                    }
                    String databaseExecutionCode = String.format("executor.executeMutation(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                %s\n" + "        );", StringUtils.isNotBlank(mapPutCode) ? "map" : "null");
                    code = String.format("\n" + "    @Override\n" + "    public %s %s(%s) {\n" + "        String sql = \"%s\";\n" + "        %s\n" + "        int sqlResult = %s\n" + "        if (sqlResult == 0) {\n" + "            throw new java.lang.RuntimeException(\"No row updated\");\n" + "        }\n" + "        return %s;\n" + "    }\n", currentMethodOutputString, methodInfo.getMethodName(), methodInfo.getInputParameters().stream().map(e -> String.format("%s %s", "T".equals(e.asType().toString()) ? this.genericEntityClass.getQualifiedName() : e.asType(), e.getSimpleName())).collect(Collectors.joining(", ")), sqlStatementDeclaration, mapPutCode, databaseExecutionCode, currentMethodOutputIsVoid ? "" : "sqlResult");
                }
                listOfCodeLine.add(code);
            }
        }
        return listOfCodeLine.isEmpty() ? CommonConstant.EMPTY_STRING : String.join(System.lineSeparator(), listOfCodeLine);
    }

    public String parseSpecificationWithPagination() {
        return String.format("\n\n" + "    public vn.com.lcx.common.database.pageable.Page<%1$s> find(vn.com.lcx.common.database.specification.Specification specification, vn.com.lcx.common.database.pageable.Pageable pageable) {\n" + "        pageable.fieldToColumn();\n" + "        String sql = vn.com.lcx.common.database.reflect.SelectStatementBuilder.of(%1$s.class).build(\"findAll\") + (specification.getTimes() > 0 ? \" WHERE \" : \"\") + specification.getFinalSQL() + \" \" + pageable.toSql();\n" + "        String countSql = vn.com.lcx.common.database.reflect.SelectStatementBuilder.of(%1$s.class).build(\"countAll\") + (specification.getTimes() > 0 ? \" WHERE \" : \"\") + specification.getFinalSQL();\n" + "        java.util.Map<Integer, Object> map = new java.util.HashMap<>();\n" + "        int startingPosition = 0;\n" + "        for (Object param : specification.getParameters()) {\n" + "            map.put(++startingPosition, param);\n" + "        }\n" + "        java.util.List<%1$s> sqlResult = executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                map,\n" + "                %1$sBuilder::resultSetMapping\n" + "        );\n" + "        if (sqlResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "\n" + "        java.util.List<Integer> countResult = executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                countSql,\n" + "                map,\n" + "                resultSet -> {\n" + "                    try {\n" + "                        return resultSet.getInt(1);\n" + "                    } catch (java.sql.SQLException sqlException) {\n" + "                        return 0;\n" + "                    }\n" + "                }\n" + "        );\n" + "        if (countResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "        int count = countResult.isEmpty() ? 0 : countResult.get(0);\n" + "        return vn.com.lcx.common.database.pageable.Page.<%1$s>create(\n" + "                sqlResult,\n" + "                count,\n" + "                pageable.getPageNumber(),\n" + "                pageable.getPageSize()\n" + "        );\n" + "    }\n", genericEntityClass.getQualifiedName().toString());
    }

    public String parseSpecification() {
        return String.format("\n\n" + "    public java.util.List<%1$s> find(vn.com.lcx.common.database.specification.Specification specification) {\n" + "        String sql = vn.com.lcx.common.database.reflect.SelectStatementBuilder.of(%1$s.class).build(\"findAll\") + (specification.getTimes() > 0 ? \" WHERE \" : \"\") + specification.getFinalSQL();\n" + "        java.util.Map<Integer, Object> map = new java.util.HashMap<>();\n" + "        int startingPosition = 0;\n" + "        for (Object param : specification.getParameters()) {\n" + "            map.put(++startingPosition, param);\n" + "        }\n" + "        java.util.List<%1$s> sqlResult = executor.executeQuery(\n" + "                vn.com.lcx.common.database.context.ConnectionContext.get(),\n" + "                sql,\n" + "                map,\n" + "                %1$sBuilder::resultSetMapping\n" + "        );\n" + "        if (sqlResult == null) {\n" + "            throw new java.lang.RuntimeException(\"Data source exception\");\n" + "        }\n" + "\n" + "        return sqlResult.isEmpty() ? new ArrayList<>() : sqlResult;\n" + "    }\n", genericEntityClass.getQualifiedName().toString());
    }

    private String findColumnNameOfField(HashSet<Element> allElementsOfReturnClass, final String fieldName) {
        String databaseColumnNameToBeGet = CommonConstant.EMPTY_STRING;
        for (Element element : allElementsOfReturnClass) {
            if (element.getSimpleName().toString().equals(fieldName)) {
                ColumnName columnNameAnnotation = element.getAnnotation(ColumnName.class);
                databaseColumnNameToBeGet = Optional.ofNullable(columnNameAnnotation).filter(a -> StringUtils.isNotBlank(a.name())).map(ColumnName::name).orElse(convertCamelToConstant(fieldName));
            }
        }
        return databaseColumnNameToBeGet;
    }

}
