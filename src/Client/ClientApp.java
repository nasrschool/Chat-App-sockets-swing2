package Client;

import javax.swing.SwingUtilities;

public class ClientApp {
    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true)); }
}
