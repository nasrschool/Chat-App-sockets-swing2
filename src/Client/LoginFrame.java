package Client;

import org.json.JSONObject;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField userIdField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JLabel errorLabel = new JLabel(" ");
    private JButton loginButton = new JButton("Login");

    public LoginFrame(){
        setTitle("Chat Login"); setSize(360,240); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLocationRelativeTo(null);
        JPanel form = new JPanel(); form.setBorder(BorderFactory.createEmptyBorder(25,35,25,35)); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.add(new JLabel("User ID")); form.add(userIdField); form.add(Box.createVerticalStrut(10));
        form.add(new JLabel("Password")); form.add(passwordField); form.add(Box.createVerticalStrut(8));
        errorLabel.setForeground(new java.awt.Color(180,45,45)); form.add(errorLabel); form.add(Box.createVerticalStrut(8)); form.add(loginButton); add(form);
        loginButton.addActionListener(e -> login()); passwordField.addActionListener(e -> login());
    }

    private void login(){
        int userId;
        try{ userId = Integer.parseInt(userIdField.getText().trim()); }
        catch(Exception e){ errorLabel.setText("Enter a numeric user ID"); return; }
        loginButton.setEnabled(false); errorLabel.setText("Connecting...");
        String password = new String(passwordField.getPassword());
        new Thread(() -> {
            try{
                JSONObject response = ClientLogic.login(userId,password);
                SwingUtilities.invokeLater(() -> {
                    if("ERROR".equals(response.optString("msgType"))){ errorLabel.setText(response.optString("content","Login failed")); loginButton.setEnabled(true); ClientLogic.close(); }
                    else{ ChatFrame frame = new ChatFrame(userId); ClientLogic.chatFrame = frame; dispose(); frame.setVisible(true); frame.loadConversations(); }
                });
            }catch(Exception e){ SwingUtilities.invokeLater(() -> { errorLabel.setText("Could not connect to the server"); loginButton.setEnabled(true); }); }
        }).start();
    }
}
