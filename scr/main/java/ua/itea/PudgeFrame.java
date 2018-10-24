package ua.itea;

import ua.itea.model.Data;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.List;

public class PudgeFrame extends JFrame {

    private Connection conn = null;g
    private Object[][] resData = null;
    private String[] headers = null;
    JTable table = new JTable();


    private void alertError(Exception e) {
        e.printStackTrace();
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        String error = (writer.toString().length() > 700) ? writer.toString().substring(0,700) : writer.toString();
        JOptionPane.showMessageDialog(null,
                error,
                "Ooops! Error occured.",
                JOptionPane.ERROR_MESSAGE);
    }

    private void connect() {
        try {
            if (Files.exists(Paths.get("pudge.db"))) {
                Class.forName("org.sqlite.JDBC");
                // db parameters
                String url = "jdbc:sqlite:pudge.db";
                // create a connection to the database
                conn = DriverManager.getConnection(url);

                print("Connection to SQLite has been established.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            alertError(e);
        }
    }

    private void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
                print("Connection closed");
            }
        } catch (SQLException ex) {
            alertError(ex);
        }
    }

    private Map<String[], String[][]> executeAndGetResult(String sql) {
        try {
            if (conn != null) {
                PreparedStatement prep = conn.prepareStatement(sql);
                ResultSet res = prep.executeQuery();
                if (res != null) {
                    if (res.next()) {
                        ResultSetMetaData resMeta = res.getMetaData();
                        String[] headers = new String[resMeta.getColumnCount()];
                        List<Data> resData = new ArrayList<>();

                        for (int i = 0; i < resMeta.getColumnCount(); i++) {
                            headers[i] = resMeta.getColumnName(i+1);
                        }
                        print(Arrays.toString(headers));
                        do {
                            Data d = new Data(resMeta.getColumnCount());
                            for (int i = 0; i < resMeta.getColumnCount(); i++) {
                                d.set(res.getObject(i + 1), i);
                            }
                            resData.add(d);
                        } while (res.next());
                        String[][] resStrArray = new String[resData.size()][headers.length];
                        for (int i = 0; i < resData.size();i++) {
                            for (int j = 0; j < headers.length; j++) {
                                resStrArray[i][j] = resData.get(i).get(j).toString();
                            }
                            print (Arrays.toString(resStrArray[i]));
                        }
                        Map<String[], String[][]> resMap = new HashMap<>();
                        resMap.put(headers, resStrArray);
                        return resMap;
                    } else {
                        alertError(new SQLException("The result of sql is empty"));
                        return null;
                    }
                } else {
                    alertError(new SQLException("The result of sql = null"));
                    return null;
                }
            }
        } catch (SQLException ex) {
            alertError(ex);
            return null;
        }
        return null;
    }

    private void print(String s) {
        System.out.println(s);
    }

    private void sqlButtonAction(String sql) {
        try {
            connect();
            Map<String[],String[][]> res = executeAndGetResult(sql);
            headers = null;
            resData = null;
            if (res != null && res.keySet().size() != 0) {
                for (String[] s: res.keySet()) {
                    headers = s;
                    resData = res.get(s);
                }
                if (headers != null && resData != null) {
                    table.setModel(new DefaultTableModel(resData, headers) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            //all cells false
                            return false;
                        }
                    });
                }
            } else {
                alertError(new SQLException("The result is empty"));
            }

        } catch (Exception e) {
            alertError(e);
        } finally {
            closeConnection();
        }
    }

    public PudgeFrame() {
        setTitle("Sql Pudge Frame");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int width = 600;
        int height = 400;
        setSize(width, height);
        setLocation((int) (screenSize.getWidth() / 2) - (width / 2), (int) (screenSize.getHeight() / 2) - (height / 2));

        JTextArea sqlTextArea = new JTextArea("select * from main.Heroes");
        JButton sqlButton = new JButton("<html>" + "Execute<br>(Ctrl+Enter)" + "</html>");
        sqlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sqlButtonAction(sqlTextArea.getText());
            }
        });

        JSplitPane spv = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(sqlTextArea) , sqlButton);
        spv.setDividerSize(5);

        JSplitPane spDownBar = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spv, new JScrollPane(table));
        spDownBar.setDividerSize(5);

        sqlTextArea.requestFocus();
        sqlTextArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sqlButtonAction(sqlTextArea.getText());
                    e.consume();
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                spDownBar.setDividerLocation(height / 3);
                spv.setDividerLocation(getWidth()-(150));
            }
        });
        URL url = this.getClass().getClassLoader().getResource("images/img.png");
        JLabel label = new JLabel(new ImageIcon(url));

//        add(spDownBar);
        add(label);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

    }

    public static void main(String[] args) {
        new PudgeFrame();
    }
}