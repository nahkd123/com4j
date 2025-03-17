# Value conversion
## Introduction
COM4J supports converting from foreign types (things like `int`, `long` or `MemorySegment`) to your own Java objects and vice versa.

## TODO
This page is TODO. Check out example from `Guid` and `HResult`. Key annotations to consider:

- `@ForeignConvertible`
- `@ForeignConvertibleLayout` (for `MemorySegment` only)
- `@ConvertFromForeign`
- `@ConvertToForeign`