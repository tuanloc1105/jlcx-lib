package vn.com.lcx.vertx.base.http.response;

import java.io.Serializable;

public class CommonResponse implements Serializable {
    private static final long serialVersionUID = 2019642062237923133L;

    private String trace;
    private int errorCode;
    private String errorDescription;
    private int httpCode;

    public CommonResponse() {
    }

    public CommonResponse(String trace, int errorCode, String errorDescription, int httpCode) {
        this.trace = trace;
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.httpCode = httpCode;
    }

    public static CommonResponseBuilder builder() {
        return new CommonResponseBuilder();
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public static class CommonResponseBuilder {
        private String trace;
        private int errorCode;
        private String errorDescription;
        private int httpCode;

        public CommonResponseBuilder trace(String trace) {
            this.trace = trace;
            return this;
        }

        public CommonResponseBuilder errorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public CommonResponseBuilder errorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
            return this;
        }

        public CommonResponseBuilder httpCode(int httpCode) {
            this.httpCode = httpCode;
            return this;
        }

        public CommonResponse build() {
            return new CommonResponse(trace, errorCode, errorDescription, httpCode);
        }

    }

}
