package vn.com.lcx.vertx.base.http.response;

import java.util.List;

public class PageableResponse<T> extends CommonResponse {
    private static final long serialVersionUID = -8473059124749032152L;

    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
    private Integer numberOfElements;
    private Long totalElements;
    private Boolean firstPage;
    private Boolean lastPage;
    private List<T> content;

    public PageableResponse() {
    }

    public PageableResponse(String trace,
                            int errorCode,
                            String errorDescription,
                            int httpCode,
                            Integer pageNumber,
                            Integer pageSize,
                            Integer totalPages,
                            Integer numberOfElements,
                            Long totalElements,
                            Boolean firstPage,
                            Boolean lastPage,
                            List<T> content) {
        super(trace, errorCode, errorDescription, httpCode);
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.numberOfElements = numberOfElements;
        this.totalElements = totalElements;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.content = content;
    }

    public static <V> PageableResponse<V> create(List<V> list, int totalElements, int pageNumber, int pageSize) {
        final PageableResponse<V> pageableResponse = new PageableResponse<>();
        pageableResponse.setContent(list);
        pageableResponse.setPageNumber(pageNumber);
        pageableResponse.setPageSize(pageSize);
        pageableResponse.setTotalPages(list.isEmpty() ? 0 : Math.round((float) totalElements / (float) pageSize));
        pageableResponse.setNumberOfElements(list.size());
        pageableResponse.setTotalElements((long) totalElements);
        pageableResponse.setFirstPage(pageNumber == 1);
        pageableResponse.setLastPage(((pageNumber) * (pageSize) >= totalElements) || (pageSize > totalElements));
        return pageableResponse;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Boolean getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(Boolean firstPage) {
        this.firstPage = firstPage;
    }

    public Boolean getLastPage() {
        return lastPage;
    }

    public void setLastPage(Boolean lastPage) {
        this.lastPage = lastPage;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public interface Handler<T, V> {
        V handle(T input);
    }

}
