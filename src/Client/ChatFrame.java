package Client;

import Tools.MsgTypes;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class ChatFrame extends JFrame {
    private final int userId;
    private int selectedGroupId = -1;
    private boolean selectedIsPrivate;
    private JPanel directPanel = new JPanel(), groupPanel = new JPanel(), messagesPanel = new JPanel();
    private JScrollPane messagesScroll;
    private JLabel conversationName = new JLabel("Select a conversation");
    private JTextField messageField = new JTextField();
    private HashMap<Integer,JPanel> conversationItems = new HashMap<>();
    private final Color normal = new Color(48,58,66), hover = new Color(65,76,84), selected = new Color(82,96,104);

    public ChatFrame(int userId){
        this.userId = userId;
        setTitle("Chat - User " + userId); setSize(900,620); setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); setLayout(new BorderLayout());
        add(createSidebar(),BorderLayout.WEST); add(createChatPanel(),BorderLayout.CENTER);
        addWindowListener(new WindowAdapter(){ public void windowClosed(WindowEvent e){ ClientLogic.close(); } });
    }

    private JPanel createSidebar(){
        JPanel sidebar = new JPanel(new BorderLayout()); sidebar.setPreferredSize(new Dimension(260,0)); sidebar.setBackground(normal);
        JPanel buttons = new JPanel(new GridLayout(1,2,6,0)); JButton direct = new JButton("+ Direct"), group = new JButton("+ Group");
        direct.addActionListener(e -> startDirect()); group.addActionListener(e -> createGroup()); buttons.add(direct); buttons.add(group);
        buttons.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); sidebar.add(buttons,BorderLayout.NORTH);
        JPanel lists = new JPanel(); lists.setBackground(normal); lists.setLayout(new BoxLayout(lists,BoxLayout.Y_AXIS));
        lists.add(sectionLabel("DIRECT")); setupList(directPanel); lists.add(directPanel);
        lists.add(sectionLabel("GROUPS")); setupList(groupPanel); lists.add(groupPanel);
        sidebar.add(new JScrollPane(lists),BorderLayout.CENTER); return sidebar;
    }

    private JLabel sectionLabel(String text){
        JLabel label = new JLabel(text); label.setForeground(new Color(190,200,205));
        label.setBorder(BorderFactory.createEmptyBorder(14,14,6,8)); label.setAlignmentX(Component.LEFT_ALIGNMENT); return label;
    }
    private void setupList(JPanel panel){ panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(normal); panel.setAlignmentX(Component.LEFT_ALIGNMENT); }

    private JPanel createChatPanel(){
        JPanel chat = new JPanel(new BorderLayout()); conversationName.setFont(conversationName.getFont().deriveFont(Font.BOLD,18f));
        conversationName.setBorder(BorderFactory.createEmptyBorder(14,16,14,16)); chat.add(conversationName,BorderLayout.NORTH);
        messagesPanel.setLayout(new BoxLayout(messagesPanel,BoxLayout.Y_AXIS)); messagesPanel.setBackground(new Color(242,239,232));
        messagesScroll = new JScrollPane(messagesPanel); chat.add(messagesScroll,BorderLayout.CENTER);
        JPanel compose = new JPanel(new BorderLayout(8,0)); JButton send = new JButton("Send");
        send.addActionListener(e -> sendMessage()); messageField.addActionListener(e -> sendMessage());
        compose.add(messageField,BorderLayout.CENTER); compose.add(send,BorderLayout.EAST); compose.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        chat.add(compose,BorderLayout.SOUTH); return chat;
    }

    public void loadConversations(){ send(MsgTypes.GET_ALL_GROUP_DATA,new JSONObject()); }
    private void send(MsgTypes type,JSONObject msg){ msg.put("msgType",type); msg.put("msgSource",userId); ClientLogic.send(msg); }

    public void receiveMessage(JSONObject msg){
        String type = msg.optString("msgType");
        if("ERROR".equals(type)) JOptionPane.showMessageDialog(this,msg.optString("content","Operation failed"));
        else if("GET_ALL_GROUP_DATA".equals(type)) renderConversations(msg.optJSONArray("content"));
        else if("GET_ALL_GROUP_CHAT".equals(type) && msg.optInt("group_id") == selectedGroupId) renderHistory(msg.optJSONArray("msgs"));
        else if("SEND_MSG".equals(type) && msg.optInt("group_id") == selectedGroupId) appendMessage(msg);
        else if("INVITE_TO_DM".equals(type) || "MAKE_GROUP".equals(type)) loadConversations();
    }

    private void renderConversations(JSONArray conversations){
        directPanel.removeAll(); groupPanel.removeAll(); conversationItems.clear();
        if(conversations != null) for(int i = 0; i < conversations.length(); i++){
            JSONObject conversation = conversations.getJSONObject(i); boolean isPrivate = conversation.getBoolean("is_private");
            String name = isPrivate ? directName(conversation.getJSONArray("users_id")) : conversation.optString("group_name","Group " + conversation.getInt("group_id"));
            JPanel item = conversationItem(conversation.getInt("group_id"),name,isPrivate);
            if(isPrivate) directPanel.add(item); else groupPanel.add(item);
        }
        directPanel.revalidate(); groupPanel.revalidate(); directPanel.repaint(); groupPanel.repaint();
    }

    private String directName(JSONArray users){
        for(int i = 0; i < users.length(); i++) if(users.getInt(i) != userId) return "User " + users.getInt(i);
        return "Direct conversation";
    }

    private JPanel conversationItem(int groupId,String name,boolean isPrivate){
        JPanel item = new JPanel(new BorderLayout()); item.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));
        item.setBackground(groupId == selectedGroupId ? selected : normal); JLabel label = new JLabel(name);
        label.setForeground(Color.WHITE); label.setBorder(BorderFactory.createEmptyBorder(12,14,12,8)); item.add(label); conversationItems.put(groupId,item);
        item.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ if(groupId != selectedGroupId) item.setBackground(hover); }
            public void mouseExited(MouseEvent e){ if(groupId != selectedGroupId) item.setBackground(normal); }
            public void mouseClicked(MouseEvent e){ selectConversation(groupId,name,isPrivate); }
        }); return item;
    }

    private void selectConversation(int groupId,String name,boolean isPrivate){
        selectedGroupId = groupId; selectedIsPrivate = isPrivate; conversationName.setText(name);
        for(Integer id: conversationItems.keySet()) conversationItems.get(id).setBackground(id == groupId ? selected : normal);
        messagesPanel.removeAll(); messagesPanel.revalidate(); messagesPanel.repaint();
        send(MsgTypes.GET_ALL_GROUP_CHAT,new JSONObject().put("msgDestination",groupId));
    }

    private void renderHistory(JSONArray messages){
        messagesPanel.removeAll(); if(messages != null) for(int i = 0; i < messages.length(); i++) appendMessage(messages.getJSONObject(i));
        messagesPanel.revalidate(); messagesPanel.repaint(); scrollToBottom();
    }

    private void appendMessage(JSONObject msg){
        int sender = msg.getInt("user_id"); boolean own = sender == userId;
        JPanel row = new JPanel(new FlowLayout(own ? FlowLayout.RIGHT : FlowLayout.LEFT)); row.setOpaque(false);
        JPanel bubble = new JPanel(); bubble.setLayout(new BoxLayout(bubble,BoxLayout.Y_AXIS)); bubble.setBackground(own ? new Color(210,240,205) : Color.WHITE);
        bubble.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(205,205,205)),BorderFactory.createEmptyBorder(7,10,7,10)));
        if(!own && !selectedIsPrivate){ JLabel senderLabel = new JLabel("User " + sender); senderLabel.setFont(senderLabel.getFont().deriveFont(Font.BOLD,11f)); bubble.add(senderLabel); }
        bubble.add(new JLabel(msg.optString("content"))); row.add(bubble); messagesPanel.add(row);
        messagesPanel.revalidate(); messagesPanel.repaint(); scrollToBottom();
    }

    private void scrollToBottom(){ SwingUtilities.invokeLater(() -> messagesScroll.getVerticalScrollBar().setValue(messagesScroll.getVerticalScrollBar().getMaximum())); }
    private void sendMessage(){
        String content = messageField.getText().trim(); if(content.isEmpty() || selectedGroupId < 0) return;
        send(MsgTypes.SEND_MSG,new JSONObject().put("msgDestination",selectedGroupId).put("content",content)); messageField.setText("");
    }
    private void startDirect(){
        String value = JOptionPane.showInputDialog(this,"Other user's numeric ID:"); if(value == null) return;
        try{ send(MsgTypes.INVITE_TO_DM,new JSONObject().put("user_id",Integer.parseInt(value.trim()))); }
        catch(Exception e){ JOptionPane.showMessageDialog(this,"Enter a numeric user ID"); }
    }
    private void createGroup(){
        String name = JOptionPane.showInputDialog(this,"Group name:"); if(name == null || name.trim().isEmpty()) return;
        String members = JOptionPane.showInputDialog(this,"Member IDs, separated by commas:"); if(members == null) return;
        try{
            JSONArray users = new JSONArray(); for(String value: members.split(",")) if(!value.trim().isEmpty()) users.put(Integer.parseInt(value.trim()));
            send(MsgTypes.MAKE_GROUP,new JSONObject().put("group_name",name.trim()).put("users_id",users));
        }catch(Exception e){ JOptionPane.showMessageDialog(this,"Member IDs must be numeric"); }
    }
}
