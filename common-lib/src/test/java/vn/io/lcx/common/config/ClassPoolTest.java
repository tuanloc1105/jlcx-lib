package vn.io.lcx.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.common.exception.DuplicateInstancesException;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ClassPoolTest {

    @BeforeEach
    void clearClassPool() throws Exception {
        // Clear CLASS_POOL
        Field classPoolField = ClassPool.class.getDeclaredField("CLASS_POOL");
        classPoolField.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) classPoolField.get(null)).clear();

        // Clear ENTITIES
        Field entitiesField = ClassPool.class.getDeclaredField("ENTITIES");
        entitiesField.setAccessible(true);
        ((List<?>) entitiesField.get(null)).clear();
    }

    // -- Simple helper types for testing polymorphic registration --

    interface Greeter {
        String greet();
    }

    static class HelloGreeter implements Greeter, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String greet() {
            return "hello";
        }
    }

    // -- Tests --

    static class ServiceA {
        String value = "A";
    }

    static class ServiceB {
        String value = "B";
    }

    @Test
    void setInstance_withName_registersSuccessfully() {
        String instanceName = "myService";
        ServiceA instance = new ServiceA();

        ClassPool.setInstance(instanceName, instance);

        assertSame(instance, ClassPool.getInstance(instanceName));
    }

    @Test
    void setInstance_duplicateName_throwsDuplicateInstancesException() {
        String instanceName = "duplicateService";
        ClassPool.setInstance(instanceName, new ServiceA());

        assertThrows(DuplicateInstancesException.class,
                () -> ClassPool.setInstance(instanceName, new ServiceB()));
    }

    @Test
    void setInstance_withObject_registersUnderMultipleKeys() {
        HelloGreeter greeter = new HelloGreeter();
        ClassPool.setInstance(greeter);

        // Accessible by fully qualified class name
        assertSame(greeter, ClassPool.getInstance(HelloGreeter.class.getName()));
        // Accessible by simple class name
        assertSame(greeter, ClassPool.getInstance("HelloGreeter"));
        // Accessible by interface fully qualified name
        assertSame(greeter, ClassPool.getInstance(Greeter.class.getName()));
        // Accessible by interface simple name
        assertSame(greeter, ClassPool.getInstance("Greeter"));
    }

    @Test
    void getInstance_byName_returnsInstance() {
        String name = "namedInstance";
        ServiceA value = new ServiceA();
        ClassPool.setInstance(name, value);

        Object result = ClassPool.getInstance(name);

        assertSame(value, result);
    }

    @Test
    void getInstance_byClass_returnsInstance() {
        HelloGreeter greeter = new HelloGreeter();
        ClassPool.setInstance(greeter);

        HelloGreeter result = ClassPool.getInstance(HelloGreeter.class);

        assertSame(greeter, result);
    }

    @Test
    void getInstance_typeSafe_returnsCorrectType() {
        HelloGreeter greeter = new HelloGreeter();
        ClassPool.setInstance("helloGreeter", greeter);

        HelloGreeter result = ClassPool.getInstance("helloGreeter", HelloGreeter.class);

        assertNotNull(result);
        assertSame(greeter, result);
        assertEquals("hello", result.greet());
    }

    @Test
    void getInstance_nonExistent_returnsNull() {
        Object result = ClassPool.getInstance("nonExistentKey");

        assertNull(result);
    }

    @Test
    void getEntities_returnsUnmodifiableList() {
        List<Class<?>> entities = ClassPool.getEntities();

        assertNotNull(entities);
        assertThrows(UnsupportedOperationException.class,
                () -> entities.add(String.class));
    }

    @Test
    void loadProperties_loadsApplicationYaml() {
        // Use the application-test.yaml in test resources via system property
        String originalProp = System.getProperty("application_config.file");
        try {
            // Clear the system property so loadProperties falls through to classpath resource.
            // There is no application.yaml on the common-lib test classpath,
            // but loadProperties should still succeed without throwing.
            System.clearProperty("application_config.file");
            ClassPool.loadProperties();

            // After loadProperties, applicationConfig should be non-null (even if empty)
            assertNotNull(CommonConstant.applicationConfig);
        } finally {
            // Restore original system property
            if (originalProp != null) {
                System.setProperty("application_config.file", originalProp);
            } else {
                System.clearProperty("application_config.file");
            }
        }
    }
}
