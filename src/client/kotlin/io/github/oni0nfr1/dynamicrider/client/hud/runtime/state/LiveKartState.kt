package io.github.oni0nfr1.dynamicrider.client.hud.runtime.state

import io.github.oni0nfr1.dynamicrider.client.hud.state.ChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.DraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.DraftSpeedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroDraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.SpeedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.V1KartState
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.inventory.KartTeamBoostCounter
import io.github.oni0nfr1.skid.client.api.engine.ChargeEngine
import io.github.oni0nfr1.skid.client.api.engine.DraftEngine
import io.github.oni0nfr1.skid.client.api.engine.JiuEngine
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.engine.SpeedEngine
import io.github.oni0nfr1.skid.client.api.engine.V1Engine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

open class LiveKartState<E : KartEngine>(
    protected val kart: KartRef.Specific<E>,
) : KartState

open class LiveSpeedKartState<E : SpeedEngine>(
    kart: KartRef.Specific<E>,
) : LiveKartState<E>(kart), SpeedKartState {
    override val speed: Double
        get() = kart.accessEngine { it.tachometer?.speed } ?: 0.0
}

open class LiveNitroKartState<E : NitroEngine>(
    kart: KartRef.Specific<E>,
) : LiveSpeedKartState<E>(kart), NitroKartState {
    override val isDrifting: Boolean
        get() = kart.accessEngine { it.isDrifting } ?: false
    override val isBoosting: Boolean
        get() = kart.accessEngine { it.isBoosting } ?: false
    override val maxBoost: Int
        get() = kart.accessEngine { it.maxBoost } ?: 2
    override val nitro: Int
        get() = kart.accessEngine { it.tachometer?.nitro } ?: 0
    override val nitroGauge: Float
        get() = kart.accessEngine { it.tachometer?.gauge?.toFloat() } ?: 0f
    override val teamNitro: Int
        get() = KartTeamBoostCounter.boostCount
    override val teamBoostGaugeAvailable: Boolean
        get() = KartTeamBoostTracker.gaugeExists
    override val teamBoostGauge: Float
        get() = if (teamBoostGaugeAvailable) KartTeamBoostTracker.gauge else 0f
}

open class LiveNitroDraftKartState<E>(
    kart: KartRef.Specific<E>,
) : LiveNitroKartState<E>(kart), NitroDraftKartState
    where E : NitroEngine, E : DraftEngine {
    override val draftActive: Boolean
        get() = kart.accessEngine { it.draftActive } ?: false
    override val draftCharging: Boolean
        get() = kart.accessEngine { it.draftCharging } ?: false
}

class LiveJiuKartState(
    kart: KartRef.Specific<JiuEngine>,
) : LiveNitroDraftKartState<JiuEngine>(kart), JiuKartState

class LiveChargeKartState(
    kart: KartRef.Specific<ChargeEngine>,
) : LiveNitroDraftKartState<ChargeEngine>(kart), ChargeKartState {
    override val chargerGauge: Float
        get() = kart.accessEngine { it.tachometer?.chargerGauge } ?: 0f
}

class LiveV1KartState(
    kart: KartRef.Specific<V1Engine>,
) : LiveNitroDraftKartState<V1Engine>(kart), V1KartState {
    override val exceedGauge: Float
        get() = kart.accessEngine { it.tachometer?.exceedGauge?.div(0.9851485f) } ?: 0f
}

class LiveDraftSpeedKartState<E>(
    kart: KartRef.Specific<E>,
) : LiveSpeedKartState<E>(kart), DraftSpeedKartState
    where E : SpeedEngine, E : DraftEngine {
    override val draftActive: Boolean
        get() = kart.accessEngine { it.draftActive } ?: false
    override val draftCharging: Boolean
        get() = kart.accessEngine { it.draftCharging } ?: false
}

class LiveDraftKartState<E>(
    kart: KartRef.Specific<E>,
) : LiveKartState<E>(kart), DraftKartState
    where E : KartEngine, E : DraftEngine {
    override val draftActive: Boolean
        get() = kart.accessEngine { it.draftActive } ?: false
    override val draftCharging: Boolean
        get() = kart.accessEngine { it.draftCharging } ?: false
}
