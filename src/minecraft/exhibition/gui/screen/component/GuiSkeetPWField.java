/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.gui.screen.component;

import exhibition.gui.altmanager.PasswordField;
import exhibition.util.RenderingUtil;
import exhibition.util.render.Colors;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public class GuiSkeetPWField extends PasswordField {

    public GuiSkeetPWField(FontRenderer par1FontRenderer, int par2, int par3, int par4, int par5) {
        super(par1FontRenderer, par2, par3, par4, par5);
    }

    public void drawTextBox() {
        if (this.func_73778_q()) {
            int var1 = this.isEnabled ? Colors.getColor(148,184,4) : this.disabledColor;
            int var2 = this.cursorPosition - this.field_73816_n;
            int var3 = this.selectionEnd - this.field_73816_n;
            String var4 = this.fontRenderer.trimStringToWidth(this.text.substring(this.field_73816_n).replaceAll("(?s).", "*"), this.getWidth());
            boolean var5 = var2 >= 0 && var2 <= var4.length();
            boolean var6 = this.isFocused && this.cursorCounter / 6 % 2 == 0 && var5;
            int var7 = this.xPos + 4;
            float var8 = this.yPos + (this.height - 8) / 2F - 0.5F;
            int var9 = var7;

            if (var3 > var4.length())
            {
                var3 = var4.length();
            }

            if (var4.length() > 0)
            {
                String var10 = var5 ? var4.substring(0, var2) : var4;
                RenderingUtil.drawOutlinedString(var10, var7, (float)var8, Colors.getColor(0));
                var9 = this.fontRenderer.drawString(var10, var7, (float)var8, var1);
            }

            boolean var13 = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int var11 = var9;

            if (!var5)
            {
                var11 = var2 > 0 ? var7 + this.width : var7;
            }
            else if (var13)
            {
                var11 = var9 - 1;
            }

            if (var4.length() > 0 && var5 && var2 < var4.length())
            {
                RenderingUtil.drawOutlinedString(var4.substring(var2).replaceAll("(?s).", "*"), (float)var9, (float)var8, Colors.getColor(0));
                var9 = this.fontRenderer.drawString(var4.substring(var2).replaceAll("(?s).", "*"), (float)var9, (float)var8, var1);
            }

            if (var6)
            {
                if (var13)
                {
                    Gui.drawRect(var11, var8 - 1, var11 + 1, var8 + 1 + this.fontRenderer.FONT_HEIGHT, -3092272);
                }
                else
                {
                    RenderingUtil.drawOutlinedString("_", var11, (float)var8, Colors.getColor(0));
                    this.fontRenderer.drawString("_", var11, (float)var8, var1);
                }
            }

            if (var3 != var2)
            {
                int var12 = var7 + this.fontRenderer.getStringWidth(var4.substring(0, var3));
                this.drawCursorVertical(var11, (int)var8 - 1, var12 - 1, (int)var8 + 1 + this.fontRenderer.FONT_HEIGHT);
            }
        }
    }
}
