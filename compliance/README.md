# Compliance Testing

Sauerkraut defines two levels of compliance:
- Baseline compliance for handling serializing any type that has a `Reader`/`Buildable`
  pair.
- Evolution compliance, where specific refactorings to classes are allowed.


## Baselines Compliance

Baselines compliance attempts to ensure that all types that could be expressed using the `Reader`/`Buildable`
abstractions are supported for the same binary reading/writing data.   I.e. it only guarantees that the SAME
code can read and write values.


| *format*     | RawBinary | Protos | json | nbt | xml |
| *compliant*  | yes       | no     | yes  | yes | yes |

A format can show compliance by implementing `saurkraut.format.testing.ComplianceTests` and ensuring everything passes.

## Evolution Compliance

Evolution compliance attempts to ensure that developers can evolve APIs in specific ways, allowing code to change but
still reading previously recorded values (sometimes with data loss).   The specific types of evoluation are
called out and formats can opt-in to any of them, with an apporpaite test.


| evolution                    | description                                                           | Supporting formats |
| ---------------------------- | --------------------------------------------------------------------- | ------------------ |
| `FieldToCollectionEvolution` | Developers can alter the type of a field from `T` to `Collection[T]`. | json               |