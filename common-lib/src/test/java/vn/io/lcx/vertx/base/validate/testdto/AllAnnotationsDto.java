package vn.io.lcx.vertx.base.validate.testdto;

import com.google.gson.annotations.SerializedName;
import vn.io.lcx.vertx.base.annotation.GreaterThan;
import vn.io.lcx.vertx.base.annotation.NotNull;
import vn.io.lcx.vertx.base.annotation.Regex;
import vn.io.lcx.vertx.base.annotation.Values;

public class AllAnnotationsDto {

    @NotNull
    @SerializedName("user_name")
    private String userName;

    @Regex("^\\d{3}-\\d{3}-\\d{4}$")
    private String phoneNumber;

    @Values({"ACTIVE", "INACTIVE"})
    private String status;

    @GreaterThan(0)
    private Integer age;

    public AllAnnotationsDto() {
    }

    public AllAnnotationsDto(String userName, String phoneNumber, String status, Integer age) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
