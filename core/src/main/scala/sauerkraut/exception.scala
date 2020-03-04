package sauerkraut


/** An exception during pickling/unpickling. */
class SauerkrautException(msg: String, cause: Throwable)
  extends RuntimeException(msg, cause)

class BuildException(msg: String, cause: Throwable)
  extends SauerkrautException(msg, cause)

class WriteException(msg: String, cause: Throwable)
  extends SauerkrautException(msg, cause)