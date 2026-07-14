package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.ChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroDraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.SpeedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.V1KartState

open class PreviewKartState : KartState

open class PreviewSpeedKartState(
    override var speed: Double = 169.9,
) : PreviewKartState(), SpeedKartState

open class PreviewNitroKartState(
    speed: Double = 169.9,
    override var isDrifting: Boolean = false,
    override var isBoosting: Boolean = false,
    override var maxBoost: Int = 3,
    override var nitro: Int = 2,
    override var nitroGauge: Float = 0.65f,
    override var teamNitro: Int = 0,
    override var teamBoostGaugeAvailable: Boolean = false,
    override var teamBoostGauge: Float = 0f,
) : PreviewSpeedKartState(speed), NitroKartState

open class PreviewNitroDraftKartState(
    speed: Double = 169.9,
) : PreviewNitroKartState(speed), NitroDraftKartState {
    override var draftActive: Boolean = false
    override var draftCharging: Boolean = false
}

class PreviewJiuKartState : PreviewNitroDraftKartState(), JiuKartState

class PreviewChargeKartState : PreviewNitroDraftKartState(), ChargeKartState {
    override var chargerGauge: Float = 0.5f
}

class PreviewV1KartState : PreviewNitroDraftKartState(), V1KartState {
    override var exceedGauge: Float = 0.4f
}
