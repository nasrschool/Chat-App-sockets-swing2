package Client;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModernScrollBarUI extends BasicScrollBarUI {
    private Color background;

    public ModernScrollBarUI(){ this(UiTheme.CHAT_BACKGROUND); }
    public ModernScrollBarUI(Color background){ this.background = background; }

    protected void configureScrollBarColors(){
        trackColor = background;
        thumbColor = UiTheme.INPUT_BORDER;
        thumbHighlightColor = UiTheme.TEXT_SECONDARY;
        thumbDarkShadowColor = UiTheme.INPUT_BORDER;
        thumbLightShadowColor = UiTheme.INPUT_BORDER;
    }

    protected JButton createDecreaseButton(int orientation){ return emptyButton(); }
    protected JButton createIncreaseButton(int orientation){ return emptyButton(); }

    protected void paintThumb(Graphics graphics,JComponent component,Rectangle bounds){
        if(!scrollbar.isEnabled() || bounds.isEmpty()) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(isDragging || isThumbRollover() ? UiTheme.TEXT_SECONDARY : UiTheme.INPUT_BORDER);
        g.fillRoundRect(bounds.x + 3,bounds.y + 2,Math.max(4,bounds.width - 6),Math.max(4,bounds.height - 4),8,8);
        g.dispose();
    }

    private JButton emptyButton(){
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0,0));
        button.setMinimumSize(new Dimension(0,0));
        button.setMaximumSize(new Dimension(0,0));
        return button;
    }
}
