# Creating COM interface
## Introduction
Component Object Model consists of 2 type of IDs: Class ID (CLSID) and Interface ID (IID). When creating a new COM object, you must pass both CLSID and IID to `ole32.dll::CoCreateInstance`. `ole32.dll` will then find suitable COM server, create COM object from CLSID, check if that COM object is assignable to your desired interface with `QueryInterface`, then return that COM object back to you.

## When do I need IID?
You only need IID if you are going to create COM object from CLSID, or you are going to pass your Java object to COM that uses `QueryInterface` to check the implemented interface.

## Creating new interface
In reality, COM interfaces in COM4J are basically Java _abstract_ classes, not the kind of Java interfaces like you used to. If you are going to call native COM functions from Java, you probably don't need to provide default implementation, since it will be overriden by generated wrapper anyways.

Every COM objects must implements `IUnknown`. This interface contain functions for checking if your object is implementing specific interface, as well as memory management through the use of reference counting.

```java
public class IMyComObject extends IUnknown {
    public IMyComObject(MemorySegment comPtr, Runnable destroyCallback) {
        super(comPtr, destroyCallback);
        // You can hijack destroyCallback into calling your own destroy method,
        // but it MUST call the callback passed from ComFactory when destroying.
        // TODO: In the future, the constructor no longer takes in these 2 params
        // and instead get these 2 from Scoped values.
    }

    @ComMethod(index = 3)
    public abstract void myFunction();
}
```

From the interface above, `myFunction` is the function located at index 3 in virtual table. COM objects are basically C++ classes: each pointer to COM object points to a struct that holds a pointer to virtual table and a set of object attributes. The equivalent of that in C is like this:

```c
struct IMyComObject;
struct IMyComObjectVtbl;

typedef uint32_t IMyComObject_QueryInterface(IMyComObject *self, Guid guid, void **ppvObject);
typedef uint32_t IMyComObject_AddRef(IMyComObject *self);
typedef uint32_t IMyComObject_Release(IMyComObject *self);
typedef void IMyComObject_myFunction(IMyComObject *self);

struct IMyComObject {
    IMyComObjectVtbl *vtbl;
};

struct IMyComObjectVtbl {
    // Implementing IUnknown
    IMyComObject_QueryInterface *QueryInterface; // Index 0
    IMyComObject_AddRef         *AddRef;         // Index 1
    IMyComObject_Release        *Release;        // Index 2

    // Own functions
    IMyComObject_myFunction     *myFunction;     // Index 3
};
```

`IMyComObjectVtbl` can also be written as `void* functions[4]`, with index 0 to 3 pointing to function in `IUnknown` and index 4 pointing to `myFunction`. Because Java methods are not sorted in implementation order (you can declare method `a()` and method `b()`, yet there are JVM implementation that returns `b()` before `a()`), an annotation `@ComMethod` is required to identify which index in virtual table the method is corresponding to.

At this point, you can easily create new COM object from `ComFactory` by using `createFromClsid(Guid, Guid)`, and then wrap it as `IMyComObject` with `wrap(MemorySegment, Class)`:

```java
ComFactory com;

MemorySegment ptr = com.createFromClsid(iid, clsid);
IMyComObject obj = com.wrap(ptr, IMyComObject.class);
obj.myFunction();
obj.Release();
```

However, your `IMyComObject` is not ready to be passed to other software components yet, because the implementation of `QueryInterface` is not correct. See the section below this for more information.

## Adding IID to your interface
In case you want to interact with other software components through COM client, you must add `@ComInterface` with IID of the interface, or override `QueryInterface` with proper implementation. `QueryInterface` default implementation in `IUnknown` uses IID derived from `@ComInterface` of all superclasses (implmenting `IUnknown` directly or indirectly).

```java
@ComInterface("A81436D8-4757-4fd1-A185-133F97C6C545")
public abstract class IStylusPlugin { /* ... */ }
```

After that, you can easily create new Java object and wrap it as COM object at the same time like this:

```java
IStylusPlugin plugin = com.createFromClsid(IStylusPlugin.class, clsid);
```