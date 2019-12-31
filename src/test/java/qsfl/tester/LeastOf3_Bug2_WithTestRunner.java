package qsfl.tester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class LeastOf3_Bug2_WithTestRunner {
    private Class classToTest;

    public LeastOf3_Bug2_WithTestRunner(Class toTest){
        classToTest = toTest;
    }

    @Parameterized.Parameters
    public static Collection classes(){
        File dir = new File("src/fault_localization.reports.main/java/qsfl/tester/leastof");

        return Stream.of(dir.list())
                .filter(name -> name.startsWith("LeastOf"))
                .map(name -> name.split("\\.")[0])
                .map(name -> {
                    Class[] res = null;
                    try {
                        res = new Class[]{Class.forName("qsfl.tester.leastof." + name)};
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return res;
                })
                .collect(Collectors.toList());
    }

    private static Method getMethodToTest(Class c, String method, Class<?>... params){
        Method m = null;
        try {
            m = c.getDeclaredMethod(method, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return m;
    }

    private static void performCheck(Class c, Method methodToTest, Object expected, Object... params){
        try {
            assertEquals(expected, methodToTest.invoke(null, params));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void test1(){
        Method methodToTest = getMethodToTest(classToTest, "leastOf_bug2", int.class, int.class, int.class);
        System.out.println("Class to test: " + classToTest);
        performCheck(classToTest, methodToTest, 1, 1,2,3);
    }


    @Test
    public void test2(){
        Method methodToTest = getMethodToTest(classToTest, "leastOf_bug2", int.class, int.class, int.class);
        System.out.println("Class to test: " + classToTest);
        performCheck(classToTest, methodToTest, 2, 5,2,3);
    }


    @Test
    public void test3(){
        Method methodToTest = getMethodToTest(classToTest, "leastOf_bug2", int.class, int.class, int.class);
        System.out.println("Class to test: " + classToTest);
        performCheck(classToTest, methodToTest, 3, 5,6,3);
    }


    @Test
    public void test4(){
        Method methodToTest = getMethodToTest(classToTest, "leastOf_bug2", int.class, int.class, int.class);
        System.out.println("Class to test: " + classToTest);
        performCheck(classToTest, methodToTest, 1, 1,3,2);
    }


    @Test
    public void test5(){
        Method methodToTest = getMethodToTest(classToTest, "leastOf_bug2", int.class, int.class, int.class);
        System.out.println("Class to test: " + classToTest);
        performCheck(classToTest, methodToTest, 2, 3,2,5);
    }


    @Test
    public void test6(){
        Method methodToTest = getMethodToTest(classToTest, "leastOf_bug2", int.class, int.class, int.class);
        System.out.println("Class to test: " + classToTest);
        performCheck(classToTest, methodToTest, 3, 6,5,3);
    }
}
