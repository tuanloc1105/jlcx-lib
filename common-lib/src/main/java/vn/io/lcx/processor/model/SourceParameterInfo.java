package vn.io.lcx.processor.model;

import javax.lang.model.element.Element;
import java.util.List;

/**
 * Holds metadata about a single source parameter of a mapping method.
 */
public class SourceParameterInfo {

    private final String paramName;
    private final String paramType;
    private final List<Element> fields;

    public SourceParameterInfo(String paramName, String paramType, List<Element> fields) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.fields = fields;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamType() {
        return paramType;
    }

    public List<Element> getFields() {
        return fields;
    }
}
