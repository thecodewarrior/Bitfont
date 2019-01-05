package games.thecodewarrior.bitfont.stream

class ReadException: RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}

enum class IndexSize { BYTE, SHORT, INT }
