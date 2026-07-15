package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import java.nio.file.Path
import net.minecraft.resources.ResourceLocation

object HudScenePaths {
    private fun modeDirectoryName(mode: HudSceneMode): String = when (mode) {
        HudSceneMode.RIDE -> "ride"
        HudSceneMode.SPECTATE -> "spectate"
    }

    /** 지정한 모드와 상태 타입의 config override 파일 경로를 생성한다. */
    fun customHudScenePath(
        root: Path,
        mode: HudSceneMode,
        stateType: KartStateType<*>,
    ): Path {
        val modeDirectoryName = modeDirectoryName(mode)
        val stateFileName = "${stateType.id}.json"
        return root.resolve("hud").resolve(modeDirectoryName).resolve(stateFileName)
    }

    /** 지정한 모드와 상태 타입의 전용 리소스 장면 ID를 생성한다. */
    fun resourceHudSceneId(mode: HudSceneMode, stateType: KartStateType<*>): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "${modeDirectoryName(mode)}/${stateType.id}",
        )

    /** 상태 타입 전용 장면이 없을 때 사용할 모드별 기본 리소스 ID를 생성한다. */
    fun defaultResourceHudSceneId(mode: HudSceneMode): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "${modeDirectoryName(mode)}/default",
        )

    /** 상태 타입 전용 리소스 오류를 사용자에게 표시하기 위한 논리 경로를 생성한다. */
    fun resourceDisplayPath(mode: HudSceneMode, stateType: KartStateType<*>): Path =
        Path.of(
            "assets",
            ResourceStore.MOD_ID,
            "hud",
            modeDirectoryName(mode),
            "${stateType.id}.json",
        )

    /** 모드별 기본 리소스 오류를 사용자에게 표시하기 위한 논리 경로를 생성한다. */
    fun defaultResourceDisplayPath(mode: HudSceneMode): Path =
        Path.of(
            "assets",
            ResourceStore.MOD_ID,
            "hud",
            modeDirectoryName(mode),
            "default.json",
        )
}
