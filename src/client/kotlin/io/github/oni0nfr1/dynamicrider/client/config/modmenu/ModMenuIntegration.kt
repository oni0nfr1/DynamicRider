package io.github.oni0nfr1.dynamicrider.client.config.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.oni0nfr1.dynamicrider.client.config.gui.DynRiderConfigMain
import net.minecraft.client.gui.screens.Screen

class ModMenuIntegration: ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { parent: Screen ->
            DynRiderConfigMain(parent)
        }
    }
}