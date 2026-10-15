package com.neospawn.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.neospawn.NeoSpawn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Opens the Field Guide in the vanilla book screen, reading pages from assets/neospawn/books/field_guide.json. */
public final class FieldGuideScreen {
    public static void open() {
        List<Component> pages = new ArrayList<>();
        var resource = Minecraft.getInstance().getResourceManager().getResource(NeoSpawn.id("books/field_guide.json"));
        if (resource.isPresent()) {
            try (Reader reader = resource.get().openAsReader()) {
                JsonArray array = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("pages");
                array.forEach(page -> pages.add(Component.literal(page.getAsString())));
            } catch (Exception e) {
                NeoSpawn.LOGGER.error("Couldn't read the Field Guide", e);
            }
        }
        if (pages.isEmpty()) {
            pages.add(Component.literal("The Field Guide's pages are missing."));
        }
        Minecraft.getInstance().setScreen(new BookViewScreen(new BookViewScreen.BookAccess(pages)));
    }

    private FieldGuideScreen() {}
}
