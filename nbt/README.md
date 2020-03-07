# Named Binary Tag Format

This is a binary format that is very similar to JSON with a few differences.  You can see a specification [here](https://minecraft.gamepedia.com/NBT_format).

Currently, the implementation for this format is highly naive.


# TODOs

- [ ] Attempt to optimise reading/write with some new class vs. `DataInputStream`/`DataOutputStream`.
- [ ] See if we can read anything minecraft-y.
- [ ] Add GZip compression option to input/output