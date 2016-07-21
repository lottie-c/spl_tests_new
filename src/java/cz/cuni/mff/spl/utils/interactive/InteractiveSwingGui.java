/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package cz.cuni.mff.spl.utils.interactive;

import java.awt.Label;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * The default graphical implementation of interactive login interface.
 * 
 * @author Martin Lacina
 */
public class InteractiveSwingGui implements InteractiveInterface {

    /** The logger. */
    SplLog logger = SplLogger.getLogger(InteractiveSwingGui.class);

    /**
     * Instantiates a new swing interactive gui.
     */
    public InteractiveSwingGui() {

    }

    @Override
    public boolean isInteractive() {
        return true;
    }

    @Override
    public String getString(String prompt) {
        final JTextField value = new JPasswordField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel(convertToMultiline(prompt)),
                value,
        };
        JOptionPane jop = new JOptionPane(inputs,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jop.createDialog("SPL Tools Framework");
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        value.requestFocusInWindow();
                    }
                });
            }
        });
        dialog.pack();
        logger.warn("User attention to string requested [%s]", prompt);
        dialog.setVisible(true);
        int result = (Integer) jop.getValue();
        dialog.dispose();
        if (result == JOptionPane.OK_OPTION) {
            logger.trace("User confirmed request [%s]", prompt);
            return value.getText();
        } else {
            logger.trace("User aborted request [%s]", prompt);
            return null;
        }
    }

    @Override
    public String getMaskedString(String prompt) {
        final JPasswordField value = new JPasswordField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel(convertToMultiline(prompt)),
                value,
        };
        JOptionPane jop = new JOptionPane(inputs,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = jop.createDialog("SPL Tools Framework");
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        value.requestFocusInWindow();
                    }
                });
            }
        });
        dialog.pack();
        logger.warn("User attention to masked string requested [%s]", prompt);
        dialog.setVisible(true);
        int result = (Integer) jop.getValue();
        dialog.dispose();
        if (result == JOptionPane.OK_OPTION) {
            logger.trace("User confirmed request [%s]", prompt);
            return new String(value.getPassword());
        } else {
            return null;
        }
    }

    @Override
    public Boolean getBoolean(String prompt) {
        logger.warn("User attention to decision requested [%s]", convertToMultiline(prompt));
        int result = JOptionPane.showConfirmDialog(null, prompt, "SPL Tools Framework", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            logger.trace("User decided request [%s]", prompt);
            return Boolean.TRUE;
        } else if (result == JOptionPane.NO_OPTION) {
            logger.trace("User decided request [%s]", prompt);
            return Boolean.FALSE;
        } else {
            logger.trace("User aborted request [%s]", prompt);
            return null;
        }
    }

    /**
     * Convert text to multi-line text for {@link Label}.
     * 
     * @param text
     *            The text.
     * @return The multi-line text for {@link Label}.
     */
    private String convertToMultiline(String text) {
        return "<html>" + text.replaceAll("\n", "<br>") + "</html>";
    }
}
