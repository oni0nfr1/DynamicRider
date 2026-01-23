package io.github.oni0nfr1.dynamicrider.client.hud

object VanillaSuppression {

    var suppressionEnabled: Boolean = true

    @JvmStatic var suppressVanillaKartState = true
        get() = field && suppressionEnabled
    @JvmStatic var suppressVanillaSidebarRanking = true
        get() = field && suppressionEnabled

}