package vn.io.lcx.reactive.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReactiveExceptionsTest {

    // --- EmptySelectStatementException ---

    @Nested
    class EmptySelectStatementExceptionTests {

        @Test
        void defaultConstructor_hasDefaultMessage() {
            EmptySelectStatementException ex = new EmptySelectStatementException();
            assertEquals("The select statement cannot empty", ex.getMessage());
            assertNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void messageConstructor_usesProvidedMessage() {
            EmptySelectStatementException ex = new EmptySelectStatementException("custom select error");
            assertEquals("custom select error", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    // --- EmptyFromStatementException ---

    @Nested
    class EmptyFromStatementExceptionTests {

        @Test
        void defaultConstructor_hasDefaultMessage() {
            EmptyFromStatementException ex = new EmptyFromStatementException();
            assertEquals("The from statement cannot empty", ex.getMessage());
            assertNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void messageConstructor_usesProvidedMessage() {
            EmptyFromStatementException ex = new EmptyFromStatementException("custom from error");
            assertEquals("custom from error", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    // --- EmptyConditionStatementException ---

    @Nested
    class EmptyConditionStatementExceptionTests {

        @Test
        void defaultConstructor_hasDefaultMessage() {
            EmptyConditionStatementException ex = new EmptyConditionStatementException();
            assertEquals("The condition statement cannot empty", ex.getMessage());
            assertNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void messageConstructor_usesProvidedMessage() {
            EmptyConditionStatementException ex = new EmptyConditionStatementException("custom condition error");
            assertEquals("custom condition error", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    // --- EmptyGroupByStatementException ---

    @Nested
    class EmptyGroupByStatementExceptionTests {

        @Test
        void defaultConstructor_hasDefaultMessage() {
            EmptyGroupByStatementException ex = new EmptyGroupByStatementException();
            assertEquals("The group by statement cannot empty", ex.getMessage());
            assertNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void messageConstructor_usesProvidedMessage() {
            EmptyGroupByStatementException ex = new EmptyGroupByStatementException("custom group by error");
            assertEquals("custom group by error", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    // --- EmptyOrderStatementException ---

    @Nested
    class EmptyOrderStatementExceptionTests {

        @Test
        void defaultConstructor_hasDefaultMessage() {
            EmptyOrderStatementException ex = new EmptyOrderStatementException();
            assertEquals("The order statement cannot empty", ex.getMessage());
            assertNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void messageConstructor_usesProvidedMessage() {
            EmptyOrderStatementException ex = new EmptyOrderStatementException("custom order error");
            assertEquals("custom order error", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    // --- NonUniqueQueryResult ---

    @Nested
    class NonUniqueQueryResultTests {

        @Test
        void defaultConstructor_hasDefaultMessage() {
            NonUniqueQueryResult ex = new NonUniqueQueryResult();
            assertEquals("The result of query return more than 1 row", ex.getMessage());
            assertNull(ex.getCause());
            assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        void messageConstructor_usesProvidedMessage() {
            NonUniqueQueryResult ex = new NonUniqueQueryResult("custom non-unique error");
            assertEquals("custom non-unique error", ex.getMessage());
            assertNull(ex.getCause());
        }
    }

    // --- Cross-cutting: all are RuntimeExceptions ---

    @Test
    void allExceptions_areRuntimeExceptions() {
        assertInstanceOf(RuntimeException.class, new EmptySelectStatementException());
        assertInstanceOf(RuntimeException.class, new EmptyFromStatementException());
        assertInstanceOf(RuntimeException.class, new EmptyConditionStatementException());
        assertInstanceOf(RuntimeException.class, new EmptyGroupByStatementException());
        assertInstanceOf(RuntimeException.class, new EmptyOrderStatementException());
        assertInstanceOf(RuntimeException.class, new NonUniqueQueryResult());
    }

    @Test
    void allExceptions_defaultMessages_areNotNull() {
        assertNotNull(new EmptySelectStatementException().getMessage());
        assertNotNull(new EmptyFromStatementException().getMessage());
        assertNotNull(new EmptyConditionStatementException().getMessage());
        assertNotNull(new EmptyGroupByStatementException().getMessage());
        assertNotNull(new EmptyOrderStatementException().getMessage());
        assertNotNull(new NonUniqueQueryResult().getMessage());
    }
}
