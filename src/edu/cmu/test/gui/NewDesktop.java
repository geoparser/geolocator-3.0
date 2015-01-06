package edu.cmu.test.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import edu.cmu.geolocator.GlobalParam;
import edu.cmu.geolocator.Tag;
import edu.cmu.geolocator.coder.CoderFactory;
import edu.cmu.geolocator.io.GetReader;
import edu.cmu.geolocator.io.GetWriter;
import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.LocGroupFeatures;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.parser.ParserFactory;

public class NewDesktop extends JPanel implements ActionListener, ItemListener {

  public static String newline = "\n";

  public static String copyright = "© 2012-2014 CMU LTI All Right Reserved";

  public static String desc = "CMU GeoLocator v3.0"
          + newline
          + newline
          + "Function: geoparsing and geocoding tweets."
          + newline
          + "Input: tweet JSON file, one tweet JSON per line"
          + newline
          + "Output: An line-aligned file corresponding to input file. "
          + newline
          + "Each line contains the recognized locations extracted from corresponding line in input file."
          + newline
          + " Output format is:"
          + newline
          + "[location] /whitespace ...[location] /tab [location coordinates]...whitespace... [location coordinates]"
          + newline + newline + "How to use: " + newline + "1.Open a file by pressing Open, "
          + newline + "2.Choose save file by pressing Save, " + newline
          + "3.Choose the gazetteer index that is created (that you created with indexer.jar). "
          + newline + "4.click run to tag the data." + newline
          + "The progress will be shown on console";

  JButton openButton, saveButton, runButton, palseButton, resumeButton, openIndexButton;

  JCheckBox tpButton, bdButton, stButton, abButton;

  JTextArea log;

  JFileChooser fc;

  File inputfile, outputfile, indexfile;

  BufferedWriter bw;

  BufferedReader br;

  int linecount = 0;

  HashMap<String, Boolean> tagFilter;

  public NewDesktop() {
    super(new BorderLayout());

    // Create the log first, because the action listeners
    // need to refer to it.
    log = new JTextArea(5, 20);
    log.setMargin(new Insets(5, 5, 5, 5));
    log.setEditable(false);
    log.append(desc + newline + newline + copyright + newline + newline);

    // initilize the tag Filter here
    tagFilter = new HashMap<String, Boolean>();
    tagFilter.put("tp", true);
    tagFilter.put("bd", true);
    tagFilter.put("st", true);
    tagFilter.put("ab", true);

    JScrollPane logScrollPane = new JScrollPane(log);

    // Create a file chooser
    fc = new JFileChooser();

    openButton = new JButton("Step 1:\nOpen a JSON tweet File...");
    openButton.addActionListener(this);

    saveButton = new JButton("Step 2:\nChoose save file path...");
    saveButton.addActionListener(this);

    openIndexButton = new JButton("Step 3:\nOpen the Gazetteer Index ...");
    openIndexButton.addActionListener(this);

    runButton = new JButton("Step 5:\nRun");
    runButton.addActionListener(this);

    // create checkbox to select the output result type

    // Create the check boxes.
    tpButton = new JCheckBox("toponym");
    tpButton.setMnemonic(KeyEvent.VK_T);
    tpButton.setSelected(true);
    tpButton.addItemListener(this);

    bdButton = new JCheckBox("building");
    bdButton.setMnemonic(KeyEvent.VK_B);
    bdButton.setSelected(true);
    bdButton.addItemListener(this);

    stButton = new JCheckBox("street");
    stButton.setMnemonic(KeyEvent.VK_S);
    stButton.setSelected(true);
    stButton.addItemListener(this);

    abButton = new JCheckBox("abbreviation");
    abButton.setMnemonic(KeyEvent.VK_A);
    abButton.setSelected(true);
    abButton.addItemListener(this);

    // For layout purposes, put the buttons in a separate panel
    JPanel checkPanel = new JPanel(new GridLayout(0, 1));
    checkPanel.add(tpButton);
    checkPanel.add(bdButton);
    checkPanel.add(stButton);
    checkPanel.add(abButton);

    JPanel buttonPanel = new JPanel(new GridLayout(1, 0)); // use FlowLayout
    buttonPanel.add(openButton);
    buttonPanel.add(saveButton);
    buttonPanel.add(openIndexButton);
    buttonPanel.add(new JTextArea("Step 4:\nChoose output location types:"));
    buttonPanel.add(checkPanel);
    buttonPanel.add(runButton);

    // Add the buttons and the log to this panel.
    add(buttonPanel, BorderLayout.PAGE_START);
    add(logScrollPane, BorderLayout.CENTER);

    // Add the initialization of geolocator
  }

