package org.example;

import javax.mail.*;
import java.util.*;
import java.util.stream.Collectors;

public class GmailLabeler {
    private static final String HOST = "imap.gmail.com";
    private static final String USER = "";
    private static final String PASSWORD = "";
    private static final int NUM_MAX_MENSAJES = 7;
    private static final String LABEL_DONE = "Done";
    private static final String LABEL_WORK_IN_PROGRESS = "Work.in.Progress";
    private static final String LABEL_TO_BE_DONE = "To.be.Done";

    public void labelEmails() {
        Store store = null;
        Folder inbox = null;
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);

            store = session.getStore();
            store.connect(HOST, USER, PASSWORD);

            createLabelFolder(store, LABEL_DONE);
            createLabelFolder(store, LABEL_WORK_IN_PROGRESS);
            createLabelFolder(store, LABEL_TO_BE_DONE);

            inbox = store.getDefaultFolder().getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();
            int totalMessages = Math.min(messages.length, NUM_MAX_MENSAJES);

            for (int i = 0; i < totalMessages; i++) {
                String label = getLabelForIndex(i);
                copyMessageToLabel(store, inbox, messages[i], label);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            closeResources(inbox, store);
        }
    }

    private String getLabelForIndex(int index) {
        if (index < 3) return LABEL_DONE;
        if (index == 3) return LABEL_WORK_IN_PROGRESS;

        return LABEL_TO_BE_DONE;
    }

    private void copyMessageToLabel(Store store, Folder inbox, Message message, String label) throws MessagingException {
        Folder rootFolder = store.getDefaultFolder();
        Folder labelFolder = rootFolder.getFolder(label);

        if (labelFolder.exists()) inbox.copyMessages(new Message[]{message}, labelFolder);
    }

    private void createLabelFolder(Store store, String label) throws MessagingException {
        Folder rootFolder = store.getDefaultFolder();
        Folder labelFolder = rootFolder.getFolder(label);

        if (!labelFolder.exists()) labelFolder.create(Folder.HOLDS_MESSAGES);
    }

    public Map<String, List<String>> fetchLabeledMessages() {
        Map<String, List<String>> labeledMessages = new HashMap<>();
        Store store = null;
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props);

            store = session.getStore();
            store.connect(HOST, USER, PASSWORD);

            for (String label : Arrays.asList("Done", "Work.in.Progress", "To.be.Done")) {
                Folder rootFolder = store.getDefaultFolder();
                Folder folder = rootFolder.getFolder(label);

                if (folder.exists()) {
                    folder.open(Folder.READ_ONLY);
                    List<String> subjects = Arrays.stream(folder.getMessages())
                            .map(m -> {
                                try {
                                    return m.getSubject() != null ? m.getSubject() : "(Sin Asunto)";
                                } catch (MessagingException e) {
                                    return "(Error al obtener asunto)";
                                }
                            })
                            .collect(Collectors.toList());
                    labeledMessages.put(label, subjects);
                    folder.close(false);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        return labeledMessages;
    }

    private void closeResources(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) folder.close(false);
            if (store != null && store.isConnected()) store.close();
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
        }
    }
}
