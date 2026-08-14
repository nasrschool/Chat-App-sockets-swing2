package Client;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private Color borderColor;
    private int radius;

    public RoundedPanel(Color background,Color borderColor,int radius){
        this.borderColor = borderColor;
        this.radius = radius;
        setBackground(background);
        setOpaque(false);
    }

    protected void paintComponent(Graphics graphics){
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getBackground());
        g.fillRoundRect(0,0,getWidth() - 1,getHeight() - 1,radius,radius);
        if(borderColor != null){
            g.setColor(borderColor);
            g.drawRoundRect(0,0,getWidth() - 1,getHeight() - 1,radius,radius);
        }
        g.dispose();
        super.paintComponent(graphics);
    }
}
