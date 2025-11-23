package vn.com.lcx.reactive.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static vn.com.lcx.common.utils.MyStringUtils.findAllOccurrences;
import static vn.com.lcx.common.utils.MyStringUtils.replaceCharWithSubstring;

public class QueryProcessor {

    /**
     * Processes a SQL statement template by replacing '#' placeholders with
     * database-specific placeholders ('?' or '$N' sequentially) based on the
     * parameter list and specified placeholder type.
     *
     * @param originalQueryTemplate The SQL query string containing '#' placeholders.
     * @param parameterList The list of parameter values (including Collections for IN).
     * @param placeholder The placeholder type string (e.g., "?" for JDBC, "$" for numbered).
     * @return The processed SQL query string ready for execution.
     */
    public static String processQueryStatement(
            String originalQueryTemplate,
            ArrayList<Object> parameterList,
            String placeholder) {

        // Use normalized query for all operations
        var queryStatement = originalQueryTemplate.replace('\u00A0', ' ');

        // 1. Find all index positions of the '#' character.
        final var statementParameterIndexes = findAllOccurrences(queryStatement, '#');

        // --- CASE 1: Standard JDBC '?' Placeholder ---
        if (Objects.equals(placeholder, "?")) {
            // Process replacements in REVERSE order to handle IN clause expansion
            for (int i = statementParameterIndexes.size() - 1; i >= 0; i--) {
                final Object currentParameter = parameterList.get(i);
                final int currentIndex = statementParameterIndexes.get(i);

                if (currentParameter instanceof Collection<?>) {
                    // Handling IN Condition: Replace '#' with (?, ?, ...)
                    final var collection = (Collection<?>) currentParameter;
                    final int collectionSize = collection.size();

                    final var inConditionStatement = new StringBuilder("(");

                    // Build the string: ?, ?, ...
                    for (int j = 0; j < collectionSize; j++) {
                        if (j > 0) {
                            inConditionStatement.append(", ");
                        }
                        inConditionStatement.append("?"); // Simply append '?'
                    }
                    inConditionStatement.append(")");

                    queryStatement = replaceCharWithSubstring(queryStatement, currentIndex, inConditionStatement.toString());
                } else {
                    // Handling Single Parameter: Replace '#' with '?'
                    queryStatement = replaceCharWithSubstring(queryStatement, currentIndex, "?");
                }
            }
            return queryStatement;
        }

        // --- CASE 2: Sequential Placeholder ($1, $2, ...) ---

        // 2. Pre-calculate the TOTAL number of final parameters ($N) required.
        int totalParameters = 0;
        for (Object o : parameterList) {
            if (o instanceof Collection<?>) {
                totalParameters += ((Collection<?>) o).size();
            } else {
                totalParameters++;
            }
        }

        int currentParamIndex = totalParameters;

        // 3. Process replacements in REVERSE order (crucial for sequential/numbered placeholders).
        for (int i = statementParameterIndexes.size() - 1; i >= 0; i--) {

            final Object currentParameter = parameterList.get(i);
            final int currentIndex = statementParameterIndexes.get(i);

            if (currentParameter instanceof Collection<?>) {
                // Handling IN Condition (e.g., $4, $5, $6)

                final var collection = (Collection<?>) currentParameter;
                final int collectionSize = collection.size();

                // Calculate the sequence number for the START parameter of this collection.
                final int startParam = currentParamIndex - collectionSize + 1;

                final var inConditionStatement = new StringBuilder("(");

                // Build the placeholder string: ($startParam, $startParam + 1, ...)
                for (int j = 0; j < collectionSize; j++) {
                    if (j > 0) {
                        inConditionStatement.append(", ");
                    }
                    // Use the provided placeholder (e.g., '$') combined with the number
                    inConditionStatement.append(placeholder).append(startParam + j);
                }
                inConditionStatement.append(")");

                queryStatement = replaceCharWithSubstring(queryStatement, currentIndex, inConditionStatement.toString());
                currentParamIndex -= collectionSize;

            } else {
                // Handling Single Parameter (e.g., $1)

                // Replace '#' with the placeholder combined with the highest unused parameter number
                queryStatement = replaceCharWithSubstring(queryStatement, currentIndex, placeholder + currentParamIndex);

                currentParamIndex--;
            }
        }

        return queryStatement;
    }

}
