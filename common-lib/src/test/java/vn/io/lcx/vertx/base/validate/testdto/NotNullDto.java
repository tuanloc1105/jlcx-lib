package vn.io.lcx.vertx.base.validate.testdto;

import vn.io.lcx.vertx.base.annotation.NotNull;

public class NotNullDto {

    @NotNull
    private String requiredField;

    private String optionalField;

    public NotNullDto() {
    }

    public NotNullDto(String requiredField, String optionalField) {
        this.requiredField = requiredField;
        this.optionalField = optionalField;
    }

    public String getRequiredField() {
        return requiredField;
    }

    public void setRequiredField(String requiredField) {
        this.requiredField = requiredField;
    }

    public String getOptionalField() {
        return optionalField;
    }

    public void setOptionalField(String optionalField) {
        this.optionalField = optionalField;
    }
}
