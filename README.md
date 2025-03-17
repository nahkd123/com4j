# COM4J
Interacting with Component Object Model from your Java application.

**[COM4J Documentations][com4j-docs]** | **~~Releases~~**

> [!WARNING]
> COM4J is not ready for production use. Use at your own risk.

## Introduction
Check out [Microsoft's own documentation][ms-com-intro] for introduction to Component Object Model.

This library aims to provide COM client functionality for your Java application by using just [Foreign Function and Memory API][java-ffi], which means this library have no C++ glue code (yet). It does not support creating COM server at this moment.

## Requirement
You must be using Java 23 or newer, since COM4J is using Foreign Memory and Function API. That's also the reason why this repository have zero C++ code!

## Quick example
### Example 1: Creating new COM object from CLSID
```java
ComFactory com = ComFactory.instance();

// This is CLSID of RealTimeStylus
// Find more of these with https://github.com/jkerai1/CLSID-Lookup
Guid rtsClsid = Guid.of("E26B366D-F998-43ce-836F-CB6D904432B0");
IUnknown rts = com.createFromClsid(IUnknown.class, rtsGuid);

// Since we don't know what to do with RTS (it's pretty useless without extending the interface), we just release it for now.
// Note that you MUST release IUnknown once you are done with it. Java GC will not do it for you.
rts.Release();
```

### Example 2: Creating new COM object from instantiating Java object
```java
// You must use public modifier for your interface and implementation for now
// If you want to create COM object from CLSID, you must provide IID through
// @ComInterface annotation.
// @ComInterface("IID-HERE")
public abstract class IMyComObject extends IUnknown {
	public MyComObject(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	// You must use public modifier for methods annotated with @ComMethod for now
	@ComMethod(index = 3)
	public abstract void coolMethod(int msg);
}

public class MyComObject extends IMyComObject {
	public MyComObject(MemorySegment comPtr, Runnable destroyCallback) {
		super(comPtr, destroyCallback);
	}

	@Override
	public void coolMethod(int msg) {
		System.out.println(msg);
	}
}

ComFactory com = ComFactory.instance();
IMyComObject obj = com.createJava(IMyComObject.class, MyComObject::new);
obj.coolMethod(42);

// Wrap from COM object pointer
IMyComObject wrapped = com.wrap(obj.getComPointer(), IMyComObject.class);
wrapped.coolMethod(727);
wrapped.Release();

// We don't call obj.Release() here because our COM to Java wrapper already
// released it.
```

> [!NOTE]
> You must always release COM object once you are done with it. Java GC will not garbage collect your COM objects.

## License
MIT License.

---
[com4j-docs]: ./docs/index.md
[ms-com-intro]: https://learn.microsoft.com/en-us/windows/win32/com/component-object-model--com--portal
[java-ffi]: https://dev.java/learn/ffm/