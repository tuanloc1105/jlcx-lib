package vn.io.lcx.vertx.base.validate.testdto;

import vn.io.lcx.vertx.base.annotation.GreaterThan;
import vn.io.lcx.vertx.base.annotation.LessThan;

import java.math.BigDecimal;

public class NumericBoundsDto {

    @GreaterThan(0)
    private Integer quantity;

    @LessThan(100)
    private Double percentage;

    @GreaterThan(0)
    @LessThan(1000000)
    private BigDecimal amount;

    public NumericBoundsDto() {
    }

    public NumericBoundsDto(Integer quantity, Double percentage, BigDecimal amount) {
        this.quantity = quantity;
        this.percentage = percentage;
        this.amount = amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
