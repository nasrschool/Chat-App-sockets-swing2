package Client;

import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField userIdField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JLabel errorLabel = new JLabel(" ");
    private JButton loginButton = new RoundedButton("Login",UiTheme.PRIMARY_BLUE,UiTheme.PRIMARY_BLUE_HOVER,UiTheme.PRIMARY_BLUE_PRESSED,null);

    public LoginFrame(){
        setTitle("Chat Login"); setSize(380,270); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLocationRelativeTo(null);
        getContentPane().setBackground(UiTheme.MAIN_BACKGROUND);
        JPanel form = new JPanel(); form.setBackground(UiTheme.MAIN_BACKGROUND); form.setBorder(BorderFactory.createEmptyBorder(28,38,28,38)); form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        JLabel userLabel = new JLabel("User ID"), passwordLabel = new JLabel("Password");
        styleLabel(userLabel); styleLabel(passwordLabel); styleField(userIdField); styleField(passwordField);
        form.add(userLabel); form.add(Box.createVerticalStrut(6)); form.add(userIdField); form.add(Box.createVerticalStrut(12));
        form.add(passwordLabel); form.add(Box.createVerticalStrut(6)); form.add(passwordField); form.add(Box.createVerticalStrut(8));
        errorLabel.setForeground(new java.awt.Color(180,45,45)); form.add(errorLabel); form.add(Box.createVerticalStrut(8)); form.add(loginButton); add(form);
        loginButton.setFont(UiTheme.font(Font.BOLD,14)); loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10,22,10,22)); loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> login()); passwordField.addActionListener(e -> login());
    }

    private void styleLabel(JLabel label){ label.setFont(UiTheme.font(Font.BOLD,13)); label.setForeground(UiTheme.TEXT_PRIMARY); label.setAlignmentX(Component.LEFT_ALIGNMENT); }
    private void styleField(JTextField field){
        field.setFont(UiTheme.font(Font.PLAIN,14)); field.setForeground(UiTheme.TEXT_PRIMARY); field.setBackground(UiTheme.INPUT_BACKGROUND);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.INPUT_BORDER,1,true),BorderFactory.createEmptyBorder(8,10,8,10)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,38)); field.setAlignmentX(Component.LEFT_ALIGNMENT);
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
