package vn.io.lcx.vertx.base.validate.testdto;

import vn.io.lcx.vertx.base.annotation.Regex;

public class RegexDto {

    @Regex("^[a-zA-Z0-9]+$")
    private String alphanumericField;

    public RegexDto() {
    }

    public RegexDto(String alphanumericField) {
        this.alphanumericField = alphanumericField;
    }

    public String getAlphanumericField() {
        return alphanumericField;
    }

    public void setAlphanumericField(String alphanumericField) {
        this.alphanumericField = alphanumericField;
    }
}
