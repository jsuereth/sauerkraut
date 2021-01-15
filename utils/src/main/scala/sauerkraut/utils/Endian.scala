package sauerkraut
package utils

/** An enum representing where most-significant-bit is written in a byte sequence. */
enum Endian:
  /** Most significant byte is written first. */
  case Big
  /** Least significant byte is written first. */
  case Little