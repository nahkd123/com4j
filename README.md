# COM4J
Interacting with Component Object Model from your Java application.

> [!WARNING]
> Please note that COM4J is not ready for production use. Use at your own risk.

## Requirement
You must be using Java 23 or newer, since COM4J is using Foreign Memory and Function API. That's also the reason why this repository have zero C++ code!

## Quick example
```java
ComFactory com = ComFactory.instance();

// Create IRealTimeStylus
IUnknown rts = com.createFromClsid(IUnknown.class, Guid.of("E26B366D-F998-43ce-836F-CB6D904432B0"));

// If we save IUnknown to, let's say, field for example, we must increase reference counter
rts.AddRef();
field = rts;

// And when we unset the field, we have to release the reference
field.Release();
field = null;

// Free it one more time because ref counter was increased when we use createFromClsid()
rts.Release();

// Passing Java object to COM
IUnknown obj = com.createJava(IUnknown.class, (ptr, destroyCb) -> new IUnknown(ptr, destroyCb) {
	@Override
	public int AddRef() {
		System.out.println("AddRef() called");
		return IUnknown.super.AddRef();
	}
});

// Pass this to somewhere
obj.getComPointer(); // => java.lang.foreign.MemorySegment

// And don't forget to free reference!
obj.Release();
```

## License
MIT License.
