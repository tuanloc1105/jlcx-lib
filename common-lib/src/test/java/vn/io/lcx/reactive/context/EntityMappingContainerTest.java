package vn.io.lcx.reactive.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.lcx.jpa.exception.IllegalEntityClassException;
import vn.io.lcx.reactive.entity.EntityMapping;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.sql.ResultSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityMappingContainerTest {

    /**
     * A minimal test entity class.
     */
    private static class TestEntity {
        private int id;
        private String name;

        public TestEntity() {
        }

        public TestEntity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Stub EntityMapping implementation for testing.
     * Must be public so that EntityMappingContainer.addMapping(Class) can reflectively instantiate it.
     */
    public static class TestEntityMapping implements EntityMapping<TestEntity> {
        @Override
        public TestEntity resultSetMapping(ResultSet resultSet) {
            return new TestEntity(1, "test");
        }

        @Override
        public String insertStatement(TestEntity model) {
            return "INSERT INTO test (id, name) VALUES (?, ?)";
        }

        @Override
        public String updateStatement(TestEntity model) {
            return "UPDATE test SET name = ? WHERE id = ?";
        }

        @Override
        public String deleteStatement(TestEntity model) {
            return "DELETE FROM test WHERE id = ?";
        }

        @Override
        public String reactiveInsertStatement(TestEntity model, String placeHolder) {
            return "INSERT INTO test (id, name) VALUES (" + placeHolder + ", " + placeHolder + ")";
        }

        @Override
        public String reactiveUpdateStatement(TestEntity model, String placeHolder) {
            return "UPDATE test SET name = " + placeHolder + " WHERE id = " + placeHolder;
        }

        @Override
        public String reactiveDeleteStatement(TestEntity model, String placeHolder) {
            return "DELETE FROM test WHERE id = " + placeHolder;
        }

        @Override
        public Map<Integer, Object> insertJDBCParams(TestEntity model) {
            return Map.of(1, model.getId(), 2, model.getName());
        }

        @Override
        public Map<Integer, Object> updateJDBCParams(TestEntity model) {
            return Map.of(1, model.getName(), 2, model.getId());
        }

        @Override
        public Map<Integer, Object> deleteJDBCParams(TestEntity model) {
            return Map.of(1, model.getId());
        }

        @Override
        public TestEntity vertxRowMapping(Row row) {
            return new TestEntity(1, "test");
        }

        @Override
        public Tuple insertTupleParam(TestEntity model) {
            return Tuple.of(model.getId(), model.getName());
        }

        @Override
        public Tuple updateTupleParam(TestEntity model) {
            return Tuple.of(model.getName(), model.getId());
        }

        @Override
        public Tuple deleteTupleParam(TestEntity model) {
            return Tuple.of(model.getId());
        }

        @Override
        public String getColumnNameFromFieldName(String fieldName) {
            return switch (fieldName) {
                case "id" -> "id";
                case "name" -> "name";
                default -> throw new IllegalArgumentException("Unknown field: " + fieldName);
            };
        }
    }

    @BeforeEach
    void setUp() {
        // Note: EntityMappingContainer uses static maps. Each test adds its own unique key
        // to avoid collisions.
    }

    @Test
    void addMapping_and_getMapping_roundTrip() {
        String className = "vn.io.lcx.reactive.context.EntityMappingContainerTest$TestEntity_roundTrip";
        EntityMapping<TestEntity> mapping = new TestEntityMapping();

        EntityMappingContainer.addMapping(className, mapping);

        EntityMapping<TestEntity> retrieved = EntityMappingContainer.getMapping(className);
        assertNotNull(retrieved);
        // Verify the retrieved mapping produces the expected result
        TestEntity entity = retrieved.resultSetMapping(null);
        assertEquals(1, entity.getId());
        assertEquals("test", entity.getName());
    }

    @Test
    void addMapping_and_getMapping_returnsCorrectMappingInstance() {
        String className = "vn.io.lcx.reactive.context.EntityMappingContainerTest$TestEntity_instance";
        TestEntityMapping mapping = new TestEntityMapping();

        EntityMappingContainer.addMapping(className, mapping);

        EntityMapping<TestEntity> retrieved = EntityMappingContainer.getMapping(className);
        assertNotNull(retrieved);

        // Verify it produces correct SQL
        TestEntity entity = new TestEntity(42, "Alice");
        String insertSql = retrieved.insertStatement(entity);
        assertEquals("INSERT INTO test (id, name) VALUES (?, ?)", insertSql);
    }

    @Test
    void getMapping_nonExistent_throwsIllegalEntityClassException() {
        assertThrows(IllegalEntityClassException.class,
                () -> EntityMappingContainer.getMapping("com.example.NonExistentClass"));
    }

    @Test
    void getMapping_nonExistent_exceptionContainsClassName() {
        String className = "com.example.DoesNotExist";

        IllegalEntityClassException ex = assertThrows(IllegalEntityClassException.class,
                () -> EntityMappingContainer.getMapping(className));

        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains(className));
    }

    @Test
    void addMapping_byClass_registersMapping() {
        // TestEntityMapping implements EntityMapping<TestEntity>
        EntityMappingContainer.addMapping(TestEntityMapping.class);

        // The type argument extracted from TestEntityMapping is TestEntity's full class name
        String expectedKey = TestEntity.class.getName();
        EntityMapping<TestEntity> retrieved = EntityMappingContainer.getMapping(expectedKey);
        assertNotNull(retrieved);
    }

    @Test
    void addMapping_multipleMappings_eachRetrievable() {
        String className1 = "vn.io.lcx.reactive.context.EntityMappingContainerTest$Entity1";
        String className2 = "vn.io.lcx.reactive.context.EntityMappingContainerTest$Entity2";

        EntityMapping<TestEntity> mapping1 = new TestEntityMapping();
        EntityMapping<TestEntity> mapping2 = new TestEntityMapping();

        EntityMappingContainer.addMapping(className1, mapping1);
        EntityMappingContainer.addMapping(className2, mapping2);

        assertNotNull(EntityMappingContainer.getMapping(className1));
        assertNotNull(EntityMappingContainer.getMapping(className2));
    }

    private static boolean assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
        return condition;
    }
}
