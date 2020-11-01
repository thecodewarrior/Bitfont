package dev.thecodewarrior.bitfont.data.file

import java.lang.RuntimeException

class FileFormatException: RuntimeException {
    constructor(): super()
    constructor(message: String?): super(message)
    constructor(message: String?, cause: Throwable?): super(message, cause)
    constructor(cause: Throwable?): super(cause)
}