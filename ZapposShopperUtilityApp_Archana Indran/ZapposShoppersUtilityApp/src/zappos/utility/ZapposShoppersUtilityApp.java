package zappos.utility;

import java.awt.Color;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * A Zappos API based utility class that helps shoppers by providing possible suggestions from
 * <code>www.zappos.com</code> based on their input of the total money they want to spend against a
 * specific number of items.
 * </p>
 * @author Archana Indran
 */
public class ZapposShoppersUtilityApp extends JFrame
{
    private static final long serialVersionUID = -5757455430937014525L;

    private JPanel contentPane;
    private JTextField amountTextField;
    private JTextField itemsTextField;
    private JButton previousButton;
    private JButton nextButton;
    private JButton getGiftsButton;
    private JTextArea jTextAreaForDisplay = new JTextArea();

    private int counter = 0;
    private boolean itemsFoundInd;
    private int onlyOneItemFoundCounter = 0;
    private List<List<JSONObject>> finalProductsListForDisplay = new ArrayList<List<JSONObject>>();

    /**
     * Main method.
     */
    public static void main(String[] args)
    {
        ZapposShoppersUtilityApp utilityApp = new ZapposShoppersUtilityApp();
        utilityApp.loadZapposShoppersUtilityAppGUI();
        utilityApp.setVisible(true);
    }

    /**
     * Create the GUI for the utility and loads its contents.
     */
    private void loadZapposShoppersUtilityAppGUI()
    {
        contentPane = new JPanel();
        contentPane.setBackground(UIManager.getColor("Button.highlight"));
        contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        setForeground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 25, 600, 700);

        // Zappos logo for the GUI
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL zicon = cl.getResource("Zlogo.png");
        ImageIcon zIcon = new ImageIcon(zicon);
        setIconImage(zIcon.getImage());
        setTitle("Zappos Shoppers Utility App");

        URL zlogo = cl.getResource("zappos full.png");
        ImageIcon ic = new ImageIcon(zlogo);
        JLabel jimg = new JLabel(ic);
        contentPane.add(jimg, "wrap");

        // Gift amount
        JLabel lblGiftAmount = new JLabel("Gift Amount:   $");
        lblGiftAmount.setBounds(10, 75, 95, 14);
        contentPane.add(lblGiftAmount);

        // Number of Gifts
        JLabel lblNoOfGifts = new JLabel("Number of Gifts:");
        lblNoOfGifts.setBounds(10, 100, 124, 14);
        contentPane.add(lblNoOfGifts);

        // Text fields
        amountTextField = new JTextField();
        amountTextField.setBounds(147, 72, 58, 20);
        contentPane.add(amountTextField);
        amountTextField.setColumns(10);

        itemsTextField = new JTextField();
        itemsTextField.setBounds(147, 97, 58, 20);
        contentPane.add(itemsTextField);
        itemsTextField.setColumns(10);

        JSeparator separator = new JSeparator();
        separator.setBounds(0, 125, 590, 2);
        contentPane.add(separator);

        JLabel lblLogoLabel = new JLabel("");
        lblLogoLabel.setIcon(ic);
        lblLogoLabel.setBounds(0, 0, 143, 70);
        contentPane.add(lblLogoLabel);

        // Get Gifts button logic
        getGiftsButton = new JButton("Get Gifts!");
        getGiftsButton.setBounds(316, 66, 89, 23);
        contentPane.add(getGiftsButton);
        getGiftsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                counter = 0;
                onlyOneItemFoundCounter = 0;
                itemsFoundInd = false;
                finalProductsListForDisplay = new ArrayList<List<JSONObject>>();

                jTextAreaForDisplay.setBorder(new EtchedBorder(EtchedBorder.LOWERED, SystemColor.activeCaptionText, null));
                jTextAreaForDisplay.setBounds(10, 130, 555, 470);
                jTextAreaForDisplay.setText(null);
                jTextAreaForDisplay.setVisible(true);

