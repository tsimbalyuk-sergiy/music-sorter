//package dev;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class List {
//    public static void main(String[] args) throws IOException {
//        findAllClassesUsingGoogleGuice("org.jaudiotagger.tag.id3.framebody").forEach((Class x) -> {
//            if (!x.isInterface()) {
//
//                System.out.println(x.getName());
//            }
//        });
//    }
//    public static Set<Class> findAllClassesUsingGoogleGuice(String packageName) throws IOException {
//        return ClassPath.from(ClassLoader.getSystemClassLoader())
//                .getAllClasses()
//                .stream()
//                .filter(clazz -> clazz.getPackageName()
//                        .equalsIgnoreCase(packageName))
//                .map(clazz -> clazz.load())
//                .collect(Collectors.toSet());
//    }
//    public static Set<Class> findAllClassesUsingClassLoader(String packageName) {
//        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"));
//        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//        return reader.lines()
//                .filter(line -> line.endsWith(".class"))
//                .map(line -> {
//                    Class aClass = getClass(line, packageName);
//                    return aClass;
//                })
//                .collect(Collectors.toSet());
//    }
//
//    private static Class getClass(String className, String packageName) {
//        try {
//            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
//        } catch (ClassNotFoundException e) {
//            // handle the exception
//        }
//        return null;
//    }
//}
