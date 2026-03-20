package vn.io.lcx.common.database.pageable;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageableImplTest {

    @Nested
    class GetOffset {

        @Test
        void calculatesOffsetForFirstPage() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertEquals(0, pageable.getOffset());
        }

        @Test
        void calculatesOffsetForSecondPage() {
            PageableImpl pageable = new PageableImpl(2, 10);
            assertEquals(10, pageable.getOffset());
        }

        @Test
        void calculatesOffsetForThirdPage() {
            PageableImpl pageable = new PageableImpl(3, 25);
            assertEquals(50, pageable.getOffset());
        }

        @Test
        void throwsForZeroPageNumberAndZeroPageSize() {
            PageableImpl pageable = new PageableImpl(0, 0);
            assertThrows(IllegalArgumentException.class, pageable::getOffset);
        }

        @Test
        void calculatesOffsetForPageSize1() {
            PageableImpl pageable = new PageableImpl(5, 1);
            assertEquals(4, pageable.getOffset());
        }
    }

    @Nested
    class PageNumberValidation {

        @Test
        void setPageNumberThrowsForZero() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(IllegalArgumentException.class, () -> pageable.setPageNumber(0));
        }

        @Test
        void setPageNumberThrowsForNegative() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(IllegalArgumentException.class, () -> pageable.setPageNumber(-1));
        }

        @Test
        void setPageNumberAcceptsPositive() {
            PageableImpl pageable = new PageableImpl(1, 10);
            pageable.setPageNumber(5);
            assertEquals(5, pageable.getPageNumber());
        }
    }

    @Nested
    class SortFieldManagement {

        @Test
        void addReturnsSameInstance() {
            PageableImpl pageable = new PageableImpl(1, 10);
            Pageable result = pageable.add("name", Direction.ASC);
            assertEquals(pageable, result);
        }

        @Test
        void addStoresFieldNameAndDirection() {
            PageableImpl pageable = new PageableImpl(1, 10);
            pageable.add("name", Direction.ASC);
            pageable.add("createdAt", Direction.DESC);

            Map<String, Direction> fieldMap = pageable.getFieldNameAndDirectionMap();
            assertEquals(2, fieldMap.size());
            assertEquals(Direction.ASC, fieldMap.get("name"));
            assertEquals(Direction.DESC, fieldMap.get("createdAt"));
        }

        @Test
        void getColumnNameAndDirectionMapReturnsEmptyMap() {
            PageableImpl pageable = new PageableImpl(1, 10);
            Map<String, Direction> columnMap = pageable.getColumnNameAndDirectionMap();
            assertTrue(columnMap.isEmpty());
        }
    }

    @Nested
    class ToSql {

        @Test
        void throwsNotImplementedException() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(NotImplementedException.class, pageable::toSql);
        }
    }

    @Nested
    class Builder {

        @Test
        void buildsWithPageNumberAndSize() {
            PageableImpl pageable = PageableImpl.builder()
                    .pageNumber(3)
                    .pageSize(20)
                    .build();

            assertEquals(3, pageable.getPageNumber());
            assertEquals(20, pageable.getPageSize());
        }

        @Test
        void buildsWithDefaultValues() {
            PageableImpl pageable = PageableImpl.builder().build();
            assertEquals(0, pageable.getPageNumber());
            assertEquals(0, pageable.getPageSize());
        }
    }

    @Nested
    class StaticOfPageable {

        @Test
        void createsPageableFromStaticMethod() {
            PageableImpl pageable = Pageable.ofPageable(2, 15);
            assertEquals(2, pageable.getPageNumber());
            assertEquals(15, pageable.getPageSize());
        }
    }

    @Nested
    class NotImplementedMethods {

        @Test
        void setColumnNameAndDirectionMapThrows() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(NotImplementedException.class,
                    () -> pageable.setColumnNameAndDirectionMap(Map.of()));
        }

        @Test
        void getEntityClassThrows() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(NotImplementedException.class, pageable::getEntityClass);
        }

        @Test
        void setEntityClassThrows() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(NotImplementedException.class,
                    () -> pageable.setEntityClass(String.class));
        }

        @Test
        void addNewColumnAndDirectionOrderThrows() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(NotImplementedException.class,
                    () -> pageable.addNewColumnAndDirectionOrder("col", Direction.ASC));
        }

        @Test
        void fieldToColumnThrows() {
            PageableImpl pageable = new PageableImpl(1, 10);
            assertThrows(NotImplementedException.class, pageable::fieldToColumn);
        }
    }
}
