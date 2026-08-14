package Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UiTheme {
    public static final Color MAIN_BACKGROUND = new Color(246,247,249);
    public static final Color CHAT_BACKGROUND = new Color(248,250,252);
    public static final Color HEADER_BACKGROUND = new Color(248,250,252);
    public static final Color SIDEBAR_BACKGROUND = new Color(32,42,51);
    public static final Color SIDEBAR_HOVER = new Color(43,55,66);
    public static final Color SIDEBAR_SELECTED = new Color(53,71,88);
    public static final Color PRIMARY_BLUE = new Color(59,130,246);
    public static final Color PRIMARY_BLUE_HOVER = new Color(37,99,235);
    public static final Color PRIMARY_BLUE_PRESSED = new Color(29,78,216);
    public static final Color PRIMARY_BLUE_LIGHT = new Color(219,234,254);
    public static final Color TEXT_PRIMARY = new Color(31,41,55);
    public static final Color TEXT_SECONDARY = new Color(100,116,139);
    public static final Color SENDER_TEXT = new Color(71,85,105);
    public static final Color SIDEBAR_TEXT = new Color(248,250,252);
    public static final Color SIDEBAR_MUTED = new Color(148,163,184);
    public static final Color BORDER_LIGHT = new Color(220,226,232);
    public static final Color BORDER_SUBTLE = new Color(226,232,240);
    public static final Color INPUT_BORDER = new Color(203,213,225);
    public static final Color INPUT_BACKGROUND = Color.WHITE;
    public static final Color INCOMING_MESSAGE = Color.WHITE;
    public static final Color OWN_MESSAGE = new Color(219,234,254);
    public static final Color OWN_MESSAGE_BORDER = new Color(191,219,254);
    public static final Color BUTTON_HOVER = new Color(241,245,249);
    public static final Color BUTTON_PRESSED = new Color(226,232,240);

    private static final String FONT_NAME = findFont();

    public static Font font(int style,int size){ return new Font(FONT_NAME,style,size); }

    public static ImageIcon icon(String name,int size,Color color){
        try{
            BufferedImage source = ImageIO.read(UiTheme.class.getResource("/icons/" + name));
            BufferedImage tinted = new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y = 0; y < source.getHeight(); y++){
                for(int x = 0; x < source.getWidth(); x++){
                    int alpha = (source.getRGB(x,y) >> 24) & 0xff;
                    tinted.setRGB(x,y,(alpha << 24) | (color.getRGB() & 0x00ffffff));
                }
            }
            Image scaled = tinted.getScaledInstance(size,size,Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }catch(Exception e){
            System.out.println("error loading icon " + name + ": " + e);
            return new ImageIcon();
        }
    }

    private static String findFont(){
        for(String name: GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()){
            if("Segoe UI".equalsIgnoreCase(name)) return name;
        }
        return Font.SANS_SERIF;
    }
}
