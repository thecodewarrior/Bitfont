package dev.thecodewarrior.bitfont.fonteditor

import dev.thecodewarrior.bitfont.fonteditor.utils.DrawList
import org.lwjgl.nuklear.NkContext
import org.lwjgl.nuklear.NkRect
import org.lwjgl.nuklear.Nuklear.*
import org.lwjgl.system.MemoryStack
import java.awt.Color

class NuklearFontWindow: Window(260f, 530f, false) {
    override fun pushContents(ctx: NkContext) {
        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("Light", 18f).userFont)
        nk_label(ctx, "Sans Light: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("Regular", 18f).userFont)
        nk_label(ctx, "Sans Regular: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("Medium", 18f).userFont)
        nk_label(ctx, "Sans Medium: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("Bold", 18f).userFont)
        nk_label(ctx, "Sans Bold: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("Black", 18f).userFont)
        nk_label(ctx, "Sans Black: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("CondensedLight", 18f).userFont)
        nk_label(ctx, "Sans Condensed Light: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("Condensed", 18f).userFont)
        nk_label(ctx, "Sans Condensed: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("CondensedMedium", 18f).userFont)
        nk_label(ctx, "Sans Condensed Medium: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("CondensedBold", 18f).userFont)
        nk_label(ctx, "Sans Condensed Bold: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSans("CondensedBlack", 18f).userFont)
        nk_label(ctx, "Sans Condensed Black: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("Light", 18f).userFont)
        nk_label(ctx, "Serif Light: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("Regular", 18f).userFont)
        nk_label(ctx, "Serif Regular: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("Medium", 18f).userFont)
        nk_label(ctx, "Serif Medium: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("Bold", 18f).userFont)
        nk_label(ctx, "Serif Bold: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("Black", 18f).userFont)
        nk_label(ctx, "Serif Black: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("CondensedLight", 18f).userFont)
        nk_label(ctx, "Serif Condensed Light: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("Condensed", 18f).userFont)
        nk_label(ctx, "Serif Condensed: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("CondensedMedium", 18f).userFont)
        nk_label(ctx, "Serif Condensed Medium: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("CondensedBold", 18f).userFont)
        nk_label(ctx, "Serif Condensed Bold: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)

        nk_layout_row_dynamic(ctx, 20f, 1)
        nk_style_push_font(ctx, NuklearFonts.getSerif("CondensedBlack", 18f).userFont)
        nk_label(ctx, "Serif Condensed Black: Lorem ipsum", NK_TEXT_LEFT)
        nk_style_pop_font(ctx)
    }

    override fun free() {
    }
}