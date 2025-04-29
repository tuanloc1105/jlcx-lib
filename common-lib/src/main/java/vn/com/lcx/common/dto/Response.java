package vn.com.lcx.common.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class Response<T> {

    private int code;
    private String msg;
    private T response;
    private Map<String, List<String>> responseHeaders;
    private String errorResponse;

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder<T> {
        private int code;
        private String msg;
        private T response;
        private Map<String, List<String>> responseHeaders;
        private String errorResponse;

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
