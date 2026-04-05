package vn.io.lcx.vertx.base.validate.testdto;

import vn.io.lcx.vertx.base.annotation.NotNull;

public class NestedDto {

    @NotNull
    private String parentName;

    private NotNullDto child;

    public NestedDto() {
    }

    public NestedDto(String parentName, NotNullDto child) {
        this.parentName = parentName;
        this.child = child;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public NotNullDto getChild() {
        return child;
    }

    public void setChild(NotNullDto child) {
        this.child = child;
    }
}
