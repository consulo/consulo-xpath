/*
 * Copyright 2002-2005 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.xpathView.ui;

import consulo.ui.ColorBox;
import consulo.ui.ex.awt.IdeBorderFactory;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import org.intellij.plugins.xpathView.Config;

import jakarta.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConfigUI extends JPanel {

    private JCheckBox scrollToFirst;
    private JCheckBox useContextAtCursor;
    private JCheckBox highlightStartTagOnly;
    private JCheckBox addErrorStripe;
    private JCheckBox showInToolbar;
    private JCheckBox showInMainMenu;
    private ColorBox chooseHighlight;
    private ColorBox chooseContext;

    public ConfigUI(Config configuration) {
        init();
        setConfig(configuration);
    }

    private void init() {
        setLayout(new BorderLayout());
        JPanel c = this;

        scrollToFirst = new JCheckBox("Scroll first hit into visible area");
        scrollToFirst.setMnemonic('S');

        useContextAtCursor = new JCheckBox("Use node at cursor as context node");
        useContextAtCursor.setMnemonic('N');
        useContextAtCursor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stateChanged();
            }
        });

        highlightStartTagOnly = new JCheckBox("Highlight only start tag instead of whole tag content");
        highlightStartTagOnly.setMnemonic('H');

        addErrorStripe = new JCheckBox("Add error stripe markers for each result");
        addErrorStripe.setMnemonic('A');

        showInToolbar = new JCheckBox("Show actions in Toolbar");
        showInToolbar.setMnemonic('T');
        showInToolbar.setToolTipText("Uncheck to remove XPath-related actions from the toolbar");
        showInMainMenu = new JCheckBox("Show actions in Main Menu");
        showInMainMenu.setMnemonic('M');
        showInMainMenu.setToolTipText("Uncheck to remove XPath-related actions from the Main-Menubar");

        JPanel settings = new JPanel(new BorderLayout());
        settings.setBorder(IdeBorderFactory.createTitledBorder("Settings", true));
        c.add(c = new JPanel(new BorderLayout()), BorderLayout.NORTH);
        c.add(settings, BorderLayout.NORTH);

        settings.add(scrollToFirst, BorderLayout.NORTH);
        settings.add(settings = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
        settings.add(useContextAtCursor, BorderLayout.NORTH);
        settings.add(settings = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
        settings.add(highlightStartTagOnly, BorderLayout.NORTH);
        settings.add(settings = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
        settings.add(addErrorStripe, BorderLayout.NORTH);
        settings.add(settings = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
        settings.add(showInToolbar, BorderLayout.NORTH);
        settings.add(settings = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
        settings.add(showInMainMenu, BorderLayout.NORTH);
        settings.add(/*settings = */new JPanel(new BorderLayout()), BorderLayout.SOUTH);

        JPanel colors = new JPanel(new GridBagLayout());
        colors.setBorder(IdeBorderFactory.createTitledBorder("Colors", true));
        c.add(c = new JPanel(new BorderLayout()), BorderLayout.SOUTH);
        c.add(colors, BorderLayout.NORTH);

        final GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);

        colors.add(new JLabel("Highlight color:"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;

        chooseHighlight = ColorBox.create();
        colors.add(TargetAWT.to(chooseHighlight), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        colors.add(new JLabel("Context node color:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;

        chooseContext = ColorBox.create();
        colors.add(TargetAWT.to(chooseContext), constraints);
    }

    @Nonnull
    public Config getConfig() {
        final Config config = new Config();
        config.setHighlightStartTagOnly(highlightStartTagOnly.isSelected());
        config.setUseContextAtCursor(useContextAtCursor.isSelected());
        config.setScrollToFirst(scrollToFirst.isSelected());
        config.setAddErrorStripe(addErrorStripe.isSelected());
        config.SHOW_IN_TOOLBAR = showInToolbar.isSelected();
        config.SHOW_IN_MAIN_MENU = showInMainMenu.isSelected();
        config.getAttributes().setBackgroundColor(chooseHighlight.getValue());
        if (useContextAtCursor.isSelected()) {
            config.getContextAttributes().setBackgroundColor(chooseContext.getValue());
        }
        return config;
    }

    public void setConfig(@Nonnull Config configuration) {
        scrollToFirst.setSelected(configuration.isScrollToFirst());
        highlightStartTagOnly.setSelected(configuration.isHighlightStartTagOnly());
        useContextAtCursor.setSelected(configuration.isUseContextAtCursor());
        addErrorStripe.setSelected(configuration.isAddErrorStripe());
        showInToolbar.setSelected(configuration.SHOW_IN_TOOLBAR);
        showInMainMenu.setSelected(configuration.SHOW_IN_MAIN_MENU);
        chooseHighlight.setValue(configuration.getAttributes().getBackgroundColor());
        chooseContext.setValue(configuration.getContextAttributes().getBackgroundColor());
        stateChanged();
    }

    private void stateChanged() {
        chooseContext.setEnabled(useContextAtCursor.isSelected());
    }
}
