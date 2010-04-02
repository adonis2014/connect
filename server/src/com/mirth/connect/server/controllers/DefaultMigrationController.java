/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers;

import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.FileUtil;
import com.webreach.mirth.server.util.SqlConfig;

/**
 * The MigrationController migrates the database to the current version.
 * 
 */
public class DefaultMigrationController extends MigrationController {
    private static final String DELTA_FOLDER = "deltas";
    private Logger logger = Logger.getLogger(this.getClass());
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    // singleton pattern
    private static DefaultMigrationController instance = null;

    private DefaultMigrationController() {

    }

    public static MigrationController create() {
        synchronized (DefaultMigrationController.class) {
            if (instance == null) {
                instance = new DefaultMigrationController();
            }

            return instance;
        }
    }

    public void migrate() {
        // check for one of the tables to see if we should run the create script

        Connection conn = null;
        ResultSet resultSet = null;

        try {
            conn = SqlConfig.getSqlMapClient().getDataSource().getConnection();
            // Gets the database metadata
            DatabaseMetaData dbmd = conn.getMetaData();

            // Specify the type of object; in this case we want tables
            String[] types = { "TABLE" };
            String tablePattern = "CONFIGURATION"; // this is a table that has
            // remained unchanged since
            // day 1
            resultSet = dbmd.getTables(null, null, tablePattern, types);

            boolean resultFound = resultSet.next();

            // Some databases only accept lowercase table names
            if (!resultFound) {
                resultSet = dbmd.getTables(null, null, tablePattern.toLowerCase(), types);
                resultFound = resultSet.next();
            }

            // If missing this table we can assume that they don't have the
            // schema installed
            if (!resultFound) {
                createSchema(conn);
                return;
            }
        } catch (Exception e) {
            logger.error("Could not create schema on the configured database.", e);
            return;
        } finally {
            DatabaseUtil.close(resultSet);
            DatabaseUtil.close(conn);
        }

        // otherwise proceed with migration if necessary
        try {
            int newSchemaVersion = configurationController.getSchemaVersion();
            int oldSchemaVersion;

            if (newSchemaVersion == -1)
                return;

            Object result = null;

            try {
                result = SqlConfig.getSqlMapClient().queryForObject("Configuration.getSchemaVersion");
            } catch (SQLException e) {

            }

            if (result == null)
                oldSchemaVersion = 0;
            else
                oldSchemaVersion = ((Integer) result).intValue();

            if (oldSchemaVersion == newSchemaVersion)
                return;
            else {
                migrate(oldSchemaVersion, newSchemaVersion);
                migrateContents(oldSchemaVersion, newSchemaVersion);

                if (result == null)
                    SqlConfig.getSqlMapClient().update("Configuration.setInitialSchemaVersion", newSchemaVersion);
                else
                    SqlConfig.getSqlMapClient().update("Configuration.updateSchemaVersion", newSchemaVersion);
            }
        } catch (Exception e) {
            logger.error("Could not initialize migration controller.", e);
        }
    }

