// use an integer for version numbers
version = 22


cloudstream {
    language = "id"
    // All of these properties are optional, you can safely remove them

    description = "Lk21 Nonton Streaming Download Film Box Office Series Drama Korea Mandarin Thailand Sub Indo Dunia21 Terbaru dan Terlengkap HD Bluray WEBDL Layarkaca21 Dunia21"
    
    authors = listOf("kodim")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "AsianDrama",
        "TvSeries",
        "Movie",
    )


    iconUrl = "https://s8.lk21static.xyz/wp-content/themes/dunia21/images/favicon-set/apple-icon-144x144.png"

}