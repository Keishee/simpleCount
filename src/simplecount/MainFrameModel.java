package simplecount;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import javafx.scene.control.Label;

/**
 * Model
 * @author da-sil_l
 */
public class MainFrameModel extends Observable {

    private static final String OP_REGEX = "(?<=[-+*/%()])|(?=[-+*/%()])";
    private static final int MAX_NUMBERS = 20;
    private DecimalFormat decimalFormat;

    MainFrameModel() {
        decimalFormat = new DecimalFormat("#.#");
        decimalFormat.setMaximumFractionDigits(8);
    }

    /**
     * Takes the resultArea string and format it
     *
     * @return an arrayList with every module of the equations ex: 1+2*3 becomes
     * {1, +, 2, *, 3}
     */
    private ArrayList<String> getItemList(String str) {
        String[] items = (str.split(OP_REGEX));
        ArrayList<String> itemList = new ArrayList<>();
        Collections.addAll(itemList, items);
        return itemList;
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
     * Finds the first occurence of prioritized operator
     *
     * @param list the list
     * @return the index of that operator or 1
     */
    private int operatorPriorityFinder(ArrayList<String> list) {
        int index0 = list.indexOf("*") != -1 ? list.indexOf("*") : Integer.MAX_VALUE;
        int index1 = list.indexOf("/") != -1 ? list.indexOf("/") : Integer.MAX_VALUE;
        int index2 = list.indexOf("%") != -1 ? list.indexOf("%") : Integer.MAX_VALUE;
        if (index0 == Integer.MAX_VALUE && index1 == Integer.MAX_VALUE && index2 == Integer.MAX_VALUE) {
            return 1;
        }

        return Math.min(Math.min(index0, index1), index2);
    }

    /**
     * Computes the equation found in the textArea
     *
     * @param equation the equation
     * @param shouldDisplay decides whether the result should be displayed
     * @return a String corresponding to the result, formatted as "#.#*"
     */
    public String computeResult(String equation, boolean shouldDisplay) {
        ArrayList<String> itemList = getItemList(equation);
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
                setText("");
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
        if (shouldDisplay) {
            notifyObservers(true);
            notifyObservers(new Label(equation));
            notifyObservers(decimalFormat.format(result));
        }
        return decimalFormat.format(result);
    }

    /**
     * Finds and returns a String in the list by its index
     *
     * @param index index of the item you want
     * @param last if true, returns the last element, ignoring index
     * @return the string correspondig to the index
     */
    private String getItemAt(int index, String text, Boolean last) {
        String item = "0";
        try {
            ArrayList<String> itemList = getItemList(text);
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
     * Erases last character if possible
     * @param displayedText 
     */
    public void returnKey(String displayedText) {
        if (displayedText.length() > 0) {
            notifyObservers(displayedText.substring(0, displayedText.length() - 1));
        }
    }

    /**
     * Add an operator to the equation and notify views
     * @param op
     * @param displayedText 
     */
    public void addOperator(String op, String displayedText) {
        if (displayedText.length() > 0) {
            if (displayedText.length() > 1 && isOp(getItemAt(0, displayedText, Boolean.TRUE))) {
                setText(displayedText.substring(0, displayedText.length() - 1) + op);
            } else {
                setText(displayedText + op);
            }
            notifyObservers(false);
        }
    }

    /**
     * Check validity of text and notify views
     * @param text 
     * @param displayedText 
     */
    public void addNumPad(String text, String displayedText) {
        if (".".equals(text) && getItemAt(0, displayedText, Boolean.TRUE).endsWith(".")) {
            return;
        }
        notifyObservers(false);
        setText(displayedText + text);
    }

    /**
     * Compute results for special operatos (log, exp..) and notify views
     * @param text the operator
     * @param displayedText 
     */
    public void computeSpecialOperation(String text, String displayedText) {
        String numberAsString = "";
        Double number = 0.0;
        Double result = 0.0;
        if (getItemList(displayedText).size() > 1) {
            numberAsString = computeResult(displayedText, false);
        } else {
            numberAsString = getItemAt(0, displayedText, false);
        }
        try {
            number = Double.parseDouble(numberAsString);
        } catch (Exception e) {
            return;
        }
        switch (text) {
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
            case "sin":
                result = Math.sin(number);
                break;
            case "v":
                result = Math.sqrt(number);
                break;
            case "²":
                result = Math.pow(number, 2);
                break;
            default:
                break;
        }
        notifyObservers(new Label(text + "(" + (displayedText) + ")"));
        setText(decimalFormat.format(result));
        notifyObservers(true);
    }

    /**
     * Writes "text" to the textArea after checking its validity
     *
     * @param text text to write to the textArea
     */
    public void setText(String text) {
        if (text.length() > MAX_NUMBERS) {
            text = text.substring(0, MAX_NUMBERS);
        }
        if (text.contains(",")) {
            text = text.replaceAll(",", ".");
        }
        notifyObservers(text);
    }

    @Override
    public void notifyObservers(Object arg) {
        setChanged();
        super.notifyObservers(arg);
    }

}
