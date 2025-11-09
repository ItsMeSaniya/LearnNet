package com.netQuiz.client.swing;

import com.netQuiz.client.service.ClientServiceManager;
import com.netQuiz.shared.FileInfo;
import com.netQuiz.shared.Message;
import com.netQuiz.shared.Quiz;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainSwing extends JFrame {
    private final ClientServiceManager serviceManager = ClientServiceManager.getInstance();

    private JTextArea chatArea;
    private JTextField chatInput;

    private DefaultListModel<String> quizListModel;
    private DefaultListModel<String> fileListModel;
    private DefaultListModel<String> userListModel;
    
    private JList<String> quizJList;
    private JPanel quizContentPanel;
    private Quiz currentQuiz;
    private ButtonGroup[] answerGroups;

    public MainSwing() {
        setTitle("NetQuiz");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        // Chat tab
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel chatInputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> sendChat());
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendBtn, BorderLayout.EAST);
        chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
        tabs.add("Chat", chatPanel);

        // Quiz tab
        JPanel quizPanel = buildQuizPanel();
        tabs.add("Quiz", quizPanel);

        // Files tab
        JPanel filesPanel = new JPanel(new BorderLayout());
        fileListModel = new DefaultListModel<>();
        JList<String> filesList = new JList<>(fileListModel);
        filesPanel.add(new JScrollPane(filesList), BorderLayout.CENTER);
        JButton uploadBtn = new JButton("Upload");
        uploadBtn.addActionListener(e -> uploadFile());
        filesPanel.add(uploadBtn, BorderLayout.SOUTH);
        tabs.add("Files", filesPanel);

        // Users tab
        JPanel usersPanel = new JPanel(new BorderLayout());
        userListModel = new DefaultListModel<>();
        JList<String> usersList = new JList<>(userListModel);
        usersPanel.add(new JScrollPane(usersList), BorderLayout.CENTER);
        tabs.add("Users", usersPanel);

        // Notifications tab
        JPanel notifPanel = new JPanel(new BorderLayout());
        JTextArea notifArea = new JTextArea();
        notifArea.setEditable(false);
        notifPanel.add(new JScrollPane(notifArea), BorderLayout.CENTER);
        tabs.add("Notifications", notifPanel);

        add(tabs, BorderLayout.CENTER);

        // Register chat handler to append messages
        serviceManager.connectChat(msg -> SwingUtilities.invokeLater(() -> {
            // Filter out own messages (already displayed as "You: ...")
            if (!msg.getSender().equals(serviceManager.getUsername())) {
                chatArea.append(msg.getSender() + ": " + msg.getContent() + "\n");
            }
        }));

        // Load initial lists asynchronously
        loadInitialData();
    }
    
    /**
     * Update user list - called when server broadcasts user list updates
     */
    public void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            users.forEach(userListModel::addElement);
        });
    }

    private void loadInitialData() {
        new Thread(() -> {
            try {
                List<String> quizzes = serviceManager.getQuizService().getQuizList();
                SwingUtilities.invokeLater(() -> {
                    quizListModel.clear();
                    quizzes.forEach(quizListModel::addElement);
                });
            } catch (Exception ignored) {}

            try {
                List<FileInfo> files = serviceManager.getFileService().getFileList();
                SwingUtilities.invokeLater(() -> {
                    fileListModel.clear();
                    files.forEach(f -> fileListModel.addElement(f.getFileName()));
                });
            } catch (Exception ignored) {}

            // User list is automatically updated via the userListHandler registered during login
            // No need to fetch it here
        }).start();
    }

    private void sendChat() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        try {
            serviceManager.getChatService().sendMessage(serviceManager.getUsername(), text);
            chatArea.append("You: " + text + "\n");
            chatInput.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage());
        }
    }

    private void uploadFile() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            new Thread(() -> {
                try {
                    serviceManager.getFileService().uploadFile(f, serviceManager.getUsername());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Upload complete"));
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Upload failed: " + e.getMessage()));
                }
            }).start();
        }
    }
    
    private JPanel buildQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Left: quiz list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Quizzes"));
        quizListModel = new DefaultListModel<>();
        quizJList = new JList<>(quizListModel);
        leftPanel.add(new JScrollPane(quizJList), BorderLayout.CENTER);
        JButton startBtn = new JButton("Start Quiz");
        startBtn.addActionListener(e -> startQuiz());
        leftPanel.add(startBtn, BorderLayout.SOUTH);
        
        // Right: quiz content
        quizContentPanel = new JPanel();
        quizContentPanel.setLayout(new BoxLayout(quizContentPanel, BoxLayout.Y_AXIS));
        JScrollPane contentScroll = new JScrollPane(quizContentPanel);
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, contentScroll);
        split.setDividerLocation(250);
        panel.add(split, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void startQuiz() {
        String selected = quizJList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a quiz");
            return;
        }
        
        String quizId = selected.split(":")[0].trim();
        
        new Thread(() -> {
            try {
                currentQuiz = serviceManager.getQuizService().getQuiz(quizId);
                SwingUtilities.invokeLater(this::displayQuiz);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Failed to load quiz: " + e.getMessage())
                );
            }
        }).start();
    }
    
    private void displayQuiz() {
        if (currentQuiz == null) return;
        
        quizContentPanel.removeAll();
        
        JLabel title = new JLabel("Quiz: " + currentQuiz.getTitle());
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        quizContentPanel.add(title);
        
        List<Quiz.Question> questions = currentQuiz.getQuestions();
        answerGroups = new ButtonGroup[questions.size()];
        
        for (int i = 0; i < questions.size(); i++) {
            Quiz.Question q = questions.get(i);
            
            JPanel qPanel = new JPanel();
            qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
            qPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder("Question " + (i + 1))
            ));
            
            JLabel qLabel = new JLabel("<html>" + q.getQuestion() + "</html>");
            qLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
            qPanel.add(qLabel);
            
            ButtonGroup bg = new ButtonGroup();
            answerGroups[i] = bg;
            
            for (int j = 0; j < q.getOptions().size(); j++) {
                JRadioButton rb = new JRadioButton(q.getOptions().get(j));
                rb.setActionCommand(String.valueOf(j));
                bg.add(rb);
                qPanel.add(rb);
            }
            
            quizContentPanel.add(qPanel);
        }
        
        JButton submitBtn = new JButton("Submit Answers");
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> submitQuiz());
        quizContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        quizContentPanel.add(submitBtn);
        quizContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        quizContentPanel.revalidate();
        quizContentPanel.repaint();
    }
    
    private void submitQuiz() {
        if (currentQuiz == null || answerGroups == null) return;
        
        int[] answers = new int[answerGroups.length];
        for (int i = 0; i < answerGroups.length; i++) {
            ButtonModel selected = answerGroups[i].getSelection();
            if (selected != null) {
                answers[i] = Integer.parseInt(selected.getActionCommand());
            } else {
                answers[i] = -1; // no answer
            }
        }
        
        new Thread(() -> {
            try {
                int score = serviceManager.getQuizService().submitAnswers(
                    serviceManager.getUsername(), 
                    currentQuiz.getId(), 
                    answers
                );
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Quiz Submitted Successfully!\n\nYour score: " + score + " / " + currentQuiz.getQuestions().size(),
                        "Quiz Result",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    quizContentPanel.removeAll();
                    quizContentPanel.revalidate();
                    quizContentPanel.repaint();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Failed to submit: " + e.getMessage())
                );
            }
        }).start();
    }
}
