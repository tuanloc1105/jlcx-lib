package vn.io.lcx.vertx.base.validate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.vertx.base.exception.InternalServiceException;
import vn.io.lcx.vertx.base.validate.testdto.AllAnnotationsDto;
import vn.io.lcx.vertx.base.validate.testdto.NestedDto;
import vn.io.lcx.vertx.base.validate.testdto.NotNullDto;
import vn.io.lcx.vertx.base.validate.testdto.NumericBoundsDto;
import vn.io.lcx.vertx.base.validate.testdto.RegexDto;
import vn.io.lcx.vertx.base.validate.testdto.ValuesDto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AutoValidation")
class AutoValidationTest {

    @Nested
    @DisplayName("@NotNull validation")
    class NotNullValidation {

        @Test
        @DisplayName("validate_nullRequiredField_returnsError")
        void validate_nullRequiredField_returnsError() {
            NotNullDto dto = new NotNullDto(null, "optional");

            List<String> errors = AutoValidation.validate(dto);

            assertFalse(errors.isEmpty(), "Should have validation errors");
            assertTrue(errors.get(0).contains("must not be null or empty"));
        }

        @Test
        @DisplayName("validate_emptyRequiredField_returnsError")
        void validate_emptyRequiredField_returnsError() {
            NotNullDto dto = new NotNullDto("", "optional");

            List<String> errors = AutoValidation.validate(dto);

            assertFalse(errors.isEmpty(), "Should have validation errors for empty string");
            assertTrue(errors.get(0).contains("must not be null or empty"));
        }

        @Test
        @DisplayName("validate_blankRequiredField_returnsError")
        void validate_blankRequiredField_returnsError() {
            NotNullDto dto = new NotNullDto("   ", "optional");

            List<String> errors = AutoValidation.validate(dto);

            assertFalse(errors.isEmpty(), "Should have validation errors for blank string");
            assertTrue(errors.get(0).contains("must not be null or empty"));
        }

        @Test
        @DisplayName("validate_validObject_noErrors")
        void validate_validObject_noErrors() {
            NotNullDto dto = new NotNullDto("validValue", "optional");

            List<String> errors = AutoValidation.validate(dto);

            assertTrue(errors.isEmpty(), "Should have no validation errors");
        }

        @Test
        @DisplayName("validate_optionalField_nullAllowed")
        void validate_optionalField_nullAllowed() {
            NotNullDto dto = new NotNullDto("validValue", null);

            List<String> errors = AutoValidation.validate(dto);

            assertTrue(errors.isEmpty(), "Optional field should allow null");
        }

        @Test
        @DisplayName("validate_nullObject_returnsEmptyList")
        void validate_nullObject_returnsEmptyList() {
            List<String> errors = AutoValidation.validate(null);

            assertTrue(errors.isEmpty(), "Null object should return empty error list");
        }
    }

    @Nested
    @DisplayName("@Regex validation")
    class RegexValidation {

        @Test
        @DisplayName("validate_regexField_invalidPattern_throwsException")
        void validate_regexField_invalidPattern_throwsException() {
            RegexDto dto = new RegexDto("invalid!@#");

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("value is invalid (regex mismatch)"));
            assertEquals(400, exception.getHttpCode());
        }

        @Test
        @DisplayName("validate_regexField_validPattern_passes")
        void validate_regexField_validPattern_passes() {
            RegexDto dto = new RegexDto("ValidAlphaNumeric123");

            List<String> errors = assertDoesNotThrow(() -> AutoValidation.validate(dto));

            assertTrue(errors.isEmpty(), "Valid regex match should produce no errors");
        }

        @Test
        @DisplayName("validate_regexField_blankValue_throwsException")
        void validate_regexField_blankValue_throwsException() {
            RegexDto dto = new RegexDto("");

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must not be null or empty"));
        }

