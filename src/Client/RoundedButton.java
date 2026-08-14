package Client;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color borderColor;

    public RoundedButton(String text,Color normalColor,Color hoverColor,Color pressedColor,Color borderColor){
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.pressedColor = pressedColor;
        this.borderColor = borderColor;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
    }

    protected void paintComponent(Graphics graphics){
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = getModel().isPressed() ? pressedColor : getModel().isRollover() ? hoverColor : normalColor;
        g.setColor(color);
        g.fillRoundRect(0,0,getWidth() - 1,getHeight() - 1,10,10);
        if(borderColor != null){
            g.setColor(borderColor);
            g.drawRoundRect(0,0,getWidth() - 1,getHeight() - 1,10,10);
        }
        g.dispose();
        super.paintComponent(graphics);
    }
}
