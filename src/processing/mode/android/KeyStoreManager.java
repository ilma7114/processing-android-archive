package processing.mode.android;

import processing.app.Base;
import processing.app.Preferences;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;


public class KeyStoreManager extends JFrame {
  static final String GUIDE_URL =
      "http://developer.android.com/tools/publishing/app-signing.html#cert";

  File keyStore;

  JPasswordField passwordField;
  JPasswordField repeatPasswordField;

  JTextField commonName;
  JTextField organizationalUnit;
  JTextField organizationName;
  JTextField localityName;
  JTextField country;
  JTextField stateName;

  public KeyStoreManager(final AndroidEditor editor) {
    super("Android keystore manager");

    Container outer = getContentPane();
    Box pain = Box.createVerticalBox();
    pain.setBorder(new EmptyBorder(13, 13, 13, 13));
    outer.add(pain);

    keyStore = AndroidKeyStore.getKeyStore();
    if(keyStore != null) {
      showKeystorePasswordLayout(pain);
    } else {
      showKeystoreCredentialsLayout(pain);
    }

    // buttons
    JPanel buttons = new JPanel();
    buttons.setAlignmentX(LEFT_ALIGNMENT);
    JButton okButton = new JButton("OK");
    Dimension dim = new Dimension(Preferences.BUTTON_WIDTH,
        okButton.getPreferredSize().height);
    okButton.setPreferredSize(dim);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(keyStore == null) {
          if(checkRequiredFields()) {
            try {
              AndroidKeyStore.generateKeyStore(new String(passwordField.getPassword()),
                  commonName.getText(), organizationalUnit.getText(), organizationName.getText(),
                  localityName.getText(), stateName.getText(), country.getText());

              setVisible(false);
              editor.startExportPackage(new String(passwordField.getPassword()));
            } catch (Exception e1) {
              e1.printStackTrace();
            }
          }
        } else {
          setVisible(false);
          editor.startExportPackage(new String(passwordField.getPassword()));
        }
      }
    });
    okButton.setEnabled(true);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setPreferredSize(dim);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    cancelButton.setEnabled(true);

    // think different, biznatchios!
    if (Base.isMacOS()) {
      buttons.add(cancelButton);
//      buttons.add(Box.createHorizontalStrut(8));
      buttons.add(okButton);
    } else {
      buttons.add(okButton);
//      buttons.add(Box.createHorizontalStrut(8));
      buttons.add(cancelButton);
    }
//    buttons.setMaximumSize(new Dimension(300, buttons.getPreferredSize().height));
    pain.add(buttons);

    JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
    ActionListener disposer = new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        setVisible(false);
      }
    };
    processing.app.Toolkit.registerWindowCloseKeys(root, disposer);
    processing.app.Toolkit.setIcon(this);

    pack();

    Dimension screen = processing.app.Toolkit.getScreenSize();
    Dimension windowSize = getSize();

    setLocation((screen.width - windowSize.width) / 2,
        (screen.height - windowSize.height) / 2);

    setVisible(true);
  }

  private void showKeystorePasswordLayout(Box pain) {
    passwordField = new JPasswordField(15);
    JLabel passwordLabel = new JLabel("<html><body><b>Keystore password: </b></body></html>");
    passwordLabel.setLabelFor(passwordField);

    JPanel textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(passwordLabel);
    textPane.add(passwordField);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);
  }

  private boolean checkRequiredFields() {
    if(passwordField.getPassword().length > 5) {
      if(Arrays.equals(passwordField.getPassword(), repeatPasswordField.getPassword())) {
        return true;
      } else {
        Base.showWarning("Passwords", "Keystore passwords do not match");
        return false;
      }
    } else {
      Base.showWarning("Passwords", "Keystore password should be at least 6 characters long");
      return false;
    }
  }

  private void showKeystoreCredentialsLayout(Box pain) {
    String labelText =
        "<html>" +
            "Please enter the information below so we can generate a private key for you.<br/>" +
            "Fields marked <b>bold</b> are required, " +
            "though you may consider to fill some of optional fields below those to avoid potential problems.<br/>" +
            "More about private keys can be found " +
            "<a href=\"" + GUIDE_URL + "\">here</a>.</body></html>";
    JLabel textarea = new JLabel(labelText);
    textarea.setPreferredSize(new Dimension(400, 100));
    textarea.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        Base.openURL(GUIDE_URL);
      }
    });
    textarea.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textarea);

    // password field
    passwordField = new JPasswordField(15);
    JLabel passwordLabel = new JLabel("<html><body><b>Keystore password: </b></body></html>");
    passwordLabel.setLabelFor(passwordField);

    JPanel textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(passwordLabel);
    textPane.add(passwordField);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);

    // repeat password field
    repeatPasswordField = new JPasswordField(15);
    JLabel repeatPasswordLabel = new JLabel("<html><body><b>Repeat keystore password: </b></body></html>");
    repeatPasswordLabel.setLabelFor(passwordField);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(repeatPasswordLabel);
    textPane.add(repeatPasswordField);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    textPane.setBorder(new EmptyBorder(0, 0, 15, 0));
    pain.add(textPane);

    MatteBorder mb = new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY);
    TitledBorder tb = new TitledBorder(mb, "Keystore issuer credentials", TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION);
    JPanel separatorPanel = new JPanel();
    separatorPanel.setBorder(tb);
    pain.add(separatorPanel);

    // common name (CN)
    commonName = new JTextField(15);
    JLabel commonNameLabel = new JLabel("First and last name: ");
    commonNameLabel.setLabelFor(commonName);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(commonNameLabel);
    textPane.add(commonName);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);

    // organizational unit (OU)
    organizationalUnit = new JTextField(15);
    JLabel organizationalUnitLabel = new JLabel("Organizational unit: ");
    organizationalUnitLabel.setLabelFor(organizationalUnit);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(organizationalUnitLabel);
    textPane.add(organizationalUnit);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);

    // organization name (O)
    organizationName = new JTextField(15);
    JLabel organizationNameLabel = new JLabel("Organization name: ");
    organizationNameLabel.setLabelFor(organizationName);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(organizationNameLabel);
    textPane.add(organizationName);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);

    // locality name (L)
    localityName = new JTextField(15);
    JLabel localityNameLabel = new JLabel("City or locality: ");
    localityNameLabel.setLabelFor(localityName);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(localityNameLabel);
    textPane.add(localityName);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);

    // state name (S)
    stateName = new JTextField(15);
    JLabel stateNameLabel = new JLabel("State name: ");
    stateNameLabel.setLabelFor(stateName);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(stateNameLabel);
    textPane.add(stateName);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);

    // country (C)
    country = new JTextField(15);
    JLabel countryLabel = new JLabel("Country code (XX): ");
    countryLabel.setLabelFor(country);

    textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    textPane.add(countryLabel);
    textPane.add(country);
    textPane.setAlignmentX(LEFT_ALIGNMENT);
    pain.add(textPane);
  }
}
