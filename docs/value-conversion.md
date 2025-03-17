# Value conversion
## Introduction
COM4J supports converting from foreign types (things like `int`, `long` or `MemorySegment`) to your own Java objects and vice versa.

## Default conversion
### Primitive types
Primitive types are unchanged when converting to foreign value.

| Java primitive  | Equivalent in C    |
|:----------------|-------------------:|
| `byte`          | `int8_t`           |
| `short`         | `int16_t`          |
| `int`           | `int32_t`          |
| `long`          | `int64_t`          |
| `float`         | `float` (32-bit)   |
| `double`        | `double` (64-bit)  |
| `char`          | `wchar_t` (16-bit) |
| `MemorySegment` | `void*`            |

### COM4J types
These are types declared in COM4J Base Library for convenience use. You can use them directly in your COM interface declaration and COM4J will automatically convert to/from foreign value.

| COM4J class | Win32/COM type       |
|-------------|---------------------:|
| `HResult`   | `HRESULT`/`int32_t`  |
| `Guid`      | `GUID`/`IID`/`CLSID` |

> [!NOTE]
> All of these are annotated with `@ForeignConvertible`, which you can also use to make your own type. Also, these are struct, rather than pointer to a struct, so if you want to use pointer, you must use `MemorySegment`.

## Custom conversion
If you want COM4J automatically convert your Java object into struct, you can annotate 
your class with `@ForeignConvertible`

### Primitives
You can let COM4J automatically convert from foreign primitive into Java object by using primitive class in `@ForeignConvertible`. See `HResult` for example on how to convert from `int32_t` to `HResult`. Simply annotate constructor/static method with `@ConvertFromForeign` that takes in the foreign type and method that returns foreign type with `@ConvertToForeign`.

For example, to convert `int16_t` into `Version(int major, int minor)`:

```java
@ForeignConvertible(short.class)
public record Version(int major, int minor) {

    @ConvertFromForeign
    public static Version ofForeign(short v) {
        int major = (v & 0xFF00) >> 8;
        int minor = v & 0x00FF;
        return new Version(major, minor);
    }

    @ConvertToForeign
    public short toForeign() {
        return (short) ((major << 8) | minor);
    }

}
```

### Struct type
In order to create converter for converting from struct type to your Java object, use `MemorySegment.class` as value for `@ForeignConvertible`, then annotate `@ForeignConvertibleLayout` to `public static final MemoryLayout` field inside that class for layout of the struct. For example, if my struct is `struct MyStruct { int32_t a; int16_t b; int16_t c; };`, you will have to create something like this:

```java
@ForeignConvertible(MemorySegment.class)
public record MyStruct(int a, short b, short c) {

    @ForeignConvertibleLayout
    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        ValueLayout.JAVA_INT,
        ValueLayout.JAVA_SHORT,
        ValueLayout.JAVA_SHORT
    );

    @ConvertFromForeign
    public static MyStruct of(MemorySegment memory) { /* ... */ }

    // `arena` is optional
    @ConvertToForeign
    public MemorySegment toForeign(Arena arena) { /* ... */ }

}
```