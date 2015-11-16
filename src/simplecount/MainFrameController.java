package simplecount;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author da-sil_l
 */
public class MainFrameController implements Initializable {

    @FXML
    private TextField resultArea;
    @FXML
    private Label lastEquationLabel;

    private String lastEquation;
    private boolean shouldClear = false;
    private DecimalFormat decimalFormat;
    private static final String OP_REGEX = "(?<=[-+*/%()])|(?=[-+*/%()])";
    private static final int MAX_NUMBERS = 20;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        decimalFormat = new DecimalFormat("#.#");
        decimalFormat.setMaximumFractionDigits(5);
    }

    /**
     * Check the source object nature and returns its text accordingly
     *
     * @param obj the source
     * @return the source's text
     */
    private String getObjectString(Object obj) {
        String text = "";
        if (obj instanceof MenuItem) {
            MenuItem mi = (MenuItem) obj;
            text = mi.getText();
        } else if (obj instanceof Button) {
            Button btn = (Button) obj;
            text = btn.getText();
        }
        return text;
    }

    private Boolean isOp(char text) {
        return (text == '+'
                || text == '-'
                || text == '*'
                || text == '/'
                || text == '%');
    }

    private Boolean isOp(String text) {
        return ("+".equals(text)
                || "-".equals(text)
                || "*".equals(text)
                || "/".equals(text)
                || "%".equals(text));
    }

    private void dealWithNegativeNumbers(ArrayList<String> list) {
        if (list.size() < 1) {
            return;
        }
        int i = 0;
        while (i < list.size() - 1) {
            if (list.get(i).equals("-") && i > 0 && i < list.size() - 2) {
                if (isOp(list.get(i - 1)) && !isOp(list.get(i + 1))) {
                    list.set(i + 1, "-" + list.get(i + 1));
                    list.remove(i);
                    i = 0;
                }
            }
            ++i;
        }
    }

    /**
     * Takes the resultArea string and format it
     *
     * @return an arrayList with every module of the equations ex: 1+2*3 becomes
     * {1, +, 2, *, 3}
     */
    private ArrayList<String> getItemList() {
        String[] items = (resultArea.getText().split(OP_REGEX));
        ArrayList<String> itemList = new ArrayList<>();
        Collections.addAll(itemList, items);
        return itemList;
    }

    /**
     * Finds and returns a String in the list by its index
     *
     * @param index index of the item you want
     * @param last if true, returns the last element, ignoring index
     * @return the string correspondig to the index
     */
    private String getItemAt(int index, Boolean last) {
        String item = "0";
        try {
            ArrayList<String> itemList = getItemList();
            if (itemList.isEmpty()) {
                item = "0";
            } else if (last) {
                item = itemList.get(itemList.size() - 1);
            } else {
                item = itemList.get(index);
            }
        } catch (Exception e) {
        }
        return item;
    }

    /**
     * Removes trailing operand if it exists
     *
     * @param list the list to trim
     */
    private void trimList(ArrayList<String> list) {
        String end = list.get(list.size() - 1);
        if (isOp(end)) {
            list.remove(list.size() - 1);
        }
    }

    /**
     * Writes "text" to the textArea after checking its validity
     *
     * @param text text to write to the textArea
     */
    private void editText(String text) {
        if (text.length() > MAX_NUMBERS) {
            text = text.substring(0, MAX_NUMBERS);
        }
        if (text.contains(",")) {
            text = text.replaceAll(",", ".");
        }
        resultArea.setText(text);
    }

    /**
     * Finds the first occurence of prioritized operator
     * @param list the list
     * @return the index of that operator or 1
     */
    private int operatorPriorityFinder(ArrayList<String> list) {
        int index0 = list.indexOf("*") != -1 ? list.indexOf("*") : Integer.MAX_VALUE;
        int index1 = list.indexOf("/") != -1 ? list.indexOf("/") : Integer.MAX_VALUE;
        int index2 = list.indexOf("%") != -1 ? list.indexOf("%") : Integer.MAX_VALUE;
        if (index0 == Integer.MAX_VALUE && index1 == Integer.MAX_VALUE && index2 == Integer.MAX_VALUE)
            return 1;
        
        return Math.min(Math.min(index0, index1), index2);
    }

    /**
     * Computes the equation found in the textArea
     *
     * @return a String corresponding to the result, formatted as "#.#*"
     */
    private String computeResult() {
        lastEquation = resultArea.getText();
        ArrayList<String> itemList = getItemList();
        trimList(itemList);
        Double result = 0.0;
        while (itemList.size() > 2) {
            int index = operatorPriorityFinder(itemList);
            double left = 0;
            double right = 0;
            String op = "";
            try {
                left = Double.parseDouble(itemList.get(index - 1));
                op = itemList.get(index);
                right = Double.parseDouble(itemList.get(index + 1));
            } catch (Exception e) {
                resultArea.clear();
                return "";
            }
            switch (op) {
                case "+":
                    result = left + right;
                    break;
                case "-":
                    result = left - right;
                    break;
                case "*":
                    result = left * right;
                    break;
                case "/":
                    result = left / right;
                    break;
                case "%":
                    result = left % right;
                    break;
                default:
                    break;
            }
            itemList.remove(index - 1);
            itemList.remove(index - 1);
            itemList.remove(index - 1);
            itemList.add(index - 1, result.toString());
        }
        shouldClear = true; // operation has been computed, screen should be cleared after that
        lastEquationLabel.setText(lastEquation);
        return decimalFormat.format(result);
    }

    /**
     * Called when an operation button is pressed
     *
     * @param event event generated by the button pressed
     */
    @FXML
    public void handleOperations(ActionEvent event) {
        String text = getObjectString(event.getSource());
        if ("←".equals(text) && resultArea.getText().length() > 0) {
            editText(resultArea.getText().substring(0, resultArea.getText().length() - 1));
        } else if (isOp(text.charAt(0)) && resultArea.getText().length() > 0) {
            if (resultArea.getText().length() > 1 && isOp(getItemAt(0, Boolean.TRUE))) {
                editText(resultArea.getText().substring(0, resultArea.getText().length() - 1) + text);
            } else {
                editText(resultArea.getText() + text);
            }
            shouldClear = false; // user wants to do ops with the previous result, don't clear it
        } else if ("=".equals(text)) {
            editText(computeResult());
        } else if ("C".equals(text)) {
            resultArea.clear();
        }
    }

    /**
     * Called when a special operation is called (lg, ex, cos, tan...)
     *
     * @param event event generated by the button pressed
     */
    @FXML
    public void handleSpecialOperations(ActionEvent event) {
        String op = ((Button) event.getSource()).getText();
        String numberAsString;
        Double number = 0.0;
        Double result = 0.0;

        lastEquation = resultArea.getText();
        if (getItemList().size() > 1) {
            numberAsString = computeResult();
        } else {
            numberAsString = getItemAt(0, false);
        }
        try {
            number = Double.parseDouble(numberAsString);
        } catch (Exception e) {
            return;
        }

        switch (op) {
            case "lg":
                result = Math.log(number);
                break;
            case "ex":
                result = Math.exp(number);
                break;
            case "cos":
                result = Math.cos(number);
                break;
            case "tan":
                result = Math.tan(number);
                break;
            default:
                break;
        }
        lastEquationLabel.setText(op + "(" + (lastEquationLabel.getText().equals("") ? lastEquation : lastEquationLabel.getText()) + ")");
        editText(decimalFormat.format(result));
        shouldClear = true;
    }

    /**
     * Called when a numpad button is pressed
     *
     * @param event event generated by the button pressed
     */
    @FXML
    public void handleNumPad(ActionEvent event) {
        if (shouldClear) {
            resultArea.clear();
        }
        String text = getObjectString(event.getSource());
        if (".".equals(text) && getItemAt(0, Boolean.TRUE).endsWith(".")) {
            return;
        }
        shouldClear = false;
        editText(resultArea.getText() + text);
    }

    @FXML
    public void handleKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            resultArea.clear();
            lastEquationLabel.setText("");
            lastEquation = "";
        }
    }
}
