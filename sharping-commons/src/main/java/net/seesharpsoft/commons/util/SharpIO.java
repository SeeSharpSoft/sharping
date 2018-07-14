package net.seesharpsoft.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class SharpIO {

    private SharpIO() {
        // static
    }

    public static void readFromIn2Out(InputStream in, OutputStream out) throws IOException {
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            out.write(data, 0, nRead);
        }
        out.flush();
    }

    public static byte[] readAsByteArray(InputStream stream) throws IOException {
        try(ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            readFromIn2Out(stream, buffer);
            return buffer.toByteArray();
        }
    }

    public static String readAsString(InputStream stream) throws IOException {
        return readAsString(stream, Charset.defaultCharset());
    }

    public static String readAsString(InputStream stream, Charset charset) throws IOException {
        try(ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            readFromIn2Out(stream, buffer);
            return buffer.toString(charset.name());
        }
    }

    public static InputStream createInputStream(String fileName, boolean isResource) throws IOException {
        if (isResource) {
            return SharpIO.class.getResourceAsStream(fileName);
        }
        return Files.newInputStream(Paths.get(fileName));
    }

    public static InputStream createInputStream(String fileName) throws IOException {
        return createInputStream(fileName, false);
    }

    public static String readAsString(String fileName, Charset charset) throws IOException {
        try(InputStream fileInputStream = createInputStream(fileName)) {
            return readAsString(fileInputStream, charset);
        }
    }

    public static String readAsString(String fileName) throws IOException {
        return readAsString(fileName, Charset.defaultCharset());
    }

    public static Byte[] byteArrayToObjects(byte[] primitiveByteArray) {
        Byte[] objectByteArray = new Byte[primitiveByteArray.length];
        Arrays.setAll(objectByteArray, n -> primitiveByteArray[n]);
        return objectByteArray;
    }

    public static byte[] byteArrayToPrimitives(Byte[] objectByteArray)
    {
        byte[] primitiveByteArray = new byte[objectByteArray.length];
        for(int i = 0; i < objectByteArray.length; i++) {
            primitiveByteArray[i] = objectByteArray[i];
        }
        return primitiveByteArray;
    }

    public static Stream<Byte> bytesToStream(byte[] array) {
        return Arrays.stream(byteArrayToObjects(array));
    }

    public static Collector<Number, ?, byte[]> toByteArray() {
        return Collector.of(
                () -> new ArrayList<Byte>(),
                (arrayList, value) -> {
                    arrayList.add(value == null ? Byte.valueOf((byte)0) : Byte.valueOf(value.byteValue()));
                },
                (list1, list2) -> {
                    Collections.copy(list1, list2);
                    return list1;
                },
                arrayList -> byteArrayToPrimitives(arrayList.toArray(new Byte[arrayList.size()]))
        );
    }

    public static <A, T> Collector<A, ?, T[]> toArray(Class<T> clazz) {
        return Collector.of(
                () -> new ArrayList<>(),
                (arrayList, value) -> {
                    arrayList.add(value);
                },
                (list1, list2) -> {
                    Collections.copy(list1, list2);
                    return list1;
                },
                arrayList -> arrayList.toArray((T[])Array.newInstance(clazz, arrayList.size()))
        );
    }
}
