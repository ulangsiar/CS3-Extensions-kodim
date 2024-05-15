version = 1

cloudstream {
    language = "ms"

    description = "PenCURI Official Website - Free Movie Download and Stream"
    authors = listOf("kodim")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 3 // will be 3 if unspecified
    tvTypes = listOf("TvSeries", "Movie")

    iconUrl = "https://www.google.com/s2/favicons?domain=https://ww79.pencurimovie.autos/&sz=%size%"
}