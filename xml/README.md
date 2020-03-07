# XML Format

This format was put together mostly to prove that we could.  It is a prototype, and highly undesirable for many reasons.

The current format looks something of this sort, for `Foo(name="Josh", age=13)`, you would get:

```
<structure>
  <field name="name">
    <primitive>Josh</primitive>
  </field>
  <field name="age>
    <primitive>13</primitive>
  </field>
</structure>
```

As you can see, this format COULD be optimised a lot.

# TODOS

- [ ] Optimise/minimize the output format.
- [ ] Define a DTD/XSD for our serialization format.
- [ ] See if there's ANYTHING remotely efficient in parsing (SAX seems slow)
- [ ] Look into encoding Scala types as URIs/schema directly.