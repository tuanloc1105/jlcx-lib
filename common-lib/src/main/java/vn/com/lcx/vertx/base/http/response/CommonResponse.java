package vn.com.lcx.vertx.base.http.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CommonResponse implements Serializable {
    private static final long serialVersionUID = 2019642062237923133L;

    private String trace;

    private int errorCode;

    private String errorDescription;

    private int httpCode;

}