    public void migrateExtensions() {
        try {
            for (PluginMetaData plugin : extensionController.getPluginMetaData().values()) {
                Properties pluginProperties = extensionController.getPluginProperties(plugin.getName());

                if (pluginProperties != null) {
                    int baseSchemaVersion = Integer.parseInt(pluginProperties.getProperty("schema", "-1"));

                    if (plugin.getSqlScript() != null) {
                        String contents = FileUtil.read(ExtensionController.getExtensionsPath() + plugin.getPath() + File.separator + plugin.getSqlScript());
                        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contents)));
                        TreeMap<Integer, String> scripts = getDiffsForVersion(baseSchemaVersion, document);
                        List<String> scriptList = new LinkedList<String>();

                        for (String script : scripts.values()) {
                            script = script.trim();
                            StringBuilder sb = new StringBuilder();
                            boolean blankLine = false;
                            Scanner scanner = new Scanner(script);

                            while (scanner.hasNextLine()) {
                                String temp = scanner.nextLine();

                                if (temp.trim().length() > 0)
                                    sb.append(temp + " ");
                                else
                                    blankLine = true;

                                if (blankLine || !scanner.hasNextLine()) {
                                    scriptList.add(sb.toString().trim());
                                    blankLine = false;
                                    sb.delete(0, sb.length());
                                }
                            }
                        }

                        // if there were no scripts, don't update the schema
                        // version
                        if (!scriptList.isEmpty()) {
                            DatabaseUtil.executeScript(scriptList, false);
                            int maxSchemaVersion = -1;

                            for (Entry<Integer, String> entry : scripts.entrySet()) {
                                int key = entry.getKey().intValue();

                                if (key > maxSchemaVersion) {
                                    maxSchemaVersion = key;
                                }
                            }

                            pluginProperties.setProperty("schema", String.valueOf(maxSchemaVersion));
                            extensionController.setPluginProperties(plugin.getName(), pluginProperties);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not initialize migration controller.", e);
        }
    }

    private TreeMap<Integer, String> getDiffsForVersion(int version, Document document) throws Exception {
        TreeMap<Integer, String> scripts = new TreeMap<Integer, String>();
        NodeList diffNodes = document.getElementsByTagName("diff");
        String databaseType = configurationController.getDatabaseType();

        for (int i = 0; i < diffNodes.getLength(); i++) {
            Node attribute = diffNodes.item(i).getAttributes().getNamedItem("version");
            if (attribute != null) {
                String versionString = attribute.getTextContent();

                int scriptVersion = Integer.parseInt(versionString);
                if (scriptVersion > version) {

                    NodeList scriptNodes = ((Element) diffNodes.item(i)).getElementsByTagName("script");

                    if (scriptNodes.getLength() == 0) {
                        throw new Exception("Missing script element for version = " + scriptVersion);
                    }

                    for (int j = 0; j < scriptNodes.getLength(); j++) {
                        Node scriptNode = scriptNodes.item(j);
                        Node scriptNodeAttribute = scriptNode.getAttributes().getNamedItem("type");

                        String[] dbTypes = scriptNodeAttribute.getTextContent().split(",");
                        for (int k = 0; k < dbTypes.length; k++) {
                            if (dbTypes[k].equals("all") || dbTypes[k].equals(databaseType)) {
                                scripts.put(new Integer(scriptVersion), scriptNode.getTextContent());
                            }
                        }
                    }
                }
            }
        }

        return scripts;
    }

    private void createSchema(Connection conn) throws Exception {
        File creationScript = new File(new File(configurationController.getConfigurationDir() + File.separator + configurationController.getDatabaseType()), configurationController.getDatabaseType() + "-database.sql");
        DatabaseUtil.executeScript(creationScript, true);
    }

    private void migrate(int oldVersion, int newVersion) throws Exception {
        File deltaFolder = new File(ClassPathResource.getResourceURI(DELTA_FOLDER));
        String deltaPath = deltaFolder.getPath() + File.separator;
        String databaseType = configurationController.getDatabaseType();

        while (oldVersion < newVersion) {
            // gets the correct migration script based on dbtype and versions
            File migrationFile = new File(deltaPath + databaseType + "-" + oldVersion + "-" + ++oldVersion + ".sql");
            DatabaseUtil.executeScript(migrationFile, true);
        }
    }

    /**
     * When migrating Mirth Connect versions, certain configurations saved to
     * the database might also need to be updated. This method uses the schema
     * version migration process to migrate configurations saved in the
     * database.
     */
    private void migrateContents(int oldVersion, int newVersion) throws Exception {

        if ((oldVersion == 6) && (newVersion == 7)) {
            CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
            try {
                codeTemplateController.updateCodeTemplates(ImportConverter.convertCodeTemplates(codeTemplateController.getCodeTemplate(null)));
            } catch (Exception e) {
                logger.error("Error migrating code templates.", e);
            }
        }
    }
}