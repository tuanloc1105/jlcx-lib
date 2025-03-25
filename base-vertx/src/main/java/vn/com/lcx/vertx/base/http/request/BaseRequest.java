package vn.com.lcx.vertx.base.http.request;

import java.io.Serializable;

public interface BaseRequest extends Serializable {
    void validate();
}
