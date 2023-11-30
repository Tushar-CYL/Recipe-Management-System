import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Vector;

public class RecipeManagementSystem {
    private JFrame frame;
    private JTextField idField, nameField, ingredientsField;
    private JTextArea recipeArea;
    private JComboBox<String> typeComboBox;
    private JButton addButton, updateButton;
    private JTable recipeTable;

    private Connection connection;

    public RecipeManagementSystem() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        initialize();
        connectToDatabase();
        createTable();
        displayRecipes();

        frame.setVisible(true);
    }

    private void initialize() {
        frame = new JFrame("Recipe Management System");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
    
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
    
        inputPanel.add(new JLabel("Recipe ID:"), gbc);
        gbc.gridx++;
        idField = new JTextField(10);
        idField.setEditable(false);
        inputPanel.add(idField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("Recipe Name:"), gbc);
        gbc.gridx++;
        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("Recipe Type:"), gbc);
        gbc.gridx++;
        String[] types = {"Main Dish", "Side Dish", "Dessert", "Thai Dish", "Italian Dish"};
        typeComboBox = new JComboBox<>(types);
        inputPanel.add(typeComboBox, gbc);
    
        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("Ingredients:"), gbc);
        gbc.gridx++;
        ingredientsField = new JTextField(20);
        inputPanel.add(ingredientsField, gbc);
    
        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("Recipe:"), gbc);
        gbc.gridx++;
        recipeArea = new JTextArea(5, 20);
        JScrollPane recipeScrollPane = new JScrollPane(recipeArea);
        inputPanel.add(recipeScrollPane, gbc);
    
        gbc.gridx = 1;
        gbc.gridy++;
        addButton = new JButton("Add Recipe");
        updateButton = new JButton("Update Recipe");
    
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRecipe();
            }
        });
    
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRecipe();
            }
        });
    
        inputPanel.add(addButton, gbc);
        gbc.gridx++;
        inputPanel.add(updateButton, gbc);
    
        // New components for search
        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("Search Recipe:"), gbc);
        gbc.gridx++;
        JTextField searchField = new JTextField(20);
        inputPanel.add(searchField, gbc);
    
        gbc.gridx++;
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayRecipes();
            }
        });
        inputPanel.add(searchButton, gbc);
    
        frame.add(inputPanel, BorderLayout.NORTH);
    
        recipeTable = new JTable();
        JScrollPane tableScrollPane = new JScrollPane(recipeTable);
        frame.add(tableScrollPane, BorderLayout.CENTER);
    
        recipeTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = recipeTable.getSelectedRow();
                displayRecipeDetails(selectedRow);
            }
        });
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/recipe_db", "root", "993355@Tushar");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error connecting to the database.");
        }
    }

    private void createTable() {
        try {
            Statement statement = connection.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS recipes (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(255)," +
                    "type VARCHAR(50)," +
                    "ingredients VARCHAR(1000)," +
                    "recipe TEXT)";
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating table.");
        }
    }

    private void displayRecipes() {
        try {
            String selectQuery = "SELECT * FROM recipes";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectQuery);

            recipeTable.setModel(buildTableModel(resultSet));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving recipes.");
        }
    }

    private void addRecipe() {
        try {
            String insertQuery = "INSERT INTO recipes (name, type, ingredients, recipe) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, nameField.getText());
            preparedStatement.setString(2, (String) typeComboBox.getSelectedItem());
            preparedStatement.setString(3, ingredientsField.getText());
            preparedStatement.setString(4, recipeArea.getText());

            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Recipe added successfully.");
            clearFields();
            displayRecipes();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding recipe.");
        }
    }

    private void updateRecipe() {
        try {
            String updateQuery = "UPDATE recipes SET name=?, type=?, ingredients=?, recipe=? WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, nameField.getText());
            preparedStatement.setString(2, (String) typeComboBox.getSelectedItem());
            preparedStatement.setString(3, ingredientsField.getText());
            preparedStatement.setString(4, recipeArea.getText());
            preparedStatement.setInt(5, Integer.parseInt(idField.getText()));

            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Recipe updated successfully.");
            clearFields();
            displayRecipes();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating recipe.");
        }
    }

    private void displayRecipeDetails(int selectedRow) {
        DefaultTableModel model = (DefaultTableModel) recipeTable.getModel();
        idField.setText(model.getValueAt(selectedRow, 0).toString());
        nameField.setText(model.getValueAt(selectedRow, 1).toString());
        typeComboBox.setSelectedItem(model.getValueAt(selectedRow, 2).toString());
        ingredientsField.setText(model.getValueAt(selectedRow, 3).toString());
        recipeArea.setText(model.getValueAt(selectedRow, 4).toString());
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        ingredientsField.setText("");
        recipeArea.setText("");
    }

    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Vector<String> columnNames = new Vector<>();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RecipeManagementSystem::new);
    }
}
