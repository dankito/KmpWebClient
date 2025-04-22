package net.dankito.web.client

enum class KtorEngine(val engineName: String) {

    CIO("CIO"),
    Curl("Curl"),
    Darwin("Darwin"),
    WinHttp("WinHttp"),
    Js("Js"),
    OkHttp("OkHttp"),
    Java("Java"),
    Apache("Apache"),
    Jetty("Jetty"),
    Android("Android")

    // Js does not seem to have a toString() implementation

}