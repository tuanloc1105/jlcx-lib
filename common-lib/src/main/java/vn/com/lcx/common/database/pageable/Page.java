package vn.com.lcx.common.database.pageable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -2413109919435477982L;

    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
    private Integer numberOfElements;
    private Long totalElements;
    private Boolean firstPage;
    private Boolean lastPage;
    private List<T> content;


    public static <V> Page<V> create(List<V> list, int totalElements, int pageNumber, int pageSize) {
        final Page<V> page = new Page<>();
        page.setContent(list);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setTotalPages(list.isEmpty() ? 0 : Math.round((float) totalElements / (float) pageSize));
        page.setNumberOfElements(list.size());
        page.setTotalElements((long) totalElements);
        page.setFirstPage(pageNumber == 1);
        page.setLastPage(((pageNumber) * (pageSize) >= totalElements) || (pageSize > totalElements));
        return page;
    }

}
