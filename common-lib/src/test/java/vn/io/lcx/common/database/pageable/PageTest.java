package vn.io.lcx.common.database.pageable;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageTest {

    @Nested
    class CreateWithIntTotal {

        @Test
        void createsPageWithCorrectMetadata() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = Page.create(content, 30, 1, 10);

            assertEquals(1, page.getPageNumber());
            assertEquals(10, page.getPageSize());
            assertEquals(3, page.getNumberOfElements());
            assertEquals(30L, page.getTotalElements());
            assertEquals(content, page.getContent());
        }

        @Test
        void calculatesCorrectTotalPages() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = Page.create(content, 25, 1, 10);

            assertEquals(3, page.getTotalPages());
        }

        @Test
        void calculatesCorrectTotalPagesExactDivision() {
            List<String> content = Arrays.asList("a", "b");
            Page<String> page = Page.create(content, 20, 1, 10);

            assertEquals(2, page.getTotalPages());
        }
    }

    @Nested
    class CreateWithLongTotal {

        @Test
        void createsPageWithLongTotalElements() {
            List<String> content = Arrays.asList("a", "b");
            Page<String> page = Page.create(content, 100L, 2, 10);

            assertEquals(2, page.getPageNumber());
            assertEquals(10, page.getPageSize());
            assertEquals(2, page.getNumberOfElements());
            assertEquals(100L, page.getTotalElements());
        }
    }

    @Nested
    class FirstLastPageDetection {

        @Test
        void firstPageIsDetected() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = Page.create(content, 30, 1, 10);

            assertTrue(page.getFirstPage());
            assertFalse(page.getLastPage());
        }

        @Test
        void lastPageIsDetected() {
            List<String> content = Arrays.asList("a", "b");
            Page<String> page = Page.create(content, 22, 3, 10);

            assertFalse(page.getFirstPage());
            assertTrue(page.getLastPage());
        }

        @Test
        void singlePageIsBothFirstAndLast() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = Page.create(content, 3, 1, 10);

            assertTrue(page.getFirstPage());
            assertTrue(page.getLastPage());
        }

        @Test
        void middlePageIsNeitherFirstNorLast() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = Page.create(content, 30, 2, 10);

            assertFalse(page.getFirstPage());
            assertFalse(page.getLastPage());
        }
    }

    @Nested
    class TotalPagesCalculation {

        @Test
        void zeroTotalPagesForEmptyList() {
            List<String> content = Collections.emptyList();
            Page<String> page = Page.create(content, 0, 1, 10);

            assertEquals(0, page.getTotalPages());
        }

        @Test
        void onePageWhenTotalLessThanSize() {
            List<String> content = Arrays.asList("a", "b");
            Page<String> page = Page.create(content, 5, 1, 10);

            assertEquals(1, page.getTotalPages());
        }

        @Test
        void multiplePagesWithRemainder() {
            List<String> content = Arrays.asList("a", "b", "c");
            Page<String> page = Page.create(content, 31, 1, 10);

            assertEquals(4, page.getTotalPages());
        }
    }

    @Nested
    class ContentTransformation {

        @Test
        void transformsContentUsingConvertFunction() {
            List<Integer> originalContent = Arrays.asList(1, 2, 3);
            Page<Integer> originalPage = Page.create(originalContent, 30, 1, 10);

            Page<String> transformedPage = Page.create(originalPage, String::valueOf);

            assertEquals(Arrays.asList("1", "2", "3"), transformedPage.getContent());
            assertEquals(originalPage.getPageNumber(), transformedPage.getPageNumber());
            assertEquals(originalPage.getPageSize(), transformedPage.getPageSize());
            assertEquals(originalPage.getTotalPages(), transformedPage.getTotalPages());
            assertEquals(originalPage.getNumberOfElements(), transformedPage.getNumberOfElements());
            assertEquals(originalPage.getTotalElements(), transformedPage.getTotalElements());
            assertEquals(originalPage.getFirstPage(), transformedPage.getFirstPage());
            assertEquals(originalPage.getLastPage(), transformedPage.getLastPage());
        }

        @Test
        void transformsEmptyContent() {
            List<Integer> originalContent = Collections.emptyList();
            Page<Integer> originalPage = Page.create(originalContent, 0, 1, 10);

            Page<String> transformedPage = Page.create(originalPage, String::valueOf);

            assertTrue(transformedPage.getContent().isEmpty());
        }
    }

    @Nested
    class EmptyPage {

        @Test
        void emptyPageHasZeroElements() {
            List<String> content = Collections.emptyList();
            Page<String> page = Page.create(content, 0, 1, 10);

            assertEquals(0, page.getNumberOfElements());
            assertEquals(0L, page.getTotalElements());
            assertEquals(0, page.getTotalPages());
        }

        @Test
        void emptyContentIsReturned() {
            List<String> content = Collections.emptyList();
            Page<String> page = Page.create(content, 0, 1, 10);

            assertNotNull(page.getContent());
            assertTrue(page.getContent().isEmpty());
        }
    }

    @Nested
    class DefaultConstructor {

        @Test
        void createsEmptyPage() {
            Page<String> page = new Page<>();
            // All fields should be null with default constructor
            // No NPE should happen
            assertNotNull(page);
        }

        @Test
        void allArgsConstructorSetsAllFields() {
            List<String> content = Arrays.asList("a", "b");
            Page<String> page = new Page<>(1, 10, 5, 2, 50L, true, false, content);

            assertEquals(1, page.getPageNumber());
            assertEquals(10, page.getPageSize());
            assertEquals(5, page.getTotalPages());
            assertEquals(2, page.getNumberOfElements());
            assertEquals(50L, page.getTotalElements());
            assertTrue(page.getFirstPage());
            assertFalse(page.getLastPage());
            assertEquals(content, page.getContent());
        }
    }
}
