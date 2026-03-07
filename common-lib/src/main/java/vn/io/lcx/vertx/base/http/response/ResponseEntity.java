package vn.io.lcx.vertx.base.http.response;

/**
 * A simple generic response wrapper used to return an HTTP-like status code
 * together with a response payload. This class is immutable except for its fields
 * and provides convenient static factory methods for easier instantiation.
 *
 * @param <T> the type of the response payload
 */
public class ResponseEntity<T> {

    private int status;
    private T response;

    /**
     * Creates a new ResponseEntity with the given status and response payload.
     *
     * @param status   the status code representing the result of the operation
     * @param response the response payload associated with this entity
     */
    public ResponseEntity(int status, T response) {
        this.status = status;
        this.response = response;
    }

    /**
     * Returns the status code.
     *
     * @return the status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status code.
     *
     * @param status the new status code
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Returns the response payload.
     *
     * @return the response payload
     */
    public T getResponse() {
        return response;
    }

    /**
     * Sets the response payload.
     *
     * @param response the new response payload
     */
    public void setResponse(T response) {
        this.response = response;
    }

    /**
     * Creates a new ResponseEntity with the given status and payload.
     *
     * @param status   the status code
     * @param response the payload
     * @param <T>      the type of the payload
     * @return a new ResponseEntity instance
     */
    public static <T> ResponseEntity<T> of(int status, T response) {
        return new ResponseEntity<>(status, response);
    }

    /**
     * Creates a successful (status = 200) ResponseEntity with the given payload.
     *
     * @param response the payload
     * @param <T>      the type of the payload
     * @return a new ResponseEntity with status 200
     */
    public static <T> ResponseEntity<T> ok(T response) {
        return new ResponseEntity<>(200, response);
    }

    /**
     * Creates a ResponseEntity with status = 400 (Bad Request).
     *
     * @param message error message payload
     * @return a ResponseEntity with status 400
     */
    public static ResponseEntity<String> badRequest(String message) {
        return new ResponseEntity<>(400, message);
    }

    /**
     * Creates a ResponseEntity with status = 500 (Internal Server Error).
     *
     * @param message error message
     * @return a ResponseEntity with status 500
     */
    public static ResponseEntity<String> internalServerError(String message) {
        return new ResponseEntity<>(500, message);
    }

    @Override
    public String toString() {
        return "ResponseEntity{" +
                "status=" + status +
                ", response=" + response +
                '}';
    }
}
