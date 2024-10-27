import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

// Sample Class Hierarchy
class A {
    B b;
    public A(B b) {
        this.b = b;
    }
}

class B {
    C c;
    public B(C c) {
        this.c = c;
    }
}

class C {
    public C() {}
}

public class ClassInitializationSanityTest {
    public static void main(String[] args) {
        try {
            Set<Class<?>> instantiatedClasses = new HashSet<>();
            initializeClass(A.class, instantiatedClasses);
            System.out.println("All classes initialized successfully without circular dependencies.");
        } catch (Exception e) {
            System.err.println("Initialization failed: " + e.getMessage());
        }
    }

    private static Object initializeClass(Class<?> clazz, Set<Class<?>> instantiatedClasses) 
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (instantiatedClasses.contains(clazz)) {
            throw new InstantiationException("Circular dependency detected for class: " + clazz.getName());
        }

        instantiatedClasses.add(clazz);

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            } else {
                Object[] parameters = new Object[constructor.getParameterCount()];
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = initializeClass(parameterTypes[i], instantiatedClasses);
                }
                return constructor.newInstance(parameters);
            }
        }
        instantiatedClasses.remove(clazz);
        return null;
    }
}
