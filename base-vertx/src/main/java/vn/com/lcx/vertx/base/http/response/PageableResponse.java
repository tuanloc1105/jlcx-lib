package vn.com.lcx.vertx.base.http.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public interface Handler<T, V> {
        V handle(T input);
    }

}
