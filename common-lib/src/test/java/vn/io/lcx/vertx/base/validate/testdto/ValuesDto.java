package vn.io.lcx.vertx.base.validate.testdto;

import vn.io.lcx.vertx.base.annotation.Values;

public class ValuesDto {

    @Values({"CASH", "CARD", "TRANSFER"})
    private String paymentMethod;

    public ValuesDto() {
    }

    public ValuesDto(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
