package net.dankito.web.client

enum class ClientErrorType {
    NetworkError,
    Timeout,
    ClientError,
    ServerError,
    DeserializationError,
    Unknown
}