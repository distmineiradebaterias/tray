package qz.ui.tray;

import qz.common.Constants;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;

public class TaskbarTrayIcon extends JFrame implements WindowListener {

    private Dimension iconSize;
    private JPopupMenu popup;

    public TaskbarTrayIcon(Image trayImage, final ActionListener exitListener) {
        super(Constants.ABOUT_TITLE);
        initializeComponents(trayImage, exitListener);
    }

    private void initializeComponents(Image trayImage, final ActionListener exitListener) {
        // must come first
        setUndecorated(true);
        setTaskBarTitle(getTitle());
        setSize(0, 0);
        getContentPane().setBackground(Color.BLACK);
        iconSize = new Dimension(40, 40);

        setIconImage(trayImage);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitListener.actionPerformed(new ActionEvent(e.getComponent(), e.getID(), "Exit"));
            }
        });
        addWindowListener(this);
    }

    // fixes Linux taskbar title per http://hg.netbeans.org/core-main/rev/5832261b8434, JDK-6528430
    public static void setTaskBarTitle(String title) {
        try {
            Class<?> toolkit = Toolkit.getDefaultToolkit().getClass();
            if ("sun.awt.X11.XToolkit".equals(toolkit.getName())) {
                final Field awtAppClassName = toolkit.getDeclaredField("awtAppClassName");
                awtAppClassName.setAccessible(true);
                awtAppClassName.set(null, title);
            }
        }
        catch(Exception ignore) {}
    }

    /**
     * Returns the "tray" icon size (not the dialog size)
     */
    @Override
    public Dimension getSize() {
        return iconSize;
    }

    public void setJPopupMenu(final JPopupMenu popup) {
        this.popup = popup;
        this.popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                setState(JFrame.ICONIFIED);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                setState(JFrame.ICONIFIED);
            }
        });
        this.popup.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {}

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    TaskbarTrayIcon.this.popup.setVisible(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {}
        });
    }

    public void displayMessage(String caption, String text, TrayIcon.MessageType level) {
        int messageType;
        switch(level) {
            case WARNING:
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case ERROR:
                messageType = JOptionPane.ERROR_MESSAGE;
                break;
            case INFO:
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            case NONE:
            default:
                messageType = JOptionPane.PLAIN_MESSAGE;
        }
        JOptionPane.showMessageDialog(null, text, caption, messageType);
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        setLocation(p);
        // call show against parent to prevent un-clickable state
        popup.show(this, 0, 0);

        // move to mouse cursor; adjusting for screen boundaries
        Point before = popup.getLocationOnScreen();
        Point after = new Point();
        after.setLocation(before.x < p.x? p.x - popup.getWidth():p.x, before.y < p.y? p.y - popup.getHeight():p.y);
        popup.setLocation(after);
    }
    
        // Anchor this invisible frame at the top-left of the current screen so popup coordinates stay stable
        setLocation(screenBounds.x, screenBounds.y);
    
        // Calculate popup position near the click (cursor)
        Dimension popupSize = popup.getPreferredSize();
    
        int x = mouse.x + 1; // slight offset so the cursor isn't exactly on the border
        int y = mouse.y + 1;
    
        // If it would overflow right, shift left
        int maxX = screenBounds.x + screenBounds.width - popupSize.width;
        if (x > maxX) x = maxX;
    
        // If it would overflow bottom, open above the cursor instead
        int maxY = screenBounds.y + screenBounds.height - popupSize.height;
        if (y > maxY) y = mouse.y - popupSize.height - 1;
    
        // Clamp top/left
        if (x < screenBounds.x) x = screenBounds.x;
        if (y < screenBounds.y) y = screenBounds.y;
    
        // Show relative to the anchored frame
        popup.show(this, x - screenBounds.x, y - screenBounds.y);
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {}

    @Override
    public void windowClosing(WindowEvent windowEvent) {}

    @Override
    public void windowClosed(WindowEvent windowEvent) {}

    @Override
    public void windowIconified(WindowEvent windowEvent) {}

    @Override
    public void windowActivated(WindowEvent windowEvent) {}

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {
        if (popup != null) {
            popup.setVisible(false);
            setState(JFrame.ICONIFIED);
        }
    }

}
