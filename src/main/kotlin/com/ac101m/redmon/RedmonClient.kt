package com.ac101m.redmon

import com.ac101m.redmon.persistence.SaveData
import com.ac101m.redmon.persistence.v1.SaveDataV1
import com.ac101m.redmon.utils.Config.Companion.PROFILE_REGISTRY_SAVE_PATH
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import java.nio.file.Path
import kotlin.io.path.exists


class RedmonClient : ClientModInitializer {
    private lateinit var saveData: SaveDataV1
    private var currentContext: String? = null

    private fun loadSaveData(path: Path) {
        if (path.exists()) {
            saveData = SaveData.load(path)
        } else {
            saveData = SaveDataV1()
            saveData.save(path)
        }
    }

    private fun drawOutput(matrixStack: MatrixStack) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow(
            matrixStack, "test", 0f, 0f, 0xffffff
        )
    }

    override fun onInitializeClient() {
        loadSaveData(PROFILE_REGISTRY_SAVE_PATH)

        HudRenderCallback.EVENT.register { matrixStack, _ ->
            if (currentContext != null) {
                drawOutput(matrixStack)
            }
        }
    }
}
