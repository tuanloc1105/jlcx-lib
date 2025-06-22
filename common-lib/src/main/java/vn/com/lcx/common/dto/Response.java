package vn.com.lcx.common.dto;

import java.util.List;
import java.util.Map;

public class Response<T> {

    private int code;
    private String msg;
    private T response;
    private Map<String, List<String>> responseHeaders;
    private String errorResponse;

    private Response() {
    }

    private Response(int code, String msg, T response, Map<String, List<String>> responseHeaders, String errorResponse) {
        this.code = code;
        this.msg = msg;
        this.response = response;
        this.responseHeaders = responseHeaders;
        this.errorResponse = errorResponse;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(String errorResponse) {
        this.errorResponse = errorResponse;
    }

    public static class Builder<T> {
        private int code;
        private String msg;
        private T response;
        private Map<String, List<String>> responseHeaders;
        private String errorResponse;

        public Builder() {
        }

        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder<T> response(T response) {
            this.response = response;
            return this;
        }

        public Builder<T> responseHeaders(Map<String, List<String>> responseHeaders) {
            this.responseHeaders = responseHeaders;
            return this;
        }

        public Builder<T> errorResponse(String errorResponse) {
            this.errorResponse = errorResponse;
            return this;
        }

        public Response<T> build() {
            return new Response<>(
                    code,
                    msg,
                    response,
                    responseHeaders,
                    errorResponse
            );
        }
    }

}
