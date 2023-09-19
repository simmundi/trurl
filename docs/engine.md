# Engine

`Engine` is the object coordinating storing and managing:
- components (in turn, managed by `Mapper` instances)
- sessions (it uses a stored `SessionFactory`)
- listeners

It allows for running pieces of logic, called *systems*, but engine does not store or manage
`System` instances.

The list of components available and session configuration are **immutable** and
initialized at construction time. On the other hand, different objects can
require existence of certain components.

This is why `Engine` is not instantiated directly, but by `EngineConfiguration`.

## Objects that depend on language - HOWTO

No object should depend directly on `Engine` but rather on `EngineConfiguration`, which acts like a lazy loader and
allows all the objects to affect things like list of components mapped by the `Engine`.

Consequently, objects depending on `EngineConfiguration` shouldn't call `getEngine()` in their constructors,
because that would instantiate the `Engine` prematurely and  freeze its configuration.

## Objects that really need `Engine` as part of their setup

When `Engine` is **really** needed during object's setup, there are two ways:
- make the whole object lazy instantiated, never depend on it directly, but rather on its factory. `@BentoWrapper` can be used to implement such factory with minimum of fuss.
- defer object's setup - constructor calls `EngineConfiguration#addListener` to add a listener to perform the actual setup. The setup is guaranteed to be triggered before the engine is returned from the first `getEngine` call.

# Configuration

`Engine` and its parts require some setup, but the defaults might suffice. The configurable properties and their default values 
are as follows:

```properties
trurl.engine.store-factory=array
trurl.engine.component-accessor=dynamic
trurl.engine.initial-capacity=1024
trurl.engine.capacity-headroom=128
trurl.engine.shared-session=false
```
