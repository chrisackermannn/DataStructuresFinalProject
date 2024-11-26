import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class InventoryManagementSystem extends JFrame {
    private LinkedList<String> inventoryList;
    private JList<String> jList;
    private JButton addButton, undoButton;
    private JTextField skuField, nameField, quantityField, minField, maxField;
    private String[][] inventoryData;
    private HashMap<String, Integer> skuIndexes;
    private Queue<String> skuQueue;
    private static Stack<String> skuStack;
    private BinaryTree binaryTree;

    public InventoryManagementSystem(Stack<String> skuStack) {
        this.skuStack = skuStack;
        setTitle("Inventory Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inventoryList = new LinkedList<>();
        inventoryList.add("14022 - Hammer - Quantity: 32");
        inventoryList.add("14050 - Nail - Quantity: 229");
        inventoryList.add("14077 - Ladder - Quantity: 15");

        inventoryData = new String[][]{{"14022", "Hammer", "32"}, {"14050", "Nail", "229"}, {"14077", "Ladder", "15"}};

        skuIndexes = new HashMap<>();
        skuIndexes.put("14022", 0);
        skuIndexes.put("14050", 1);
        skuIndexes.put("14077", 2);

        skuQueue = new LinkedList<>();
        binaryTree = new BinaryTree();

        skuField = new JTextField(10);
        nameField = new JTextField(10);
        quantityField = new JTextField(10);
        minField = new JTextField(10);
        maxField = new JTextField(10);
        addButton = new JButton("Add/Update Item");
        undoButton = new JButton("Undo");

        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Inventory"));
        jList = new JList<>(inventoryList.toArray(new String[0]));
        inventoryPanel.add(new JScrollPane(jList), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        inputPanel.add(new JLabel("SKU:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(skuField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(quantityField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Min:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(minField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        inputPanel.add(new JLabel("Max:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(maxField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        inputPanel.add(addButton, gbc);
        gbc.gridy = 6;
        inputPanel.add(undoButton, gbc);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(inventoryPanel, BorderLayout.CENTER);
        contentPane.add(inputPanel, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String sku = skuField.getText();
                    String name = nameField.getText();
                    int quantity = Integer.parseInt(quantityField.getText());
                    int min = Integer.parseInt(minField.getText());
                    int max = Integer.parseInt(maxField.getText());

                    if (quantity < 0 || min < 0 || max < 0) {
                        JOptionPane.showMessageDialog(null, "Quantity, Min, and Max must be non-negative integers.");
                        return;
                    }
                    if (min > max) {
                        JOptionPane.showMessageDialog(null, "Min must be less than or equal to Max.");
                        return;
                    }

                    boolean skuExists = skuIndexes.containsKey(sku);

                    String newItem;
                    if (quantity < min) {
                        String orderNumber = generateFakeOrderNumber();
                        newItem = sku + " - " + name + " - Quantity: " + quantity + " ARRIVING 4/25 (Order #" + orderNumber + ")";
                    } else {
                        newItem = sku + " - " + name + " - Quantity: " + quantity;
                    }

                    skuStack.push(sku);
                    manageSKUQueue(sku);

                    if (skuExists) {
                        int index = skuIndexes.get(sku);
                        inventoryList.set(index, newItem);
                        inventoryData[index] = new String[]{sku, name, Integer.toString(quantity)};
                    } else {
                        inventoryList.add(newItem);
                        inventoryData = Arrays.copyOf(inventoryData, inventoryData.length + 1);
                        inventoryData[inventoryData.length - 1] = new String[]{sku, name, Integer.toString(quantity)};
                        skuIndexes.put(sku, inventoryData.length - 1);
                    }

                    jList.setListData(inventoryList.toArray(new String[0]));

                    skuField.setText("");
                    nameField.setText("");
                    quantityField.setText("");
                    minField.setText("");
                    maxField.setText("");

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter valid integer values for Quantity, Min, and Max.");
                }
            }
        });

        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!skuStack.isEmpty()) {
                    String skuToRemove = skuStack.pop();
                    if (skuIndexes.containsKey(skuToRemove)) {
                        int indexToRemove = skuIndexes.get(skuToRemove);
                        inventoryList.remove(indexToRemove);
                        inventoryData = removeElementFromArray(inventoryData, indexToRemove);
                        updateSkuIndexes();
                        jList.setListData(inventoryList.toArray(new String[0]));
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Nothing to undo.");
                }
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sku = skuField.getText();
                String name = nameField.getText();
                int quantity = Integer.parseInt(quantityField.getText());
                binaryTree.insert(sku, name, quantity);
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bubbleSort();
            }
        });
    }

    private String[][] removeElementFromArray(String[][] array, int indexToRemove) {
        if (indexToRemove < 0 || indexToRemove >= array.length) {
            return array;
        }
        String[][] newArray = new String[array.length - 1][array[0].length];
        for (int i = 0, k = 0; i < array.length; i++) {
            if (i == indexToRemove) {
                continue;
            }
            newArray[k++] = array[i];
        }
        return newArray;
    }

    private void updateSkuIndexes() {
        skuIndexes.clear();
        for (int i = 0; i < inventoryData.length; i++) {
            skuIndexes.put(inventoryData[i][0], i);
        }
    }

    private void manageSKUQueue(String sku) {
        if (skuQueue.size() == 5) {
            skuQueue.poll();
        }
        skuQueue.offer(sku);
    }

    private String generateFakeOrderNumber() {
        return "FAKE123";
    }

    class BinaryTree {
        TreeNode root;

        class TreeNode {
            String sku;
            String name;
            int quantity;
            TreeNode left, right;

            public TreeNode(String sku, String name, int quantity) {
                this.sku = sku;
                this.name = name;
                this.quantity = quantity;
                left = null;
                right = null;
            }
        }

        public void insert(String sku, String name, int quantity) {
            root = insertRec(root, sku, name, quantity);
        }

        private TreeNode insertRec(TreeNode root, String sku, String name, int quantity) {
            if (root == null) {
                root = new TreeNode(sku, name, quantity);
                return root;
            }
            if (Integer.parseInt(sku) < Integer.parseInt(root.sku)) {
                root.left = insertRec(root.left, sku, name, quantity);
            } else if (Integer.parseInt(sku) > Integer.parseInt(root.sku)) {
                root.right = insertRec(root.right, sku, name, quantity);
            }
            return root;
        }
    }

    private void bubbleSort() {
        for (int i = 0; i < inventoryData.length - 1; i++) {
            for (int j = 0; j < inventoryData.length - i - 1; j++) {
                if (Integer.parseInt(inventoryData[j][0]) > Integer.parseInt(inventoryData[j + 1][0])) {
                    String[] temp = inventoryData[j];
                    inventoryData[j] = inventoryData[j + 1];
                    inventoryData[j + 1] = temp;
                }
            }
        }
    }

    public static void main(String[] args) {
        Stack<String> skuStack = new Stack<>();
        skuStack.push("Initial");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new InventoryManagementSystem(skuStack).setVisible(true);
            }
        });
    }
}