                // Check first if the shopper has provided valid data for processing
                boolean validAmount = true;
                boolean validItems = true;
                String amount = amountTextField.getText();
                if(amount == null || amount.length() == 0 || amount.trim().length() == 0)
                {
                    validAmount = false;
                }
                if(validAmount && amount != null)
                {
                    double totalDollarsToSpend = 0;
                    try
                    {
                        totalDollarsToSpend = Double.parseDouble(amount);
                    }
                    catch(NumberFormatException n)
                    {
                        validAmount = false;
                    }
                    if(totalDollarsToSpend == 0)
                    {
                        validAmount = false;
                    }
                }
                String items = itemsTextField.getText();
                if(validAmount)
                {
                    if(items == null || items.length() == 0 || items.trim().length() == 0)
                    {
                        validItems = false;
                    }
                    if(validItems && items != null)
                    {
                        int totalItemsToBuy = 0;
                        try
                        {
                            totalItemsToBuy = Integer.parseInt(items);
                        }
                        catch(NumberFormatException n)
                        {
                            validItems = false;
                        }
                        if(totalItemsToBuy == 0)
                        {
                            validAmount = false;
                        }
                    }
                }

                // If input is invalid display a message
                if(!validAmount || !validItems)
                {
                    jTextAreaForDisplay.append("\nHey Zappos Shopper...We need a valid input of $$ and items to proceed." + "\n");
                    contentPane.add(jTextAreaForDisplay);
                    contentPane.revalidate();
                    contentPane.repaint();
                }
                // If input is valid continue processing
                else if(validAmount && validItems)
                {
                    double totalDollarsToSpend = Double.parseDouble(amount);
                    int totalItemsToBuy = Integer.parseInt(items);

                    // Zappos API call to get results for the shopper's input
                    Set<List<JSONObject>> finalProductSet = findAllPossibleItemsForShoppersHardEarnedDollars(totalDollarsToSpend,
                            totalItemsToBuy);
                    int finalCount = 0;
                    for(List<JSONObject> element : finalProductSet)
                    {
                        if(finalCount == 500)
                        {
                            break;
                        }
                        finalCount++;
                        finalProductsListForDisplay.add(element);
                    }

                    // Process the results from the API call and create a list for display
                    StringBuilder itemsToDisplay = getItemDataToDisplay(true, false, false);

                    if(!itemsFoundInd)
                    {
                        previousButton.setEnabled(false);
                        nextButton.setEnabled(false);

                        jTextAreaForDisplay
                                .append("\nHey Zappos Shopper..Sorry we couldn't find anything for your input. Please try again."
                                        + "\n");
                        contentPane.add(jTextAreaForDisplay);
                        contentPane.revalidate();
                        contentPane.repaint();
                    }
                    else
                    {
                        previousButton.setEnabled(true);
                        nextButton.setEnabled(true);

                        jTextAreaForDisplay
                                .setBorder(new EtchedBorder(EtchedBorder.LOWERED, SystemColor.activeCaptionText, null));
                        jTextAreaForDisplay.setBounds(10, 130, 555, 470);
                        jTextAreaForDisplay.setText(null);
                        jTextAreaForDisplay.setVisible(true);
                        jTextAreaForDisplay.append(itemsToDisplay.toString());
                        if(onlyOneItemFoundCounter == 1)
                        {
                            nextButton.setEnabled(false);
                            previousButton.setEnabled(false);
                            jTextAreaForDisplay
                                    .append("\nSorry, we could not find different combination of items..Please try a different input.");
                        }
                        contentPane.add(jTextAreaForDisplay);
                        contentPane.revalidate();
                        contentPane.repaint();
                    }
                }
            }
        });

        // Clear button logic
        JButton btnClearInput = new JButton("Clear");
        btnClearInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                amountTextField.setText(null);
                itemsTextField.setText(null);
                jTextAreaForDisplay.setText(null);
                nextButton.setEnabled(false);
                previousButton.setEnabled(false);
            }
        });
        btnClearInput.setBounds(316, 96, 89, 23);
        contentPane.add(btnClearInput);

        // Previous button logic
        previousButton = new JButton("Previous");
        previousButton.setEnabled(false);
        previousButton.setBounds(152, 625, 89, 23);
        contentPane.add(previousButton);
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                // Process the results and create a list for display
                StringBuilder itemsToDisplay = getItemDataToDisplay(false, true, false);

                if(!itemsFoundInd)
                {
                    jTextAreaForDisplay.setText(null);
                    jTextAreaForDisplay.append("\nClick Next to see items we found for your input.." + "\n");
                    contentPane.add(jTextAreaForDisplay);
                    contentPane.revalidate();
                    contentPane.repaint();
                }
                else
                {
                    jTextAreaForDisplay.setBorder(new EtchedBorder(EtchedBorder.LOWERED, SystemColor.activeCaptionText, null));
                    jTextAreaForDisplay.setBounds(10, 130, 555, 470);
                    jTextAreaForDisplay.setVisible(true);
                    jTextAreaForDisplay.setText(null);
                    jTextAreaForDisplay.setVisible(true);
                    jTextAreaForDisplay.append(itemsToDisplay.toString());
                    contentPane.add(jTextAreaForDisplay);
                    contentPane.revalidate();
                    contentPane.repaint();
                }
            }
        });

        // Next button logic
        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        nextButton.setBounds(316, 625, 89, 23);
        contentPane.add(nextButton);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                // Process the results and create a list for display
                StringBuilder itemsToDisplay = getItemDataToDisplay(false, false, true);

                if(!itemsFoundInd)
                {
                    jTextAreaForDisplay.setText(null);
                    jTextAreaForDisplay.append("\nClick Previous to see other items we found for your input." + "\n");
                    contentPane.add(jTextAreaForDisplay);
                    contentPane.revalidate();
                    contentPane.repaint();
                }
                else
                {
                    jTextAreaForDisplay.setBorder(new EtchedBorder(EtchedBorder.LOWERED, SystemColor.activeCaptionText, null));
                    jTextAreaForDisplay.setBounds(10, 130, 555, 470);
                    jTextAreaForDisplay.setVisible(true);
                    jTextAreaForDisplay.setText(null);
                    jTextAreaForDisplay.setVisible(true);
                    jTextAreaForDisplay.append(itemsToDisplay.toString());
                    contentPane.add(jTextAreaForDisplay);
                    contentPane.revalidate();
                    contentPane.repaint();
                }
            }
        });
    }

    /**
     * A method that retrieves the details about the finalProductsListForDisplay to display to the
     * shopper.
     * @param getFirstItem an indicator to get the first item
     * @param getPreviousItem an indicator to get the previous item
     * @param getNextItem an indicator to get the next item
     * @return a StringBuilder that contains details about the products for display
     */
    private StringBuilder getItemDataToDisplay(boolean getFirstItem, boolean getPreviousItem, boolean getNextItem)
    {
        StringBuilder itemsToDisplay = new StringBuilder();
        // Get Items button display list
        if(getFirstItem)
        {
            try
            {
                List<JSONObject> products = finalProductsListForDisplay.get(0);
                for(JSONObject product : products)
                {
                    String productData = createDataToDisplay(product);
                    itemsToDisplay.append(productData);

                    itemsFoundInd = true;
                    onlyOneItemFoundCounter++;
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                itemsFoundInd = false;
            }
        }
        // Previous button display list
        if(getPreviousItem)
        {
            List<JSONObject> products = new ArrayList<JSONObject>();
            try
            {
                if(onlyOneItemFoundCounter == 1)
                {
                    products = finalProductsListForDisplay.get(0);
                }
                else
                {
                    counter--;
                    products = finalProductsListForDisplay.get(counter);
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                itemsFoundInd = false;
                counter++;
            }
            for(JSONObject product : products)
            {
                String productData = createDataToDisplay(product);
                itemsToDisplay.append(productData);

                itemsFoundInd = true;
            }
        }
        // Next button display list
        if(getNextItem)
        {
            List<JSONObject> products = new ArrayList<JSONObject>();
            try
            {
                if(onlyOneItemFoundCounter == 1)
                {
                    products = finalProductsListForDisplay.get(0);
                }
                else
                {
                    counter++;
                    products = finalProductsListForDisplay.get(counter);
                }
            }
            catch(IndexOutOfBoundsException e)
            {
                itemsFoundInd = false;
                counter--;
            }
            for(JSONObject product : products)
            {
                String productData = createDataToDisplay(product);
                itemsToDisplay.append(productData);

                itemsFoundInd = true;
            }
        }
        return itemsToDisplay;
    }

    /**
     * Creates a string based on the values retrieved from the JSONObject.
     * @param product the JSONObject
     * @return a string with the product details
     */
    private String createDataToDisplay(JSONObject product)
    {
        String price = product.getString("price");
        String productName = product.getString("productName");
        String productUrl = product.getString("productUrl");
        String brandName = product.getString("brandName");

        String productData = "PRODUCT NAME: " + productName + " " + "BRAND: " + brandName + " " + "PRICE: " + price + "\n"
                + "PRODUCT URL: " + productUrl + " " + "\n\n";
        return productData;
    }

    /**
     * A method that finds all possible items that shoppers can buy based on the input of
     * <b>totalDollarsToSpend</b> and <b>totalItemsToBuy</b>.
     * @param totalDollarsToSpend the total dollars the shopper wants to spend
     * @param totalItemsToBuy the total number of items the shopper wants to buy
     */
    private Set<List<JSONObject>> findAllPossibleItemsForShoppersHardEarnedDollars(double totalDollarsToSpend, int totalItemsToBuy)
    {
        double appxDollarPerItem = totalDollarsToSpend / totalItemsToBuy;
        String priceFacet = "";
        String sortBy = null;
        // Determine priceFacet and sortBy(if needed) based on appxDollarPerItem
        if(appxDollarPerItem < 50)
        {
            priceFacet = "$50.00 and Under";
            if(appxDollarPerItem < 25)
            {
                sortBy = "asc";
            }
        }
        else if(appxDollarPerItem < 100)
        {
            priceFacet = "$100.00 and Under";
        }
        else if(appxDollarPerItem < 200)
        {
            priceFacet = "$200.00 and Under";
        }
        else if(appxDollarPerItem > 200)
        {
            priceFacet = "$200.00 and Over";
            if(appxDollarPerItem > 999)
            {
                sortBy = "desc";
            }
        }

        // Adding some categories to query from
        List<String> categories = new ArrayList<String>(3);
        categories.add("women");
        categories.add("men");
        categories.add("baby");

        // Use the Zappos API Search API to query on the above 3 categories
        List<String> zapposAPISearchQueryresults = new ArrayList<String>();
        String apiKey = "52ddafbe3ee659bad97fcce7c53592916a6bfd73";
        RestTemplate restTemplate = new RestTemplate();
        for(String category : categories)
        {
            String searchUrl = null;
            String zapposAPISearchQueryresult = null;
            // Check if we need to include sorting in the query which is based
            // on appxDollarPerItem
            if(sortBy != null)
            {
                searchUrl = "http://api.zappos.com/Search/term/" + category
                        + "?filters={priceFacet}&limit=100&sort={sort}&key={key}";
                zapposAPISearchQueryresult = restTemplate.getForObject(searchUrl, String.class, "{\"priceFacet\":[\""
                        + priceFacet + "\"]}", "{\"price\":\"" + sortBy + "\"}", apiKey);
            }
            // Retrieve search results without including sort
            else
            {
                searchUrl = "http://api.zappos.com/Search/term/" + category + "?filters={priceFacet}&limit=100&key={key}";
                zapposAPISearchQueryresult = restTemplate.getForObject(searchUrl, String.class, "{\"priceFacet\":[\""
                        + priceFacet + "\"]}", apiKey);
            }
            zapposAPISearchQueryresults.add(zapposAPISearchQueryresult);
        }

        // Create empty lists for display that will contain possible product
        // combinations
        List<Set<JSONObject>> finalItemsListForPermutation = new ArrayList<Set<JSONObject>>(totalItemsToBuy);

        // Add empty lists equivalent to totalItemsToBuy to add items which
        // qualify to it
        for(int i = 0; i < totalItemsToBuy; i++)
        {
            Set<JSONObject> productLists = new HashSet<JSONObject>();
            finalItemsListForPermutation.add(productLists);
        }

        int itemsFound = 0;
        Iterator<String> zapposAPIQueryResultsItr = zapposAPISearchQueryresults.iterator();
        // Process the search query result from Zappos API for men category
        if(zapposAPIQueryResultsItr.hasNext())
        {
            String zapposAPIQueryResult = zapposAPIQueryResultsItr.next();
            itemsFound = processZapposQueryResults(appxDollarPerItem, totalItemsToBuy, zapposAPIQueryResult,
                    finalItemsListForPermutation);
        }
        if(totalItemsToBuy > 10 && itemsFound < totalItemsToBuy)
        {
            // Process the search query result from Zappos API for women
            // category
            if(zapposAPIQueryResultsItr.hasNext())
            {
                String zapposAPIQueryResult = zapposAPIQueryResultsItr.next();
                itemsFound = processZapposQueryResults(appxDollarPerItem, totalItemsToBuy, zapposAPIQueryResult,
                        finalItemsListForPermutation);
            }

            // Process the search query result from Zappos API for baby category
            if(zapposAPIQueryResultsItr.hasNext())
            {
                String zapposAPIQueryResult = zapposAPIQueryResultsItr.next();
                itemsFound = processZapposQueryResults(appxDollarPerItem, totalItemsToBuy, zapposAPIQueryResult,
                        finalItemsListForPermutation);
            }
        }

        List<Set<JSONObject>> finalItemsSetForDisplay = new ArrayList<Set<JSONObject>>();
        List<Set<JSONObject>> tempFinalItemsListForDisplay = new ArrayList<Set<JSONObject>>(finalItemsListForPermutation);
        for(Set<JSONObject> finalItemsList : tempFinalItemsListForDisplay)
        {
            if(finalItemsList.size() == 0)
            {
                // Clean up lists to remove the ones which were not populated
                finalItemsListForPermutation.remove(finalItemsList);
            }
            else
            {
                // Add the valid sets for final display
                finalItemsSetForDisplay.add(finalItemsList);
            }
        }

        Set<List<JSONObject>> finalProductSet = new HashSet<List<JSONObject>>();
        try
        {
            finalProductSet = com.google.common.collect.Sets.cartesianProduct(finalItemsSetForDisplay);
        }
        catch(IllegalArgumentException e)
        {

        }
        return finalProductSet;
    }

    /**
     * A method that processes the results from the Zappos API call and determines if the item can
     * be considered for display based on the shoppers requirements.
     * @param totalDollarsToSpend the total dollars the shopper wants to spend
     * @param appxDollarPerItem the approximate dollars that can be spent on each item
     * @param zapposAPIQueryResult the result from the Zappos API call in the form of a string
     * @param itemsListForPermutation a list of JSONObject lists which contains all possible items
     *            that qualifies for display
     * @return the total number of items found
     */
    private int processZapposQueryResults(double appxDollarPerItem, int totalItemsToBuy, String zapposAPIQueryResult, List<Set<JSONObject>> itemsListForPermutation)
    {
        int itemsFound = 0;
        int count = 0;
        // Item price range from $25 - $1000 from the $appxDollarPerItem
        int productPriceRange = 25;
        // Set to keep track of all productIds to avoid duplicates
        Set<String> productSetToAvoidDuplicates = new HashSet<String>();

        int itemsListSize = 0;
        if(totalItemsToBuy > 10)
        {
            itemsListSize = 1;
        }
        else
        {
            itemsListSize = 3;
        }
        NumberFormat format = NumberFormat.getCurrencyInstance();
        while(productPriceRange < 1001)
        {
            JSONObject queryResult = new JSONObject(zapposAPIQueryResult);
            // Read the results from the query
            JSONArray queryResultsArray = queryResult.getJSONArray("results");
            for(int i = 0; i < queryResultsArray.length(); i++)
            {
                JSONObject product = queryResultsArray.getJSONObject(i);
                String price = product.getString("price");
                Number productPriceNumber = null;
                try
                {
                    productPriceNumber = format.parse(price);
                }
                catch(ParseException e)
                {
                    System.out.println("Error while parsing an item with price:" + price);
                }
                if(productPriceNumber != null)
                {
                    String productId = product.getString("productId");
                    if(!productSetToAvoidDuplicates.contains(productId))
                    {
                        double productPrice = productPriceNumber.doubleValue();
                        // Check if the item price is the range of $25 to $100 based on
                        // appxDollarPerItem
                        if(productPrice <= appxDollarPerItem && productPrice >= (appxDollarPerItem - productPriceRange))
                        {
                            productSetToAvoidDuplicates.add(productId);
                            // Add items to the list for permutation, permutation list size should
                            // be equal to the totalItemsToBuy, so reinitialize the count variable
                            // every time we reach the list size
                            Set<JSONObject> itemsList = itemsListForPermutation.get(count);
                            if(itemsList.size() <= itemsListSize)
                            {
                                if(count == totalItemsToBuy - 1)
                                {
                                    itemsList.add(product);
                                    count = 0;
                                }
                                else
                                {
                                    itemsList.add(product);
                                    count++;
                                }
                                itemsFound++;
                            }
                            else
                            {
                                count = count == totalItemsToBuy - 1 ? 0 : count++;
                            }
                        }
                    }
                }
            }
            // Increase our range to find more items
            productPriceRange *= 2;
        }
        return itemsFound;
    }
}