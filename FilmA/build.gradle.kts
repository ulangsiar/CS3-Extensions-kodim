
// use an integer for version numbers
version = 2

cloudstream {
    // All of these properties are optional, you can safely remove them
    language = "id"
    // description = "Nonton Film Online... Bioskop Online Terlengkap"
    description = "Test for Coding"
    authors = listOf("kodim")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 3

    tvTypes = listOf("TvSeries","Movie")

    iconUrl = "https://filmapik.ngo/wp-content/uploads/2021/11/newfa.png"
}

