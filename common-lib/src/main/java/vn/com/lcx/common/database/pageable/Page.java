package vn.com.lcx.common.database.pageable;

import vn.com.lcx.common.constant.CommonConstant;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Page() {
    }

    public Page(Integer pageNumber,
                Integer pageSize,
                Integer totalPages,
                Integer numberOfElements,
                Long totalElements,
                Boolean firstPage,
                Boolean lastPage,
                List<T> content) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.numberOfElements = numberOfElements;
        this.totalElements = totalElements;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.content = content;
    }

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

}
