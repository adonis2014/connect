/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class ChannelWriter extends ConnectorClass {

    private HashMap channelList;

    public ChannelWriter() {
        name = ChannelWriterProperties.name;
        initComponents();
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ChannelWriterProperties.DATATYPE, name);
        properties.put(ChannelWriterProperties.CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));
        properties.put(ChannelWriterProperties.CHANNEL_TEMPLATE, template.getText());

        if (channelResponseYes.isSelected()) {
            properties.put(ChannelWriterProperties.CHANNEL_SYNCHRONOUS, UIConstants.YES_OPTION);
        } else {
            properties.put(ChannelWriterProperties.CHANNEL_SYNCHRONOUS, UIConstants.NO_OPTION);
        }

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");

        String selectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(ChannelWriterProperties.CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }

        // sort the channels in alpha-numeric order.
        Collections.sort(channelNameArray);

        // add "None" to the very top of the list.
        channelNameArray.add(0, "None");

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        channelNames.setSelectedItem(selectedChannelName);

        if (((String) props.get(ChannelWriterProperties.CHANNEL_SYNCHRONOUS)).equals(UIConstants.YES_OPTION)) {
            channelResponseYes.setSelected(true);
        } else {
            channelResponseNo.setSelected(true);
        }

        template.setText((String) props.get(ChannelWriterProperties.CHANNEL_TEMPLATE));

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults() {
        return new ChannelWriterProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        return valid;
    }

    private void resetInvalidProperties() {
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        return error;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        URL = new javax.swing.JLabel();
        channelNames = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel1 = new javax.swing.JLabel();
        channelResponseYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        channelResponseNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel7 = new javax.swing.JLabel();
        template = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        URL.setText("Channel Name:");

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("<html>Select the channel to which messages accepted by this destination's filter should be written,<br> or none to not write the message at all.</html>");

        jLabel1.setText("Wait for Channel Response:");

        channelResponseYes.setBackground(new java.awt.Color(255, 255, 255));
        channelResponseYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(channelResponseYes);
        channelResponseYes.setText("Yes");
        channelResponseYes.setToolTipText("<html>If Yes, then the destination will wait until it gets a response from the destination channel<br> (after it has fully executed all of its destinations) before further destinations are processed on the current channel.<br>If No, then the current channel's workflow will continue regardless of what the destination channel is doing.</html>");
        channelResponseYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        channelResponseNo.setBackground(new java.awt.Color(255, 255, 255));
        channelResponseNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(channelResponseNo);
        channelResponseNo.setText("No");
        channelResponseNo.setToolTipText("<html>If Yes, then the destination will wait until it gets a response from the destination channel<br> (after it has fully executed all of its destinations) before further destinations are processed on the current channel.<br>If No, then the current channel's workflow will continue regardless of what the destination channel is doing.</html>");
        channelResponseNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel7.setText("Template:");

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        template.setToolTipText("<html>A Velocity enabled template for the actual message to be written to the channel.<br>In many cases, the default value of \"${message.encodedData}\" is sufficient.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel1)
                    .addComponent(URL))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(channelResponseYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(channelResponseNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(channelResponseYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(channelResponseNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private com.webreach.mirth.client.ui.components.MirthRadioButton channelResponseNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton channelResponseYes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea template;
    // End of variables declaration//GEN-END:variables
}