        @Test
        @DisplayName("validate_regexField_nullValue_throwsException")
        void validate_regexField_nullValue_throwsException() {
            // When @Regex field is null, the source code uses `assert fieldValue instanceof String`
            // which throws AssertionError if JVM assertions are enabled (-ea).
            // If assertions are disabled, StringUtils.isBlank(null) returns true and throws
            // InternalServiceException. Either way, an exception is thrown for null @Regex fields.
            RegexDto dto = new RegexDto(null);

            assertThrows(
                    Throwable.class,
                    () -> AutoValidation.validate(dto)
            );
        }
    }

    @Nested
    @DisplayName("@Values validation")
    class ValuesValidation {

        @Test
        @DisplayName("validate_valuesField_invalidValue_throwsException")
        void validate_valuesField_invalidValue_throwsException() {
            ValuesDto dto = new ValuesDto("BITCOIN");

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be like one of these"));
            assertTrue(exception.getMessage().contains("CASH"));
            assertTrue(exception.getMessage().contains("CARD"));
            assertTrue(exception.getMessage().contains("TRANSFER"));
        }

        @Test
        @DisplayName("validate_valuesField_validValue_passes")
        void validate_valuesField_validValue_passes() {
            ValuesDto dto = new ValuesDto("CASH");

            List<String> errors = assertDoesNotThrow(() -> AutoValidation.validate(dto));

            assertTrue(errors.isEmpty(), "Valid values match should produce no errors");
        }

        @Test
        @DisplayName("validate_valuesField_nullValue_throwsException")
        void validate_valuesField_nullValue_throwsException() {
            ValuesDto dto = new ValuesDto(null);

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must not null"));
        }

        @Test
        @DisplayName("validate_valuesField_anotherValidValue_passes")
        void validate_valuesField_anotherValidValue_passes() {
            ValuesDto dto = new ValuesDto("TRANSFER");

            List<String> errors = assertDoesNotThrow(() -> AutoValidation.validate(dto));

            assertTrue(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("@GreaterThan / @LessThan validation")
    class NumericBoundsValidation {

        @Test
        @DisplayName("validate_greaterThan_violation_throwsException")
        void validate_greaterThan_violation_throwsException() {
            NumericBoundsDto dto = new NumericBoundsDto(0, 50.0, new BigDecimal("500"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be greater than"));
        }

        @Test
        @DisplayName("validate_greaterThan_negativeValue_throwsException")
        void validate_greaterThan_negativeValue_throwsException() {
            NumericBoundsDto dto = new NumericBoundsDto(-5, 50.0, new BigDecimal("500"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be greater than"));
        }

        @Test
        @DisplayName("validate_lessThan_violation_throwsException")
        void validate_lessThan_violation_throwsException() {
            NumericBoundsDto dto = new NumericBoundsDto(5, 150.0, new BigDecimal("500"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be less than"));
        }

        @Test
        @DisplayName("validate_lessThan_equalToLimit_throwsException")
        void validate_lessThan_equalToLimit_throwsException() {
            // LessThan uses >= comparison, so equal to limit should throw
            NumericBoundsDto dto = new NumericBoundsDto(5, 100.0, new BigDecimal("500"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be less than"));
        }

        @Test
        @DisplayName("validate_greaterThan_equalToLimit_throwsException")
        void validate_greaterThan_equalToLimit_throwsException() {
            // GreaterThan uses <= comparison, so equal to limit should throw
            NumericBoundsDto dto = new NumericBoundsDto(0, 50.0, new BigDecimal("500"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be greater than"));
        }

        @Test
        @DisplayName("validate_allBoundsValid_noErrors")
        void validate_allBoundsValid_noErrors() {
            NumericBoundsDto dto = new NumericBoundsDto(5, 50.0, new BigDecimal("500"));

            List<String> errors = assertDoesNotThrow(() -> AutoValidation.validate(dto));

            assertTrue(errors.isEmpty(), "Valid numeric bounds should produce no errors");
        }

        @Test
        @DisplayName("validate_bigDecimalGreaterThan_violation_throwsException")
        void validate_bigDecimalGreaterThan_violation_throwsException() {
            NumericBoundsDto dto = new NumericBoundsDto(5, 50.0, new BigDecimal("-1"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be greater than"));
        }

        @Test
        @DisplayName("validate_bigDecimalLessThan_violation_throwsException")
        void validate_bigDecimalLessThan_violation_throwsException() {
            NumericBoundsDto dto = new NumericBoundsDto(5, 50.0, new BigDecimal("2000000"));

            InternalServiceException exception = assertThrows(
                    InternalServiceException.class,
                    () -> AutoValidation.validate(dto)
            );

            assertTrue(exception.getMessage().contains("must be less than"));
        }

        @Test
        @DisplayName("validate_nullNumericField_skipsValidation")
        void validate_nullNumericField_skipsValidation() {
            // When numeric fields are null, GreaterThan/LessThan block is skipped
            // because the if condition checks fieldValue != null
            NumericBoundsDto dto = new NumericBoundsDto(null, null, null);

            List<String> errors = assertDoesNotThrow(() -> AutoValidation.validate(dto));

            assertTrue(errors.isEmpty(), "Null numeric fields should skip bounds validation");
        }
    }

    @Nested
    @DisplayName("Nested object validation")
    class NestedObjectValidation {

        @Test
        @DisplayName("validate_nestedObject_validatesRecursively")
        void validate_nestedObject_validatesRecursively() {
            // Child has @NotNull requiredField set to null
            NotNullDto child = new NotNullDto(null, "optional");
            NestedDto dto = new NestedDto("parent", child);

            List<String> errors = AutoValidation.validate(dto);

            assertFalse(errors.isEmpty(), "Should detect nested validation errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("must not be null or empty")));
        }

        @Test
        @DisplayName("validate_nestedObject_allValid_noErrors")
        void validate_nestedObject_allValid_noErrors() {
            NotNullDto child = new NotNullDto("childValue", "optional");
            NestedDto dto = new NestedDto("parent", child);

            List<String> errors = AutoValidation.validate(dto);

            assertTrue(errors.isEmpty(), "All valid nested object should produce no errors");
        }

        @Test
        @DisplayName("validate_nestedObject_parentInvalid_returnsError")
        void validate_nestedObject_parentInvalid_returnsError() {
            NotNullDto child = new NotNullDto("childValue", "optional");
            NestedDto dto = new NestedDto(null, child);

            List<String> errors = AutoValidation.validate(dto);

            assertFalse(errors.isEmpty(), "Should detect parent-level validation errors");
            assertTrue(errors.stream().anyMatch(e -> e.contains("must not be null or empty")));
        }

        @Test
        @DisplayName("validate_nestedObject_nullChild_noErrors")
        void validate_nestedObject_nullChild_noErrors() {
            // Child is null but not annotated with @NotNull, so no error
            NestedDto dto = new NestedDto("parent", null);

            List<String> errors = AutoValidation.validate(dto);

            assertTrue(errors.isEmpty(), "Null child without @NotNull should produce no errors");
        }
    }

    @Nested
    @DisplayName("@SerializedName support")
    class SerializedNameSupport {

        @Test
        @DisplayName("validate_serializedName_usedInErrorMessage")
        void validate_serializedName_usedInErrorMessage() {
            // AllAnnotationsDto has @SerializedName("user_name") on userName
            AllAnnotationsDto dto = new AllAnnotationsDto(null, "123-456-7890", "ACTIVE", 25);

            List<String> errors = AutoValidation.validate(dto);

            assertFalse(errors.isEmpty());
            // The error message should use "user_name" (from @SerializedName), not "userName"
            assertTrue(errors.get(0).contains("user_name"),
                    "Error message should use @SerializedName value");
        }
    }

    @Nested
    @DisplayName("ableToValidate")
    class AbleToValidateTest {

        @Test
        @DisplayName("ableToValidate_javaObject_returnsFalse")
        void ableToValidate_javaObject_returnsFalse() {
            assertFalse(AutoValidation.ableToValidate("a string"));
            assertFalse(AutoValidation.ableToValidate(Integer.valueOf(42)));
        }

        @Test
        @DisplayName("ableToValidate_customObject_returnsTrue")
        void ableToValidate_customObject_returnsTrue() {
            assertTrue(AutoValidation.ableToValidate(new NotNullDto()));
        }

        @Test
        @DisplayName("ableToValidate_enum_returnsFalse")
        void ableToValidate_enum_returnsFalse() {
            assertFalse(AutoValidation.ableToValidate(Thread.State.NEW));
        }
    }
}
