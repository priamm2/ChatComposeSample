package com.example.chatcomposesample.result

public sealed class Error {

  public abstract val message: String

  /**
   * An error that only contains the message.
   *
   * @param message The message describing the error.
   */
  public data class GenericError(override val message: String) : Error()

  /**
   * An error that contains a message and cause.
   *
   * @param message The message describing the error.
   * @param cause The [Throwable] associated with the error.
   */
  public data class ThrowableError(override val message: String, public val cause: Throwable) :
    Error() {

    @StreamHandsOff(
      "Throwable doesn't override the equals method;" +
        " therefore, it needs custom implementation."
    )
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      return (other as? Error)?.let {
        message == it.message && cause.equalCause(it.extractCause())
      } ?: false
    }

    private fun Throwable?.equalCause(other: Throwable?): Boolean {
      if ((this == null && other == null) || this === other) return true
      return this?.message == other?.message && this?.cause.equalCause(other?.cause)
    }

    @StreamHandsOff(
      "Throwable doesn't override the hashCode method;" +
        " therefore, it needs custom implementation."
    )
    override fun hashCode(): Int {
      return 31 * message.hashCode() + cause.hashCode()
    }
  }

  /**
   * An error resulting from the network operation.
   *
   * @param message The message describing the error.
   * @param serverErrorCode The error code returned by the backend.
   * @param statusCode HTTP status code or [UNKNOWN_STATUS_CODE] if not available.
   * @param cause The optional [Throwable] associated with the error.
   */
  public data class NetworkError(
    override val message: String,
    public val serverErrorCode: Int,
    public val statusCode: Int = UNKNOWN_STATUS_CODE,
    public val cause: Throwable? = null
  ) : Error() {

    @StreamHandsOff(
      "Throwable doesn't override the equals method;" +
        " therefore, it needs custom implementation."
    )
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      return (other as? Error)?.let {
        message == it.message && cause.equalCause(it.extractCause())
      } ?: false
    }

    private fun Throwable?.equalCause(other: Throwable?): Boolean {
      if ((this == null && other == null) || this === other) return true
      return this?.message == other?.message && this?.cause.equalCause(other?.cause)
    }

    @StreamHandsOff(
      "Throwable doesn't override the hashCode method;" +
        " therefore, it needs custom implementation."
    )
    override fun hashCode(): Int {
      return 31 * message.hashCode() + (cause?.hashCode() ?: 0)
    }

    public companion object {
      public const val UNKNOWN_STATUS_CODE: Int = -1
    }
  }
}

/**
 * Copies the original [Error] objects with custom message.
 *
 * @param message The message to replace.
 *
 * @return New [Error] instance.
 */
public fun Error.copyWithMessage(message: String): Error {
  return when (this) {
    is Error.GenericError -> this.copy(message = message)
    is Error.NetworkError -> this.copy(message = message)
    is Error.ThrowableError -> this.copy(message = message)
  }
}

/**
 * Extracts the cause from [Error] object or null if it's not available.
 *
 * @return The [Throwable] that is the error's cause or null if not available.
 */
public fun Error.extractCause(): Throwable? {
  return when (this) {
    is Error.GenericError -> null
    is Error.NetworkError -> cause
    is Error.ThrowableError -> cause
  }
}