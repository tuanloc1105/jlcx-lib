package vn.com.lcx.common.database.pageable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.constant.CommonConstant;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return create(list, (long) totalElements, pageNumber, pageSize);
    }

    public static <V> Page<V> create(List<V> list, long totalElements, int pageNumber, int pageSize) {
        final Page<V> page = new Page<>();
        page.setContent(list);
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setTotalPages(
                list.isEmpty() ?
                        0 :
                        Integer.parseInt(Math.round(Math.ceil((float) totalElements / (float) pageSize)) + CommonConstant.EMPTY_STRING)
        );
        page.setNumberOfElements(list.size());
        page.setTotalElements(totalElements);
        page.setFirstPage(pageNumber == 1);
        page.setLastPage(((long) (pageNumber) * (pageSize) >= totalElements) || (pageSize > totalElements));
        return page;
    }

    public static <T, V> Page<V> create(Page<T> anotherPage, Function<T, V> convertFunction) {
        final Page<V> page = new Page<>();
        page.setContent(anotherPage.getContent().stream().map(convertFunction).collect(Collectors.toList()));
        page.setPageNumber(anotherPage.getPageNumber());
        page.setPageSize(anotherPage.getPageSize());
        page.setTotalPages(anotherPage.getTotalPages());
        page.setNumberOfElements(anotherPage.getNumberOfElements());
        page.setTotalElements(anotherPage.getTotalElements());
        page.setFirstPage(anotherPage.getFirstPage());
        page.setLastPage(anotherPage.getLastPage());
        return page;
    }

}
