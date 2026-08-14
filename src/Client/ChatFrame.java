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
    private JPanel directPanel = new JPanel(), groupPanel = new JPanel(), messagesPanel = new MessagePanel();
    private JScrollPane messagesScroll;
    private JLabel conversationName = new JLabel("Select a conversation");
    private JTextField messageField = new JTextField();
    private HashMap<Integer,JPanel> conversationItems = new HashMap<>();
    private final Color normal = UiTheme.SIDEBAR_BACKGROUND, hover = UiTheme.SIDEBAR_HOVER, selected = UiTheme.SIDEBAR_SELECTED;

    public ChatFrame(int userId){
        this.userId = userId;
        setTitle("Chat - User " + userId); setSize(1000,680); setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.MAIN_BACKGROUND);
        add(createSidebar(),BorderLayout.WEST); add(createChatPanel(),BorderLayout.CENTER);
        addWindowListener(new WindowAdapter(){ public void windowClosed(WindowEvent e){ ClientLogic.close(); } });
    }

    private JPanel createSidebar(){
        JPanel sidebar = new JPanel(new BorderLayout()); sidebar.setPreferredSize(new Dimension(260,0)); sidebar.setBackground(normal);
        JPanel buttons = new JPanel(new GridLayout(1,2,8,0));
        JButton direct = new RoundedButton("+ Direct",Color.WHITE,UiTheme.BUTTON_HOVER,UiTheme.BUTTON_PRESSED,UiTheme.INPUT_BORDER);
        JButton group = new RoundedButton("+ Group",Color.WHITE,UiTheme.BUTTON_HOVER,UiTheme.BUTTON_PRESSED,UiTheme.INPUT_BORDER);
        styleSidebarButton(direct); styleSidebarButton(group);
        direct.setIcon(UiTheme.icon("user-add.png",18,UiTheme.PRIMARY_BLUE)); group.setIcon(UiTheme.icon("group-add.png",18,UiTheme.PRIMARY_BLUE));
        direct.addActionListener(e -> startDirect()); group.addActionListener(e -> createGroup()); buttons.add(direct); buttons.add(group);
        buttons.setBackground(UiTheme.MAIN_BACKGROUND); buttons.setBorder(BorderFactory.createEmptyBorder(14,12,14,12)); sidebar.add(buttons,BorderLayout.NORTH);
        JPanel lists = new JPanel(); lists.setBackground(normal); lists.setLayout(new BoxLayout(lists,BoxLayout.Y_AXIS));
        lists.add(sectionLabel("DIRECT")); setupList(directPanel); lists.add(directPanel);
        lists.add(sectionLabel("GROUPS")); setupList(groupPanel); lists.add(groupPanel);
        JScrollPane listScroll = new JScrollPane(lists); listScroll.setBorder(BorderFactory.createEmptyBorder());
        listScroll.getViewport().setBackground(UiTheme.SIDEBAR_BACKGROUND); listScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI(UiTheme.SIDEBAR_BACKGROUND));
        listScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8,0)); sidebar.add(listScroll,BorderLayout.CENTER); return sidebar;
    }

    private JLabel sectionLabel(String text){
        JLabel label = new JLabel(text); label.setForeground(UiTheme.SIDEBAR_MUTED); label.setFont(UiTheme.font(Font.BOLD,12));
        label.setBorder(BorderFactory.createEmptyBorder(20,16,8,8)); label.setAlignmentX(Component.LEFT_ALIGNMENT); return label;
    }
    private void setupList(JPanel panel){ panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(normal); panel.setAlignmentX(Component.LEFT_ALIGNMENT); }

    private JPanel createChatPanel(){
        JPanel chat = new JPanel(new BorderLayout()); chat.setBackground(UiTheme.CHAT_BACKGROUND);
        conversationName.setFont(UiTheme.font(Font.BOLD,22)); conversationName.setForeground(UiTheme.TEXT_PRIMARY);
        conversationName.setOpaque(true); conversationName.setBackground(UiTheme.HEADER_BACKGROUND);
        conversationName.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,UiTheme.BORDER_LIGHT),BorderFactory.createEmptyBorder(20,24,20,24))); chat.add(conversationName,BorderLayout.NORTH);
        messagesPanel.setLayout(new BoxLayout(messagesPanel,BoxLayout.Y_AXIS)); messagesPanel.setBackground(UiTheme.CHAT_BACKGROUND);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(10,14,10,14));
        messagesPanel.add(Box.createVerticalGlue());
        messagesScroll = new JScrollPane(messagesPanel); messagesScroll.setBorder(BorderFactory.createEmptyBorder());
        messagesScroll.getViewport().setBackground(UiTheme.CHAT_BACKGROUND); messagesScroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        messagesScroll.getVerticalScrollBar().setPreferredSize(new Dimension(10,0)); chat.add(messagesScroll,BorderLayout.CENTER);
        JPanel compose = new JPanel(new BorderLayout(10,0));
        JButton send = new RoundedButton("Send",UiTheme.PRIMARY_BLUE,UiTheme.PRIMARY_BLUE_HOVER,UiTheme.PRIMARY_BLUE_PRESSED,null);
        stylePrimaryButton(send);
        messageField.setFont(UiTheme.font(Font.PLAIN,14)); messageField.setForeground(UiTheme.TEXT_PRIMARY); messageField.setBackground(UiTheme.INPUT_BACKGROUND);
        messageField.setCaretColor(UiTheme.TEXT_PRIMARY);
        messageField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.INPUT_BORDER,1,true),BorderFactory.createEmptyBorder(11,14,11,14)));
        send.addActionListener(e -> sendMessage()); messageField.addActionListener(e -> sendMessage());
        compose.setBackground(UiTheme.MAIN_BACKGROUND); compose.add(messageField,BorderLayout.CENTER); compose.add(send,BorderLayout.EAST);
        compose.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,UiTheme.BORDER_LIGHT),BorderFactory.createEmptyBorder(12,14,12,14)));
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
        JPanel item = new JPanel(new BorderLayout()); item.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        item.setBackground(groupId == selectedGroupId ? selected : normal); JLabel label = new JLabel(name);
        label.setForeground(UiTheme.SIDEBAR_TEXT); label.setFont(UiTheme.font(Font.PLAIN,14)); label.setIcon(UiTheme.icon(isPrivate ? "user.png" : "group.png",16,UiTheme.SIDEBAR_MUTED));
        label.setIconTextGap(8); label.setBorder(BorderFactory.createEmptyBorder(14,14,14,8)); item.add(label); conversationItems.put(groupId,item);
        setConversationBorder(item,groupId == selectedGroupId);
        item.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ if(groupId != selectedGroupId) item.setBackground(hover); }
            public void mouseExited(MouseEvent e){ if(groupId != selectedGroupId) item.setBackground(normal); }
            public void mouseClicked(MouseEvent e){ selectConversation(groupId,name,isPrivate); }
        }); return item;
    }

    private void selectConversation(int groupId,String name,boolean isPrivate){
        selectedGroupId = groupId; selectedIsPrivate = isPrivate; conversationName.setText(name);
        for(Integer id: conversationItems.keySet()){
            conversationItems.get(id).setBackground(id == groupId ? selected : normal);
            setConversationBorder(conversationItems.get(id),id == groupId);
        }
        messagesPanel.removeAll(); messagesPanel.add(Box.createVerticalGlue()); messagesPanel.revalidate(); messagesPanel.repaint();
        send(MsgTypes.GET_ALL_GROUP_CHAT,new JSONObject().put("msgDestination",groupId));
    }

    private void renderHistory(JSONArray messages){
        messagesPanel.removeAll(); messagesPanel.add(Box.createVerticalGlue());
        if(messages != null) for(int i = 0; i < messages.length(); i++) appendMessage(messages.getJSONObject(i));
        messagesPanel.revalidate(); messagesPanel.repaint(); scrollToBottom();
    }

    private void appendMessage(JSONObject msg){
        int sender = msg.getInt("user_id"); boolean own = sender == userId;
        JPanel row = new JPanel(new FlowLayout(own ? FlowLayout.RIGHT : FlowLayout.LEFT,8,3)); row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        JPanel bubble = new RoundedPanel(own ? UiTheme.OWN_MESSAGE : UiTheme.INCOMING_MESSAGE,own ? UiTheme.OWN_MESSAGE_BORDER : UiTheme.BORDER_SUBTLE,14);
        bubble.setLayout(new BoxLayout(bubble,BoxLayout.Y_AXIS)); bubble.setBorder(BorderFactory.createEmptyBorder(9,13,9,13));
        if(!own && !selectedIsPrivate){ JLabel senderLabel = new JLabel("User " + sender); senderLabel.setFont(UiTheme.font(Font.BOLD,12)); senderLabel.setForeground(UiTheme.SENDER_TEXT); senderLabel.setBorder(BorderFactory.createEmptyBorder(0,0,3,0)); bubble.add(senderLabel); }
        JLabel messageText = new JLabel(msg.optString("content")); messageText.setFont(UiTheme.font(Font.PLAIN,14)); messageText.setForeground(UiTheme.TEXT_PRIMARY);
        bubble.add(messageText); row.add(bubble);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,row.getPreferredSize().height));
        messagesPanel.add(row);
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

    private void styleSidebarButton(JButton button){
        button.setFont(UiTheme.font(Font.BOLD,14)); button.setForeground(UiTheme.TEXT_PRIMARY); button.setIconTextGap(8);
        button.setFocusPainted(false); button.setBorder(BorderFactory.createEmptyBorder(9,10,9,10));
    }

    private void stylePrimaryButton(JButton button){
        button.setFont(UiTheme.font(Font.BOLD,14)); button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(11,24,11,24));
    }

    private void setConversationBorder(JPanel item,boolean isSelected){
        item.setBorder(isSelected ? BorderFactory.createMatteBorder(0,3,0,0,UiTheme.PRIMARY_BLUE) : BorderFactory.createEmptyBorder(0,3,0,0));
    }

    private static class MessagePanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize(){ return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visibleRect,int orientation,int direction){ return 16; }
        public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction){ return Math.max(16,visibleRect.height - 32); }
        public boolean getScrollableTracksViewportWidth(){ return true; }
        public boolean getScrollableTracksViewportHeight(){
            return getParent() instanceof JViewport && getPreferredSize().height < getParent().getHeight();
        }
    }
}