  /**
   * Fill out the tag filter based on the radio selection
   * 
   * @param e
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    Object source = e.getItemSelectable();
    if (e.getStateChange() == ItemEvent.SELECTED) {
      if (source == tpButton) {
        tagFilter.put("tp", true);
      } else if (source == bdButton) {
        tagFilter.put("bd", true);
      } else if (source == stButton) {
        tagFilter.put("st", true);
      } else if (source == abButton) {
        tagFilter.put("ab", true);
      }
    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
      if (source == tpButton) {
        tagFilter.put("tp", false);
      } else if (source == bdButton) {
        tagFilter.put("bd", false);
      } else if (source == stButton) {
        tagFilter.put("st", false);
      } else if (source == abButton) {
        tagFilter.put("ab", false);
      }
    }
    log.append(tagFilter.toString());
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    // Handle open button action.
    if (e.getSource() == this.openButton) {
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnVal = fc.showOpenDialog(NewDesktop.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        inputfile = fc.getSelectedFile();
        linecount = 0;
        // This is where a real application would open the file.
        log.append("Opening: " + inputfile.getName() + "." + newline);
      } else {
        log.append("Open command cancelled by user." + newline);
      }
      log.setCaretPosition(log.getDocument().getLength());

      // Handle save button action.
    } else if (e.getSource() == saveButton) {
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int returnVal = fc.showSaveDialog(NewDesktop.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        outputfile = fc.getSelectedFile();
        linecount = 0;
        // This is where a real application would save the file.
        log.append("Saving: " + outputfile.getName() + "." + newline);
      } else {
        log.append("Save command cancelled by user." + newline);
      }
      log.setCaretPosition(log.getDocument().getLength());
    } else if (e.getSource() == openIndexButton) {
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int returnVal = fc.showOpenDialog(NewDesktop.this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        indexfile = fc.getSelectedFile();
        // This is where a real application would save the file.
        log.append("Opening Index: " + indexfile.getName() + "." + newline);
      } else {
        log.append("Save command cancelled by user." + newline);
      }
      log.setCaretPosition(log.getDocument().getLength());

      // set the index file path to indexfile.
      GlobalParam.setGazIndex(indexfile.getPath());
    } else if (e.getSource() == runButton) {
      try {
        run();
        log.setCaretPosition(log.getDocument().getLength());
      } catch (IOException e1) {
        System.err.println("Runnning Error occured.");
      }
    }
  }

  private void run() throws IOException {
    HashSet<String> filter = Tag.getFilter(tagFilter);
    System.out.println("Run output:" + inputfile.getAbsolutePath());
    System.out.println("Run output:" + outputfile.getAbsolutePath());

    try {
      br = GetReader.getUTF8FileReader(inputfile.getAbsolutePath());
    } catch (FileNotFoundException e1) {
      System.err.println("Input file not found.");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
    String path = outputfile.getAbsolutePath();
    try {
      bw = GetWriter.getFileWriter(path);
    } catch (IOException e1) {
      System.err.println("Output file not found.");
    }
    String line = null;
    Status status;
    while ((line = br.readLine()) != null) {
      bw.write(linecount);
      linecount++;
      System.out.println("Running tweet line " + linecount);
      status = null;
      // create Tweet Status from the JSON file. Status is the structure containing all the
      // information in tweet.
      try {
        status = DataObjectFactory.createStatus(line);
      } catch (TwitterException e) {
        // TODO Auto-generated catch block
        log.append("[ERROR] Tweet JSON Parse Error in input file, line " + linecount);
        continue;
      }
      if (status == null)
        continue;

      // create the tweet object from the status.
      Tweet tweet = new Tweet(status);

      log.append("\n//////////////////  Line " + linecount + " //////////////////\n");
      log.append("\n[Tweet message]: " + tweet.getText());
      log.append("\n[Tweet coordinates]:  " + tweet.getLatitude() + " " + tweet.getLongitude());
      log.append("\n[Tweet user location]:" + tweet.getUserLocation());
      log.append("\n[Tweet place field]:" + tweet.getPlace());

      log.append("\n[GEOPARSING...] ");
      List<LocEntityAnnotation> topos = ParserFactory.getEnAggrParser().parse(tweet);
      tweet.setToponyms(topos);

      for (LocEntityAnnotation topo : topos) {
        if (filter.contains(topo.getNEType())) {
          log.append("\n[Recognized]: " + topo.getTokenString() + "  [Place type]: "
                  + topo.getNEType());
          bw.write("[LOCATION=" + topo.getTokenString() + " ENTITY_TYPE=" + topo.getNEType() + "]");
        }
      }
      bw.write("\t");

      List<CandidateAndFeature> resolved = null;
      if (topos == null)
        log.append("\nNO TOPONYMS PARSED.");
      else {
        log.append("\n[GEOCODING...]");

        try {
          resolved = CoderFactory.getENAggGeoCoder().resolve(tweet, LocGroupFeatures.DEBUGMODE,
                  LocGroupFeatures.NOFILTER);
        } catch (Exception e) {
          log.append("\nLocation Resolution error");
        }
        if (resolved == null)
          log.append("\n[No location resolved.]");
        else {
          log.append("\n[Rresolved locations]:");

          // Note that we could output multiple results for one location.
          // This is for the user to decide which is the best they want.
          // We may improve this later to output only one result.
          for (CandidateAndFeature c : resolved) {
            if (filter.contains(c.getLe().getNEType())) {
              log.append(c.getAsciiName() + " Country:" + c.getCountryCode() + " State:"
                      + c.getAdm1Code() + " Latitude:" + c.getLatitude() + " Longitude:"
                      + c.getLongitude());
              bw.write("[LOCATION=" + c.getAsciiName() + " COUNTRY_CODE=" + c.getCountryCode()
                      + " STATE_CODE=" + c.getAdm1Code() + " LATITUDE=" + c.getLatitude()
                      + " LONGITUDE=" + c.getLongitude() + " PROBABILITY:" + c.getProb() + "]");
            }

          }
        }
      }
      bw.write("\n");
    }
    bw.close();
  }

  private static void createAndShowGUI() {
    // Create and set up the window.
    JFrame frame = new JFrame("CMU GeoLocator version 3.0 Running Helper");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Dimension minimumSize = new Dimension();
    minimumSize.setSize(1000, 1000);
    frame.setMinimumSize(minimumSize);

    // Add content to the window.
    frame.add(new NewDesktop());

    // Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    // Schedule a job for the event dispatch thread:
    // creating and showing this application's GUI.
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        createAndShowGUI();
      }
    });
  }

}
