package vn.com.lcx.common;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CommonLibTestClass {

    public static void main(String[] args) {
        final var statement = "oneAndTwoAndThreeOrFourOrFiveOrderBySixAndSeven";
        final var splitStatement = statement.split("OrderBy");

        final var list = new LinkedList<String>();
        final var whereList = new LinkedList<String>();
        final var sortList = new LinkedList<String>();

        // splitByWords(whereList, statement, "And", "Or");

        splitByWords(list, statement, "OrderBy", "And", "Or");

        System.out.println(list);
    }

    public static void splitByWords(final List<String> result, final String inputString, final String... words) {
        if (result == null) {
            throw new NullPointerException("A result list must not be null");
        }
        if (StringUtils.isBlank(inputString)) {
            throw new NullPointerException("An input string must not be null");
        }
        if (words.length == 0) {
            return;
        }
        result.clear();
        for (String word : words) {
            if (result.isEmpty()) {
                final var splitArr = new LinkedList<>(Arrays.asList(inputString.split(word)));
                final var temp = new LinkedList<String>();
                for (int i = 0; i < splitArr.size(); i++) {
                    if (i == splitArr.size() - 1) {
                        temp.add(splitArr.get(i));
                    } else {
                        temp.add(splitArr.get(i));
                        temp.add(word);
                    }
                }
                result.addAll(temp);
            } else {
                final var splitArr = new LinkedList<>(result);
                result.clear();
                for (String element : splitArr) {
                    final var splitArr2 = new LinkedList<>(Arrays.asList(element.split(word)));
                    if (splitArr2.size() > 1) {
                        final var temp = new LinkedList<String>();
                        for (int i = 0; i < splitArr2.size(); i++) {
                            if (i == splitArr2.size() - 1) {
                                temp.add(splitArr2.get(i));
                            } else {
                                temp.add(splitArr2.get(i));
                                temp.add(word);
                            }
                        }
                        result.addAll(temp);
                    } else {
                        result.addAll(splitArr2);
                    }
                }
            }
        }
        final var filterResult = result.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        result.clear();
        result.addAll(filterResult);
    }

}
