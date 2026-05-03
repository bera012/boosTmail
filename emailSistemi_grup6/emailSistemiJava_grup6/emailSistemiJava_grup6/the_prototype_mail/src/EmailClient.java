import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class EmailClient extends Application {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private List<User> users = new ArrayList<>();
    private List<String> rememberedUsers = new ArrayList<>();
    private User currentUser = null;
    private ObservableList<Email> inboxEmails = FXCollections.observableArrayList();
    private ObservableList<Email> outboxEmails = FXCollections.observableArrayList();

    // Tema Seçenekleri (Enum)
    private enum Theme {
        RED_WHITE, // Klasik Kırmızı-Beyaz
        DARK,      // Karanlık Mod
        LIGHT      // Aydınlık (Gri/Mavi) Mod
    }

    // Varsayılan Tema: Kırmızı-Beyaz
    private Theme currentTheme = Theme.RED_WHITE;

    // 4 farklı canvas (scene)
    private Scene loginScene;
    private Scene signupScene;
    private Scene mainScene;
    private Scene rememberedAccountsScene;

    // UI bileşenleri - Ana Ekran
    private ImageView profileImageView;
    private ImageView mainProfileImageView;
    private ListView<Email> emailListView;
    private TextArea emailContentArea;
    private BorderPane mainScreen;
    private Stage primaryStage;
    private VBox menuBox;
    private HBox topBar;
    private Label welcomeLabel;
    private Label inboxCount;

    // UI Bileşenleri - Ana Menü Butonları
    private Button inboxButton, outboxButton, composeButton, starredButton;

    // UI Bileşenleri - Giriş Ekranı
    private Label loginBoosLabel, loginTLabel, loginMailLabel, loginSubtitle;
    private TextField loginUsernameField, loginPasswordTextField;
    private PasswordField loginPasswordField;
    private Button loginLoginButton, loginToggleButton;
    private CheckBox loginRememberMeCheckbox;
    private Hyperlink loginSignUpLink, loginRememberedLink;
    private Label loginDividerLabel;

    // UI Bileşenleri - Kayıt Ekranı
    private Label signupTitleLabel, signupSubtitleLabel;
    private TextField signupUsernameField, signupPasswordTextField;
    private PasswordField signupPasswordField;
    private Button signupSignupButton, signupSelectImgButton, signupToggleButton;
    private CheckBox signupRememberMeCheckbox;
    private Hyperlink signupLoginLink;

    // UI Bileşenleri - Hesaplar Ekranı
    private Label remTitleLabel, remSubtitleLabel, remNoAccountsLabel;
    private VBox accountsBox;
    private Button remBackButton;

    // Tüm sahnelerin kök yapıları
    private VBox loginScreen;
    private VBox signinScreen;
    private VBox rememberedScreen;

    private void verileriYukle() {
        File file = new File("user.json");

        if (!file.exists() || file.length() == 0) {
            this.users = new ArrayList<>();
            return;
        }

        try {
            String jsonContent = new String(Files.readAllBytes(file.toPath()));

            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                this.users = new ArrayList<>();
                return;
            }

            String trimmedContent = jsonContent.trim();
            if (!trimmedContent.startsWith("[") || !trimmedContent.endsWith("]")) {
                this.users = new ArrayList<>();
                try (FileWriter writer = new FileWriter("user.json")) {
                    writer.write("[]");
                }
                return;
            }

            Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
            ArrayList<User> yuklenenVeriler = gson.fromJson(jsonContent, userListType);

            if (yuklenenVeriler != null) {
                this.users = yuklenenVeriler;
            } else {
                this.users = new ArrayList<>();
            }

        } catch (Exception e) {
            this.users = new ArrayList<>();
            try (FileWriter writer = new FileWriter("user.json")) {
                writer.write("[]");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void hatirlananlariYukle() {
        File file = new File("remembered.json");
        if (!file.exists() || file.length() == 0) {
            this.rememberedUsers = new ArrayList<>();
            return;
        }
        try {
            String jsonContent = new String(Files.readAllBytes(file.toPath()));
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> yuklenen = gson.fromJson(jsonContent, listType);
            if (yuklenen != null) {
                this.rememberedUsers = yuklenen;
            } else {
                this.rememberedUsers = new ArrayList<>();
            }
        } catch (Exception e) {
            this.rememberedUsers = new ArrayList<>();
        }
    }

    private void hatirlananlariKaydet() {
        try (FileWriter writer = new FileWriter("remembered.json")) {
            gson.toJson(rememberedUsers, writer);
        } catch (IOException e) {
            System.err.println("Hatırlanan kullanıcılar kaydedilemedi: " + e.getMessage());
        }
    }

    public EmailClient() throws FileNotFoundException {
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // İkon bulunamazsa devam et
        }

        this.primaryStage = primaryStage;
        verileriYukle();
        hatirlananlariYukle();

        createLoginScene();
        createSignupScene();
        createRememberedAccountsScene();

        primaryStage.setTitle("BoosTMail");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private VBox createTurkishFlag() {
        VBox flag = new VBox();
        flag.setPrefSize(60, 40);
        // Bayrak her zaman bayrak kırmızısı kalabilir veya temaya uydurulabilir.
        // İsteğiniz üzerine koyu kırmızı yapıyorum.
        flag.setStyle("-fx-background-color: #8B0000;"); // Koyu Kırmızı

        Label symbol = new Label("☾ ★");
        symbol.setTextFill(Color.WHITE);
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        symbol.setStyle("-fx-padding: 5;");

        flag.getChildren().add(symbol);
        flag.setAlignment(Pos.CENTER);

        return flag;
    }

    private void createLoginScene() {
        loginScreen = new VBox(20);
        loginScreen.setPadding(new Insets(40));
        loginScreen.setAlignment(Pos.CENTER);

        VBox flag = createTurkishFlag();

        HBox titleBox = new HBox(0);
        titleBox.setAlignment(Pos.CENTER);

        loginBoosLabel = new Label("Boos");
        loginBoosLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        loginTLabel = new Label("T");
        loginTLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        loginMailLabel = new Label("Mail");
        loginMailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        titleBox.getChildren().addAll(loginBoosLabel, loginTLabel, loginMailLabel);

        loginSubtitle = new Label("BoosTMail - Giriş");
        loginSubtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        loginUsernameField = new TextField();
        loginUsernameField.setPromptText("E-mail adresi");
        loginUsernameField.setMaxWidth(300);

        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setMaxWidth(300);

        loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Şifre");
        loginPasswordField.setPrefWidth(250);

        loginPasswordTextField = new TextField();
        loginPasswordTextField.setPromptText("Şifre");
        loginPasswordTextField.setPrefWidth(250);
        loginPasswordTextField.setVisible(false);
        loginPasswordTextField.setManaged(false);

        loginToggleButton = new Button("👁");
        loginToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");

        final boolean[] passwordVisible = {false};
        loginToggleButton.setOnAction(e -> {
            passwordVisible[0] = !passwordVisible[0];
            if (passwordVisible[0]) {
                loginPasswordTextField.setText(loginPasswordField.getText());
                loginPasswordField.setVisible(false);
                loginPasswordField.setManaged(false);
                loginPasswordTextField.setVisible(true);
                loginPasswordTextField.setManaged(true);
            } else {
                loginPasswordField.setText(loginPasswordTextField.getText());
                loginPasswordTextField.setVisible(false);
                loginPasswordTextField.setManaged(false);
                loginPasswordField.setVisible(true);
                loginPasswordField.setManaged(true);
            }
        });

        StackPane passwordStack = new StackPane(loginPasswordField, loginPasswordTextField);
        passwordBox.getChildren().addAll(passwordStack, loginToggleButton);

        loginRememberMeCheckbox = new CheckBox("Beni Hatırla");
        loginRememberMeCheckbox.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        HBox linksBox = new HBox(15);
        linksBox.setAlignment(Pos.CENTER);

        loginSignUpLink = new Hyperlink("Kayıt Ol");
        loginSignUpLink.setOnAction(e -> primaryStage.setScene(signupScene));

        loginRememberedLink = new Hyperlink("Kayıtlı Hesaplar");
        loginRememberedLink.setOnAction(e -> {
            createRememberedAccountsScene();
            primaryStage.setScene(rememberedAccountsScene);
        });

        loginDividerLabel = new Label("|");

        linksBox.getChildren().addAll(loginSignUpLink, loginDividerLabel, loginRememberedLink);

        loginLoginButton = new Button("Giriş Yap");
        loginLoginButton.setPrefWidth(150);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        loginLoginButton.setOnAction(e -> {
            String username = loginUsernameField.getText();
            String password = passwordVisible[0] ? loginPasswordTextField.getText() : loginPasswordField.getText();

            if (authenticateUser(username, password)) {
                if (loginRememberMeCheckbox.isSelected() && !rememberedUsers.contains(username)) {
                    rememberedUsers.add(username);
                    hatirlananlariKaydet();
                }
                createMainScene();
                primaryStage.setScene(mainScene);
                loginUsernameField.clear();
                loginPasswordField.clear();

            } else {
                errorLabel.setText("Hatalı kullanıcı adı veya şifre!");
            }
        });

        loginScreen.getChildren().addAll(flag, titleBox, loginSubtitle, loginUsernameField, passwordBox, loginRememberMeCheckbox, linksBox, loginLoginButton, errorLabel);
        loginScene = new Scene(loginScreen, 1000, 650);

        applyTheme();
    }


    private void setDefaultIcon(ImageView imageView) {
        try {
            var res = getClass().getResourceAsStream("/default-profile.png");
            if (res != null) {
                imageView.setImage(new Image(res));
            } else {
                System.err.println("Kaynak bulunamadı: /default-profile.png");
            }
        } catch (Exception e) {
            System.err.println("Varsayılan ikon yüklenirken hata: " + e.getMessage());
        }
    }



    private void createSignupScene() {
        signinScreen = new VBox(20);
        signinScreen.setPadding(new Insets(40));
        signinScreen.setAlignment(Pos.CENTER);

        VBox flag = createTurkishFlag();

        signupTitleLabel = new Label("BoosTMail");
        signupTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        signupSubtitleLabel = new Label("BoosTMail - Kayıt");
        signupSubtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        profileImageView = new ImageView();
        profileImageView.setFitWidth(120);
        profileImageView.setFitHeight(90);
        profileImageView.setPreserveRatio(false);

        Circle clip = new Circle(60, 45, 45);
        profileImageView.setClip(clip);

        try {
            var resource = getClass().getResourceAsStream("/default-profile.png");
            if (resource != null) {
                Image defaultImage = new Image(resource);
                profileImageView.setImage(defaultImage);
            } else {
                System.out.println("Hata: /default-profile.png dosyası kaynaklarda bulunamadı!");
            }
        } catch (Exception e) {
            System.err.println("Resim yükleme hatası: " + e.getMessage());
        }

        signupSelectImgButton = new Button("Profil Fotoğrafı Seç");
        signupSelectImgButton.setOnAction(e -> selectProfileImage(profileImageView));

        signupUsernameField = new TextField();
        signupUsernameField.setPromptText("E-mail adresi");
        signupUsernameField.setMaxWidth(300);

        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setMaxWidth(300);

        signupPasswordField = new PasswordField();
        signupPasswordField.setPromptText("Şifre");
        signupPasswordField.setPrefWidth(250);

        signupPasswordTextField = new TextField();
        signupPasswordTextField.setPromptText("Şifre");
        signupPasswordTextField.setPrefWidth(250);
        signupPasswordTextField.setVisible(false);
        signupPasswordTextField.setManaged(false);

        signupToggleButton = new Button("👁");
        signupToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");

        final boolean[] passwordVisible = {false};
        signupToggleButton.setOnAction(e -> {
            passwordVisible[0] = !passwordVisible[0];
            if (passwordVisible[0]) {
                signupPasswordTextField.setText(signupPasswordField.getText());
                signupPasswordField.setVisible(false);
                signupPasswordField.setManaged(false);
                signupPasswordTextField.setVisible(true);
                signupPasswordTextField.setManaged(true);
            } else {
                signupPasswordField.setText(signupPasswordTextField.getText());
                signupPasswordTextField.setVisible(false);
                signupPasswordTextField.setManaged(false);
                signupPasswordField.setVisible(true);
                signupPasswordField.setManaged(true);
            }
        });

        StackPane passwordStack = new StackPane(signupPasswordField, signupPasswordTextField);
        passwordBox.getChildren().addAll(passwordStack, signupToggleButton);

        signupRememberMeCheckbox = new CheckBox("Beni Hatırla");
        signupRememberMeCheckbox.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        signupLoginLink = new Hyperlink("Giriş Yap");
        signupLoginLink.setOnAction(e -> primaryStage.setScene(loginScene));

        signupSignupButton = new Button("Kayıt Ol");
        signupSignupButton.setPrefWidth(150);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        signupSignupButton.setOnAction(event -> {
            String username = signupUsernameField.getText().trim();
            String password = passwordVisible[0] ? signupPasswordTextField.getText() : signupPasswordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Lütfen tüm alanları doldurun!");
                return;
            }

            for (User existingUser : users) {
                if (existingUser.getUsername().equals(username)) {
                    errorLabel.setText("Bu e-mail adresi zaten kayıtlı!");
                    return;
                }
            }

            User user = new User(username, password);

            if (profileImageView.getImage() != null) {
                try {
                    String base64Image = imageToBase64(profileImageView.getImage());
                    user.setProfileImage(base64Image);
                } catch (Exception e) {
                    System.err.println("Profil fotoğrafı kaydedilemedi: " + e.getMessage());
                }
            }

            user.addEmail(new Email("admin@boostmail.tr", user.getUsername(), "Hoş Geldiniz", "BoosTMail - hoş geldiniz!"));
            users.add(user);

            saveUsersToJson();

            if (signupRememberMeCheckbox.isSelected() && !rememberedUsers.contains(username)) {
                rememberedUsers.add(username);
                hatirlananlariKaydet();
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Kayıt Başarılı");
            alert.setHeaderText("Başarıyla kayıt oldunuz!");
            alert.setContentText("Bu hesap ile giriş yapmak ister misiniz?");

            ButtonType yesButton = new ButtonType("Evet", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Hayır", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    currentUser = user;
                    inboxEmails.setAll(user.getInbox());
                    outboxEmails.setAll(user.getOutbox());
                    createMainScene();
                    primaryStage.setScene(mainScene);
                } else {
                    primaryStage.setScene(loginScene);
                }
                signupUsernameField.clear();
                signupPasswordField.clear();
            });
        });

        signinScreen.getChildren().addAll(flag, signupTitleLabel, signupSubtitleLabel, profileImageView, signupSelectImgButton, signupUsernameField, passwordBox, signupRememberMeCheckbox, signupLoginLink, signupSignupButton, errorLabel);
        signupScene = new Scene(signinScreen, 1000, 650);
        applyTheme();
    }

    private void createRememberedAccountsScene() {
        rememberedScreen = new VBox(20);
        rememberedScreen.setPadding(new Insets(40));
        rememberedScreen.setAlignment(Pos.CENTER);

        VBox flag = createTurkishFlag();

        remTitleLabel = new Label("Kayıtlı Hesaplar");
        remTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        remSubtitleLabel = new Label("Bir hesap seçin");
        remSubtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(350);

        accountsBox = new VBox(15);
        accountsBox.setAlignment(Pos.CENTER);
        accountsBox.setPadding(new Insets(20));

        if (rememberedUsers.isEmpty()) {
            remNoAccountsLabel = new Label("Kayıtlı hesap bulunamadı.");
            remNoAccountsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            accountsBox.getChildren().add(remNoAccountsLabel);
        } else {
            for (String email : rememberedUsers) {
                HBox accountBox = new HBox(15);
                accountBox.setAlignment(Pos.CENTER_LEFT);
                accountBox.setPrefWidth(450);

                ImageView accountImageView = new ImageView();
                accountImageView.setFitWidth(50);
                accountImageView.setFitHeight(37.5);
                accountImageView.setPreserveRatio(false);

                Circle accountClip = new Circle(25, 18.75, 18.75);
                accountImageView.setClip(accountClip);

                for (User u : users) {
                    if (u.getUsername().equals(email)) {
                        if (u.getProfileImage() != null && !u.getProfileImage().isEmpty()) {
                            Image profileImg = base64ToImage(u.getProfileImage());
                            if (profileImg != null) {
                                accountImageView.setImage(profileImg);
                            } else {
                                setDefaultIcon(accountImageView); // Decode edilemezse varsayılan
                            }
                        } else {
                            setDefaultIcon(accountImageView); // Veri yoksa varsayılan
                        }
                        break;
                    }
                }

                Label emailLabel = new Label(email);
                emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                emailLabel.setPrefWidth(250);

                Button loginBtn = new Button("Giriş Yap");
                loginBtn.setPrefWidth(100);

                Button removeBtn = new Button("🗑");
                removeBtn.setTooltip(new Tooltip("Hesabı kaldır"));

                loginBtn.setOnAction(e -> {
                    Dialog<String> dialog = new Dialog<>();
                    dialog.setTitle("Şifre Gerekli");
                    dialog.setHeaderText(email + " için şifre girin:");

                    ButtonType loginButtonType = new ButtonType("Giriş", ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    PasswordField passwordField = new PasswordField();
                    passwordField.setPromptText("Şifre");
                    passwordField.setPrefWidth(200);

                    TextField textField = new TextField();
                    textField.setPromptText("Şifre");
                    textField.setPrefWidth(200);
                    textField.setVisible(false);
                    textField.setManaged(false);

                    Button toggleButton = new Button("👁");
                    toggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px;");

                    final boolean[] isVisible = {false};
                    toggleButton.setOnAction(ev -> {
                        isVisible[0] = !isVisible[0];
                        if (isVisible[0]) {
                            textField.setText(passwordField.getText());
                            passwordField.setVisible(false);
                            passwordField.setManaged(false);
                            textField.setVisible(true);
                            textField.setManaged(true);
                        } else {
                            passwordField.setText(textField.getText());
                            textField.setVisible(false);
                            textField.setManaged(false);
                            passwordField.setVisible(true);
                            passwordField.setManaged(true);
                        }
                    });

                    StackPane fieldStack = new StackPane(passwordField, textField);
                    hbox.getChildren().addAll(new Label("Şifre:"), fieldStack, toggleButton);

                    dialog.getDialogPane().setContent(hbox);

                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == loginButtonType) {
                            return isVisible[0] ? textField.getText() : passwordField.getText();
                        }
                        return null;
                    });

                    dialog.showAndWait().ifPresent(password -> {
                        if (authenticateUser(email, password)) {
                            createMainScene();
                            primaryStage.setScene(mainScene);
                        } else {
                            showAlert("Hata", "Hatalı şifre!");
                        }
                    });
                });

                removeBtn.setOnAction(e -> {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Hesap Kaldır");
                    confirmAlert.setHeaderText("Bu hesabı kayıtlı hesaplardan kaldırmak istediğinize emin misiniz?");
                    confirmAlert.setContentText(email);

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            rememberedUsers.remove(email);
                            hatirlananlariKaydet();
                            createRememberedAccountsScene();
                            primaryStage.setScene(rememberedAccountsScene);
                        }
                    });
                });

                accountBox.getChildren().addAll(accountImageView, emailLabel, loginBtn, removeBtn);
                accountsBox.getChildren().add(accountBox);
            }
        }

        scrollPane.setContent(accountsBox);

        remBackButton = new Button("← Geri");
        remBackButton.setPrefWidth(150);
        remBackButton.setOnAction(e -> primaryStage.setScene(loginScene));

        rememberedScreen.getChildren().addAll(flag, remTitleLabel, remSubtitleLabel, scrollPane, remBackButton);
        rememberedAccountsScene = new Scene(rememberedScreen, 1000, 650);
        applyTheme();
    }

    private void createMainScene() {
        mainScreen = new BorderPane();

        topBar = new HBox(15);
        topBar.setPadding(new Insets(7));
        topBar.setAlignment(Pos.CENTER_LEFT);

        mainProfileImageView = new ImageView();
        mainProfileImageView.setFitWidth(60);
        mainProfileImageView.setFitHeight(45);
        mainProfileImageView.setPreserveRatio(false);

        Circle clip = new Circle(30, 22.5, 22.5);
        mainProfileImageView.setClip(clip);

        if (currentUser != null) {
            if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                Image profileImage = base64ToImage(currentUser.getProfileImage());
                if (profileImage != null) {
                    mainProfileImageView.setImage(profileImage);
                } else {
                    setDefaultIcon(mainProfileImageView);
                }
            } else {
                setDefaultIcon(mainProfileImageView);
            }
        }

        mainProfileImageView.setOnMouseClicked(e -> selectProfileImage(mainProfileImageView));
        mainProfileImageView.setStyle("-fx-cursor: hand;");

        welcomeLabel = new Label("Hoş Geldiniz: " + currentUser.getUsername());
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button themeButton = new Button("Tema: Kırmızı-Beyaz");
        themeButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        themeButton.setOnAction(e -> cycleTheme(themeButton));

        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: white; -fx-text-fill: #E30A17; -fx-font-weight: bold;");
        logoutButton.setOnAction(e -> primaryStage.setScene(loginScene));

        topBar.getChildren().addAll(mainProfileImageView, welcomeLabel, spacer, themeButton, logoutButton);
        mainScreen.setTop(topBar);

        menuBox = new VBox(10);
        menuBox.setPadding(new Insets(20));
        menuBox.setPrefWidth(200);

        inboxButton = new Button("Gelen Kutusu");
        inboxButton.setPrefWidth(180);
        inboxButton.setOnAction(e -> showInbox());

        outboxButton = new Button("Gönderilenler Kutusu");
        outboxButton.setPrefWidth(180);
        outboxButton.setOnAction(e -> showOutbox());

        composeButton = new Button("Yeni E-mail");
        composeButton.setPrefWidth(180);
        composeButton.setOnAction(e -> showComposeScreen());

        starredButton = new Button("⭐ Yıldızlı mailler");
        starredButton.setPrefWidth(180);
        starredButton.setOnAction(e -> showStarredEmails());

        inboxCount = new Label("Gelen Kutusu: " + inboxEmails.size() + " e-mail");
        inboxCount.setTextFill(Color.WHITE);

        menuBox.getChildren().addAll(inboxButton, outboxButton, composeButton, starredButton, inboxCount);
        mainScreen.setLeft(menuBox);

        showInbox();
        applyTheme();

        switch (currentTheme) {
            case RED_WHITE: themeButton.setText("Tema: Kırmızı-Beyaz"); break;
            case DARK: themeButton.setText("Tema: Karanlık"); break;
            case LIGHT: themeButton.setText("Tema: Aydınlık"); break;
        }

        mainScene = new Scene(mainScreen, 1000, 650);
    }

    private void cycleTheme(Button themeButton) {
        if (currentTheme == Theme.RED_WHITE) {
            currentTheme = Theme.DARK;
            themeButton.setText("Tema: Karanlık");
        } else if (currentTheme == Theme.DARK) {
            currentTheme = Theme.LIGHT;
            themeButton.setText("Tema: Aydınlık");
        } else {
            currentTheme = Theme.RED_WHITE;
            themeButton.setText("Tema: Kırmızı-Beyaz");
        }
        applyTheme();

        if (mainScreen.getCenter() instanceof VBox) {
            VBox center = (VBox) mainScreen.getCenter();
            if(!center.getChildren().isEmpty() && center.getChildren().get(0) instanceof Label) {
                Label title = (Label) center.getChildren().get(0);
                if(title.getText().contains("Gelen")) showInbox();
                else if(title.getText().contains("Gönderilen")) showOutbox();
                else if(title.getText().contains("Yıldız")) showStarredEmails();
                else if(title.getText().contains("Yeni")) showComposeScreen();
            }
        }
    }

    private void applyTheme() {
        String mainBg, menuBg, topBarBg, textColor, loginBg, darkInput, lightInput, menuBtnStyle;

        darkInput = "-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-prompt-text-fill: gray;";
        lightInput = "-fx-control-inner-background: white; -fx-text-fill: black; -fx-prompt-text-fill: gray;";

        // Koyu Kırmızı renk sabiti (Hem Karanlık hem Kırmızı-Beyaz için)
        String darkRedColor = "#8B0000";

        switch (currentTheme) {
            case DARK:
                mainBg = "#1e1e1e";
                menuBg = "#1a252f";
                topBarBg = "#2c3e50";
                textColor = "white";
                loginBg = "#121212";
                menuBtnStyle = "-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";

                // Login Ekranı
                if (loginBoosLabel != null) loginBoosLabel.setTextFill(Color.WHITE);
                if (loginTLabel != null) loginTLabel.setStyle("-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 3;");
                if (loginMailLabel != null) loginMailLabel.setTextFill(Color.WHITE);
                if (loginSubtitle != null) loginSubtitle.setTextFill(Color.LIGHTGRAY);
                if (loginUsernameField != null) loginUsernameField.setStyle(darkInput);
                if (loginPasswordField != null) loginPasswordField.setStyle(darkInput);
                if (loginPasswordTextField != null) loginPasswordTextField.setStyle(darkInput);
                if (loginLoginButton != null) loginLoginButton.setStyle("-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
                if (loginRememberMeCheckbox != null) loginRememberMeCheckbox.setTextFill(Color.WHITE);
                if (loginSignUpLink != null) loginSignUpLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #64b5f6;");
                if (loginRememberedLink != null) loginRememberedLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #64b5f6;");
                if (loginDividerLabel != null) loginDividerLabel.setTextFill(Color.GRAY);
                if (loginToggleButton != null) loginToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: transparent;");

                // Signup Ekranı
                if (signupTitleLabel != null) signupTitleLabel.setTextFill(Color.WHITE);
                if (signupSubtitleLabel != null) signupSubtitleLabel.setTextFill(Color.LIGHTGRAY);
                if (signupUsernameField != null) signupUsernameField.setStyle(darkInput);
                if (signupPasswordField != null) signupPasswordField.setStyle(darkInput);
                if (signupPasswordTextField != null) signupPasswordTextField.setStyle(darkInput);
                if (signupSignupButton != null) signupSignupButton.setStyle("-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
                if (signupSelectImgButton != null) signupSelectImgButton.setStyle("-fx-background-color: #64b5f6; -fx-text-fill: black;");
                if (signupRememberMeCheckbox != null) signupRememberMeCheckbox.setTextFill(Color.WHITE);
                if (signupLoginLink != null) signupLoginLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #64b5f6;");
                if (signupToggleButton != null) signupToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: white; -fx-background-color: transparent;");

                // Remembered Ekranı
                if (remTitleLabel != null) remTitleLabel.setTextFill(Color.WHITE);
                if (remSubtitleLabel != null) remSubtitleLabel.setTextFill(Color.LIGHTGRAY);
                if (remNoAccountsLabel != null) remNoAccountsLabel.setTextFill(Color.GRAY);
                if (remBackButton != null) remBackButton.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold;");
                if (accountsBox != null) {
                    for (Node node : accountsBox.getChildren()) {
                        if (node instanceof HBox) {
                            node.setStyle("-fx-background-color: #333333; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #555; -fx-border-radius: 10; -fx-border-width: 2;");
                            for (Node child : ((HBox) node).getChildren()) {
                                if (child instanceof Label) ((Label) child).setTextFill(Color.WHITE);
                                if (child instanceof Button) {
                                    Button btn = (Button) child;
                                    if ("Giriş Yap".equals(btn.getText())) {
                                        btn.setStyle("-fx-background-color: #64b5f6; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
                                    }
                                }
                            }
                        }
                    }
                }

                if (inboxCount != null) inboxCount.setTextFill(Color.WHITE);
                break;

            case LIGHT:
                mainBg = "#f9f9f9";
                menuBg = "#ecf0f1";
                topBarBg = "#3498db";
                textColor = "black";
                loginBg = "#ffffff";
                menuBtnStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";

                // Login Ekranı
                if (loginBoosLabel != null) loginBoosLabel.setTextFill(Color.BLACK);
                if (loginTLabel != null) loginTLabel.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 3;");
                if (loginMailLabel != null) loginMailLabel.setTextFill(Color.BLACK);
                if (loginSubtitle != null) loginSubtitle.setTextFill(Color.GRAY);
                if (loginUsernameField != null) loginUsernameField.setStyle(lightInput);
                if (loginPasswordField != null) loginPasswordField.setStyle(lightInput);
                if (loginPasswordTextField != null) loginPasswordTextField.setStyle(lightInput);
                if (loginLoginButton != null) loginLoginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                if (loginRememberMeCheckbox != null) loginRememberMeCheckbox.setTextFill(Color.BLACK);
                if (loginSignUpLink != null) loginSignUpLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                if (loginRememberedLink != null) loginRememberedLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                if (loginDividerLabel != null) loginDividerLabel.setTextFill(Color.BLACK);
                if (loginToggleButton != null) loginToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: transparent;");

                // Signup Ekranı
                if (signupTitleLabel != null) signupTitleLabel.setTextFill(Color.web("#3498db"));
                if (signupSubtitleLabel != null) signupSubtitleLabel.setTextFill(Color.GRAY);
                if (signupUsernameField != null) signupUsernameField.setStyle(lightInput);
                if (signupPasswordField != null) signupPasswordField.setStyle(lightInput);
                if (signupPasswordTextField != null) signupPasswordTextField.setStyle(lightInput);
                if (signupSignupButton != null) signupSignupButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                if (signupSelectImgButton != null) signupSelectImgButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                if (signupRememberMeCheckbox != null) signupRememberMeCheckbox.setTextFill(Color.BLACK);
                if (signupLoginLink != null) signupLoginLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                if (signupToggleButton != null) signupToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: transparent;");

                // Remembered Ekranı
                if (remTitleLabel != null) remTitleLabel.setTextFill(Color.web("#3498db"));
                if (remSubtitleLabel != null) remSubtitleLabel.setTextFill(Color.GRAY);
                if (remNoAccountsLabel != null) remNoAccountsLabel.setTextFill(Color.GRAY);
                if (remBackButton != null) remBackButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
                if (accountsBox != null) {
                    for (Node node : accountsBox.getChildren()) {
                        if (node instanceof HBox) {
                            node.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-radius: 10; -fx-border-width: 2;");
                            for (Node child : ((HBox) node).getChildren()) {
                                if (child instanceof Label) ((Label) child).setTextFill(Color.BLACK);
                                if (child instanceof Button) {
                                    Button btn = (Button) child;
                                    if ("Giriş Yap".equals(btn.getText())) {
                                        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                                    }
                                }
                            }
                        }
                    }
                }

                if (inboxCount != null) inboxCount.setTextFill(Color.BLACK);
                break;

            case RED_WHITE:
            default:
                mainBg = "#ffffff";
                menuBg = "#34495e";
                // BURASI DEĞİŞTİ: Artık Koyu Kırmızı kullanıyor (#8B0000)
                topBarBg = darkRedColor;
                textColor = "black";
                loginBg = "#f0f0f0";
                menuBtnStyle = "-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";

                // Login Ekranı
                if (loginBoosLabel != null) loginBoosLabel.setTextFill(Color.DARKBLUE);
                // BURASI DEĞİŞTİ: T harfi koyu kırmızı
                if (loginTLabel != null) loginTLabel.setStyle("-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 3;");
                if (loginMailLabel != null) loginMailLabel.setTextFill(Color.DARKBLUE);
                if (loginSubtitle != null) loginSubtitle.setTextFill(Color.GRAY);
                if (loginUsernameField != null) loginUsernameField.setStyle(lightInput);
                if (loginPasswordField != null) loginPasswordField.setStyle(lightInput);
                if (loginPasswordTextField != null) loginPasswordTextField.setStyle(lightInput);
                // BURASI DEĞİŞTİ: Buton koyu kırmızı
                if (loginLoginButton != null) loginLoginButton.setStyle("-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
                if (loginRememberMeCheckbox != null) loginRememberMeCheckbox.setTextFill(Color.BLACK);
                // BURASI DEĞİŞTİ: Linkler koyu kırmızı
                if (loginSignUpLink != null) loginSignUpLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: " + darkRedColor + ";");
                if (loginRememberedLink != null) loginRememberedLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                if (loginDividerLabel != null) loginDividerLabel.setTextFill(Color.BLACK);
                if (loginToggleButton != null) loginToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: transparent;");

                // Signup Ekranı
                if (signupTitleLabel != null) signupTitleLabel.setTextFill(Color.web(darkRedColor));
                if (signupSubtitleLabel != null) signupSubtitleLabel.setTextFill(Color.GRAY);
                if (signupUsernameField != null) signupUsernameField.setStyle(lightInput);
                if (signupPasswordField != null) signupPasswordField.setStyle(lightInput);
                if (signupPasswordTextField != null) signupPasswordTextField.setStyle(lightInput);
                // BURASI DEĞİŞTİ: Buton koyu kırmızı
                if (signupSignupButton != null) signupSignupButton.setStyle("-fx-background-color: " + darkRedColor + "; -fx-text-fill: white; -fx-font-weight: bold;");
                if (signupSelectImgButton != null) signupSelectImgButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                if (signupRememberMeCheckbox != null) signupRememberMeCheckbox.setTextFill(Color.BLACK);
                if (signupLoginLink != null) signupLoginLink.setStyle("-fx-font-style: italic; -fx-font-weight: bold; -fx-text-fill: " + darkRedColor + ";");
                if (signupToggleButton != null) signupToggleButton.setStyle("-fx-cursor: hand; -fx-font-size: 16px; -fx-text-fill: black; -fx-background-color: transparent;");

                // Remembered Ekranı
                if (remTitleLabel != null) remTitleLabel.setTextFill(Color.web("#3498db"));
                if (remSubtitleLabel != null) remSubtitleLabel.setTextFill(Color.GRAY);
                if (remNoAccountsLabel != null) remNoAccountsLabel.setTextFill(Color.GRAY);
                if (remBackButton != null) remBackButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
                if (accountsBox != null) {
                    for (Node node : accountsBox.getChildren()) {
                        if (node instanceof HBox) {
                            node.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-radius: 10; -fx-border-width: 2;");
                            for (Node child : ((HBox) node).getChildren()) {
                                if (child instanceof Label) ((Label) child).setTextFill(Color.BLACK);
                                if (child instanceof Button) {
                                    Button btn = (Button) child;
                                    if ("Giriş Yap".equals(btn.getText())) {
                                        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                                    }
                                }
                            }
                        }
                    }
                }

                if (inboxCount != null) inboxCount.setTextFill(Color.WHITE);
                break;
        }

        if (mainScreen != null) mainScreen.setStyle("-fx-background-color: " + mainBg + ";");
        if (menuBox != null) menuBox.setStyle("-fx-background-color: " + menuBg + ";");
        if (topBar != null) topBar.setStyle("-fx-background-color: " + topBarBg + ";");

        if (loginScreen != null) loginScreen.setStyle("-fx-background-color: " + loginBg + ";");
        if (signinScreen != null) signinScreen.setStyle("-fx-background-color: " + loginBg + ";");
        if (rememberedScreen != null) rememberedScreen.setStyle("-fx-background-color: " + loginBg + ";");

        if (inboxButton != null) inboxButton.setStyle(menuBtnStyle);
        if (outboxButton != null) outboxButton.setStyle(menuBtnStyle);
        if (composeButton != null) composeButton.setStyle(menuBtnStyle);
        if (starredButton != null) starredButton.setStyle(menuBtnStyle);

        if (emailContentArea != null) {
            if (currentTheme == Theme.DARK) {
                emailContentArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: 14px;");
            } else {
                emailContentArea.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-font-size: 14px;");
            }
        }
    }

    private void selectProfileImage(ImageView targetImageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Profil Fotoğrafı Seç");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                Image originalImage = new Image(file.toURI().toString());
                Image resizedImage = resizeImageTo4_3(originalImage);
                targetImageView.setImage(resizedImage);

                if (targetImageView == mainProfileImageView && currentUser != null) {
                    String base64Image = imageToBase64(resizedImage);
                    currentUser.setProfileImage(base64Image);
                    saveUsersToJson();
                    showAlert("Başarılı", "Profil fotoğrafınız güncellendi!");
                }
            } catch (Exception e) {
                showAlert("Hata", "Resim yüklenirken hata oluştu: " + e.getMessage());
            }
        }
    }

    private Image resizeImageTo4_3(Image original) {
        int targetWidth = 400;
        int targetHeight = 300;

        WritableImage resized = new WritableImage(targetWidth, targetHeight);
        PixelReader reader = original.getPixelReader();

        double scaleX = original.getWidth() / targetWidth;
        double scaleY = original.getHeight() / targetHeight;

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int origX = (int) (x * scaleX);
                int origY = (int) (y * scaleY);

                if (origX < original.getWidth() && origY < original.getHeight()) {
                    resized.getPixelWriter().setColor(x, y, reader.getColor(origX, origY));
                }
            }
        }

        return resized;
    }

    private String imageToBase64(Image image) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        PixelReader pixelReader = image.getPixelReader();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        writeChunk(baos, "IHDR", createIHDR(width, height));

        byte[] imageData = new byte[height * (1 + width * 4)];
        int idx = 0;

        for (int y = 0; y < height; y++) {
            imageData[idx++] = 0;
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                imageData[idx++] = (byte) (color.getRed() * 255);
                imageData[idx++] = (byte) (color.getGreen() * 255);
                imageData[idx++] = (byte) (color.getBlue() * 255);
                imageData[idx++] = (byte) (color.getOpacity() * 255);
            }
        }

        byte[] compressedData = compress(imageData);
        writeChunk(baos, "IDAT", compressedData);
        writeChunk(baos, "IEND", new byte[0]);

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private byte[] createIHDR(int width, int height) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeInt(baos, width);
            writeInt(baos, height);
            baos.write(8);
            baos.write(6);
            baos.write(0);
            baos.write(0);
            baos.write(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void writeChunk(ByteArrayOutputStream baos, String type, byte[] data) throws IOException {
        writeInt(baos, data.length);
        baos.write(type.getBytes());
        baos.write(data);

        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(type.getBytes());
        crc.update(data);
        writeInt(baos, (int) crc.getValue());
    }

    private void writeInt(ByteArrayOutputStream baos, int value) throws IOException {
        baos.write((value >> 24) & 0xFF);
        baos.write((value >> 16) & 0xFF);
        baos.write((value >> 8) & 0xFF);
        baos.write(value & 0xFF);
    }

    private byte[] compress(byte[] data) throws IOException {
        java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            baos.write(buffer, 0, count);
        }

        deflater.end();
        return baos.toByteArray();
    }

    private Image base64ToImage(String base64String) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            return new Image(bais);
        } catch (Exception e) {
            System.err.println("Base64'ten resim oluşturulamadı: " + e.getMessage());
            return null;
        }
    }

    private boolean authenticateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.authenticate(password)) {
                currentUser = user;
                inboxEmails.setAll(user.getInbox());
                outboxEmails.setAll(user.getOutbox());
                return true;
            }
        }
        return false;
    }

    private void saveUsersToJson() {
        try (FileWriter writer = new FileWriter("user.json")) {
            gson.toJson(users, writer);
            System.out.println("Kullanıcılar JSON'a kaydedildi!");
        } catch (IOException e) {
            System.err.println("JSON kaydedilirken hata oluştu: " + e.getMessage());
            showAlert("Hata", "Veriler kaydedilemedi!");
        }
    }

    private void openEmailInNewWindow(Email email) {
        if (email == null) return;

        Stage newStage = new Stage();
        newStage.setTitle("BoosTMail - " + email.getSubject());

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Temaya göre arka plan
        String bgColor = (currentTheme == Theme.DARK) ? "#1e1e1e" : "#ffffff";
        root.setStyle("-fx-background-color: " + bgColor + ";");

        Label subjectLabel = new Label("Konu: " + email.getSubject());
        subjectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label senderLabel = new Label("Kimden: " + email.getSender());
        senderLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label dateLabel = new Label("Tarih: " + email.getTimestamp());
        dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        // Metin renkleri temaya göre
        Color textColor = (currentTheme == Theme.DARK) ? Color.WHITE : Color.BLACK;
        subjectLabel.setTextFill(textColor);
        senderLabel.setTextFill(textColor);
        dateLabel.setTextFill(textColor);

        TextArea contentArea = new TextArea(email.getContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        if (currentTheme == Theme.DARK) {
            contentArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: 14px;");
        } else {
            contentArea.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-font-size: 14px;");
        }

        root.getChildren().addAll(subjectLabel, senderLabel, dateLabel, contentArea);

        Scene scene = new Scene(root, 500, 400);
        newStage.setScene(scene);

        try {
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {}

        newStage.show();
    }


    private void refreshData() {
        //JSON dosyasından güncel kayıtlıları tekrar oku
        if (currentUser != null) {
            // Mevcut kullanıcının listelerini ObservableList'lere tekrar aktar
            inboxEmails.setAll(currentUser.getInbox());
            outboxEmails.setAll(currentUser.getOutbox());

            // Gelen sekmesinde gelenleri tutan sayacı güncelle
            if (inboxCount != null) {
                inboxCount.setText("Gelen Kutusu: " + inboxEmails.size() + " e-mail");
            }
        }
    }

    private void showInbox() {
        VBox centerBox = new VBox();

        Label inboxTitle = new Label("Gelen Kutusu");
        inboxTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        inboxTitle.setPadding(new Insets(10));
        inboxTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        //Yenileme butonu oluştur
        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
        refreshBtn.setTooltip(new Tooltip("Yenile"));
        if(currentTheme == Theme.DARK){
            refreshBtn.setTextFill(Color.WHITE);
        }

        inboxTitle.setGraphic(refreshBtn);
        inboxTitle.setContentDisplay(ContentDisplay.RIGHT); // Butonu sağa koy
        inboxTitle.setGraphicTextGap(15); // Yazı ile buton arasındaki boşluk

        // Butona basınca yapılacak işlem:
        refreshBtn.setOnAction(e -> {
            refreshData();
            emailListView.refresh(); // Listeyi güncelle
            applyTheme();       // Genel tema ayarlarını tekrar uygula (renk değişkenleri vs.)
            showInbox();
        });



        emailListView = new ListView<>(inboxEmails);
        emailListView.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333;" : "");
                } else {
                    HBox cellBox = new HBox(10);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(cellBox, Priority.ALWAYS);

                    Button starButton = new Button(email.isStarred() ? "🌟" : "☆");
                    starButton.setStyle("-fx-text-fill: " + (currentTheme == Theme.DARK ? "white" : "black") + "; -fx-font-size: 15px;-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5; -fx-border-color: transparent;");
                    starButton.setOnAction(e -> {
                        email.toggleStar();
                        emailListView.refresh();
                        saveUsersToJson();
                    });

                    Label textLabel = new Label(email.getPreview());
                    textLabel.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);
                    if (!email.isRead()) {
                        textLabel.setStyle("-fx-font-weight: bold;");
                    }

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button deleteButton = new Button("🗑");
                    deleteButton.setStyle("-fx-background-color: transparent; -fx-font-size:20px; -fx-text-fill: " + (currentTheme == Theme.DARK ? "#ff6b6b" : "red") + "; -fx-cursor: hand;");
                    deleteButton.setTooltip(new Tooltip("E-maili Sil"));
                    deleteButton.setOnAction(e -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("E-mail Sil");
                        alert.setHeaderText("Bu e-maili silmek istediğinize emin misiniz?");
                        alert.setContentText(email.getSubject());

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                currentUser.getInbox().remove(email);
                                inboxEmails.remove(email);
                                saveUsersToJson();
                                emailContentArea.clear();
                            }
                        });
                    });

                    cellBox.getChildren().addAll(starButton, textLabel, spacer, deleteButton);
                    setGraphic(cellBox);

                    if (!email.isRead()) {
                        setStyle("-fx-background-color: " + (currentTheme == Theme.DARK ? "#4a4a4a" : "#e8f4fc") + ";");
                    } else {
                        setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333; -fx-text-fill: white;" : "");
                    }
                }
            }
        });

        // "Yeni Pencerede Aç" butonu
        Button openNewWindowBtn = new Button("Yeni Pencerede Aç ↗");
        String btnColor = (currentTheme == Theme.LIGHT) ? "#3498db" : "#8B0000";
        openNewWindowBtn.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; -fx-cursor: hand;");
        openNewWindowBtn.setDisable(true); // Başlangıçta pasif

        emailContentArea = new TextArea();
        emailContentArea.setEditable(false);
        emailContentArea.setWrapText(true);
        emailContentArea.setPrefHeight(300);

        if(currentTheme == Theme.DARK) emailContentArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: 14px;");
        else emailContentArea.setStyle("-fx-font-size: 14px;");

        // StackPane ile Overlay - Butonu TextArea üzerine yerleştiriyoruz
        StackPane mailStack = new StackPane(emailContentArea, openNewWindowBtn);
        StackPane.setAlignment(openNewWindowBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(openNewWindowBtn, new Insets(10)); // Kenardan 10px boşluk

        emailListView.getSelectionModel().selectedItemProperty().addListener((obs, oldEmail, newEmail) -> {
            if (newEmail != null) {
                newEmail.markAsRead();
                emailListView.refresh();
                saveUsersToJson();

                String content = "Kimden: " + newEmail.getSender() + "\n" +
                        "Kime: " + newEmail.getRecipient() + "\n" +
                        "Tarih: " + newEmail.getTimestamp() + "\n" +
                        "Konu: " + newEmail.getSubject() + "\n\n" +
                        newEmail.getContent();
                emailContentArea.setText(content);
                openNewWindowBtn.setDisable(false); // Seçim yapılınca aktif
            } else {
                openNewWindowBtn.setDisable(true);
            }
        });

        // Buton aksiyonu
        openNewWindowBtn.setOnAction(e -> openEmailInNewWindow(emailListView.getSelectionModel().getSelectedItem()));

        centerBox.getChildren().addAll(inboxTitle, refreshBtn, emailListView, mailStack);
        mainScreen.setCenter(centerBox);
    }

    private void showOutbox() {
        VBox centerBox = new VBox();

        Label outboxTitle = new Label("Gönderilenler Kutusu");
        outboxTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        outboxTitle.setPadding(new Insets(10));
        outboxTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        emailListView = new ListView<>(outboxEmails);
        emailListView.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333;" : "");
                } else {
                    HBox cellBox = new HBox(10);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(cellBox, Priority.ALWAYS);

                    Button starButton = new Button(email.isStarred() ? "🌟" : "☆");
                    starButton.setStyle("-fx-text-fill: " + (currentTheme == Theme.DARK ? "white" : "black") + "; -fx-font-size: 15px;-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5; -fx-border-color: transparent;");
                    starButton.setOnAction(e -> {
                        email.toggleStar();
                        emailListView.refresh();
                        saveUsersToJson();
                    });

                    Label textLabel = new Label(email.getPreview());
                    textLabel.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);
                    if (!email.isRead()) {
                        textLabel.setStyle("-fx-font-weight: bold;");
                    }

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button deleteButton = new Button("🗑");
                    deleteButton.setStyle("-fx-background-color: transparent; -fx-font-size:20px; -fx-text-fill: " + (currentTheme == Theme.DARK ? "#ff6b6b" : "red") + "; -fx-cursor: hand;");
                    deleteButton.setTooltip(new Tooltip("E-maili Sil"));
                    deleteButton.setOnAction(e -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("E-mail Sil");
                        alert.setHeaderText("Bu e-maili silmek istediğinize emin misiniz?");
                        alert.setContentText(email.getSubject());

                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                currentUser.getOutbox().remove(email);
                                outboxEmails.remove(email);
                                saveUsersToJson();
                                emailContentArea.clear();
                            }
                        });
                    });

                    cellBox.getChildren().addAll(starButton, textLabel, spacer, deleteButton);
                    setGraphic(cellBox);

                    if (!email.isRead()) {
                        setStyle("-fx-background-color: " + (currentTheme == Theme.DARK ? "#4a4a4a" : "#e8f4fc") + ";");
                    } else {
                        setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333; -fx-text-fill: white;" : "");
                    }
                }
            }
        });

        Button openNewWindowBtn = new Button("Yeni Pencerede Aç ↗");
        String btnColor = (currentTheme == Theme.LIGHT) ? "#3498db" : "#8B0000";
        openNewWindowBtn.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; -fx-cursor: hand;");
        openNewWindowBtn.setDisable(true);

        emailContentArea = new TextArea();
        emailContentArea.setEditable(false);
        emailContentArea.setWrapText(true);
        emailContentArea.setPrefHeight(300);

        if(currentTheme == Theme.DARK) emailContentArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: 14px;");
        else emailContentArea.setStyle("-fx-font-size: 14px;");

        StackPane mailStack = new StackPane(emailContentArea, openNewWindowBtn);
        StackPane.setAlignment(openNewWindowBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(openNewWindowBtn, new Insets(10));

        emailListView.getSelectionModel().selectedItemProperty().addListener((obs, oldEmail, newEmail) -> {
            if (newEmail != null) {
                newEmail.markAsRead();
                emailListView.refresh();
                saveUsersToJson();

                String content = "Kimden: " + newEmail.getSender() + "\n" +
                        "Kime: " + newEmail.getRecipient() + "\n" +
                        "Tarih: " + newEmail.getTimestamp() + "\n" +
                        "Konu: " + newEmail.getSubject() + "\n\n" +
                        newEmail.getContent();
                emailContentArea.setText(content);
                openNewWindowBtn.setDisable(false);
            } else {
                openNewWindowBtn.setDisable(true);
            }
        });

        openNewWindowBtn.setOnAction(e -> openEmailInNewWindow(emailListView.getSelectionModel().getSelectedItem()));

        centerBox.getChildren().addAll(outboxTitle, emailListView, mailStack);
        mainScreen.setCenter(centerBox);
    }

    private void showStarredEmails() {
        VBox centerBox = new VBox();
        centerBox.setPadding(new Insets(10));

        Label starredTitle = new Label("🌟 Yıldızlı Mesajlar");
        starredTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        starredTitle.setPadding(new Insets(10));
        starredTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        ObservableList<Email> starredEmails = FXCollections.observableArrayList();
        starredEmails.addAll(
                currentUser.getInbox().stream()
                        .filter(Email::isStarred)
                        .collect(Collectors.toList()));
        starredEmails.addAll(currentUser.getOutbox().stream()
                .filter(Email::isStarred)
                .collect(Collectors.toList()));

        ListView<Email> starredListView = new ListView<>(starredEmails);
        starredListView.setCellFactory(param -> new ListCell<Email>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(currentTheme == Theme.DARK ? "-fx-background-color: #333333;" : "");
                } else {
                    HBox cellBox = new HBox(10);
                    cellBox.setAlignment(Pos.CENTER_LEFT);

                    Button starButton = new Button("🌟");
                    starButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16px;");
                    starButton.setOnAction(e -> {
                        email.toggleStar();
                        starredEmails.setAll(
                                currentUser.getInbox().stream()
                                        .filter(Email::isStarred)
                                        .collect(Collectors.toList())
                        );
                        saveUsersToJson();
                    });

                    Label textLabel = new Label(email.getPreview());
                    textLabel.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);
                    if (!email.isRead()) {
                        textLabel.setStyle("-fx-font-weight: bold;");
                    }

                    cellBox.getChildren().addAll(starButton, textLabel);
                    setGraphic(cellBox);

                    if (currentTheme == Theme.DARK) {
                        setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        Button openNewWindowBtn = new Button("Yeni Pencerede Aç ↗");
        String btnColor = (currentTheme == Theme.LIGHT) ? "#3498db" : "#8B0000";
        openNewWindowBtn.setStyle("-fx-background-color: " + btnColor + "; -fx-text-fill: white; -fx-cursor: hand;");
        openNewWindowBtn.setDisable(true);

        TextArea starredContentArea = new TextArea();
        starredContentArea.setEditable(false);
        starredContentArea.setWrapText(true);
        starredContentArea.setPrefHeight(300);
        if(currentTheme == Theme.DARK) starredContentArea.setStyle("-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-font-size: 14px;");
        else starredContentArea.setStyle("-fx-font-size: 14px;");

        StackPane mailStack = new StackPane(starredContentArea, openNewWindowBtn);
        StackPane.setAlignment(openNewWindowBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(openNewWindowBtn, new Insets(10));

        starredListView.getSelectionModel().selectedItemProperty().addListener((obs, oldEmail, newEmail) -> {
            if (newEmail != null) {
                newEmail.markAsRead();
                starredListView.refresh();
                saveUsersToJson();

                String content = "Kimden: " + newEmail.getSender() + "\n" +
                        "Kime: " + newEmail.getRecipient() + "\n" +
                        "Tarih: " + newEmail.getTimestamp() + "\n" +
                        "Konu: " + newEmail.getSubject() + "\n\n" +
                        newEmail.getContent();
                starredContentArea.setText(content);
                openNewWindowBtn.setDisable(false);
            } else {
                openNewWindowBtn.setDisable(true);
            }
        });

        openNewWindowBtn.setOnAction(e -> openEmailInNewWindow(starredListView.getSelectionModel().getSelectedItem()));

        centerBox.getChildren().addAll(starredTitle, starredListView, mailStack);
        mainScreen.setCenter(centerBox);
    }

    private void showComposeScreen() {
        VBox composeBox = new VBox(15);
        composeBox.setPadding(new Insets(20));

        Label composeTitle = new Label("Yeni E-mail");
        composeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        composeTitle.setTextFill(currentTheme == Theme.DARK ? Color.WHITE : Color.BLACK);

        TextField toField = new TextField();
        toField.setPromptText("Kime (E-mail adresi)");

        TextField subjectField = new TextField();
        subjectField.setPromptText("Konu");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("E-mail içeriğinizi buraya yazın...");
        messageArea.setPrefHeight(300);
        messageArea.setWrapText(true);

        if (currentTheme == Theme.DARK) {
            String darkInputStyle = "-fx-control-inner-background: #333333; -fx-text-fill: white; -fx-prompt-text-fill: gray;";
            toField.setStyle(darkInputStyle);
            subjectField.setStyle(darkInputStyle);
            messageArea.setStyle(darkInputStyle);
        }

        HBox buttonBox = new HBox(10);
        Button sendButton = new Button("Gönder");
        sendButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        Button cancelButton = new Button("İptal");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");

        buttonBox.getChildren().addAll(sendButton, cancelButton);

        sendButton.setOnAction(e -> {
            String recipient = toField.getText();
            String subject = subjectField.getText();
            String content = messageArea.getText();
            if (recipient.isEmpty() || subject.isEmpty() || content.isEmpty()) {
                showAlert("Hata", "Lütfen tüm alanları doldurun!");
                return;
            }

            Email newEmail = new Email(currentUser.getUsername(), recipient, subject, content);

            currentUser.addEmailtoOutbox(newEmail);

            boolean recipientFound = false;
            for (User user : users) {
                if (user.getUsername().equals(recipient)) {
                    user.addEmail(newEmail);
                    recipientFound = true;
                    saveUsersToJson();
                    break;
                }
            }
            saveUsersToJson();

            if (recipientFound) {
                showAlert("Başarılı", "E-mail başarıyla gönderildi!");
                showInbox();
            } else {
                showAlert("Bilgi", "E-mail gönderildi (alıcı sistemde kayıtlı değil, simülasyon amaçlı)");
            }
        });

        cancelButton.setOnAction(e -> showInbox());

        composeBox.getChildren().addAll(composeTitle, toField, subjectField, messageArea, buttonBox);
        mainScreen.setCenter(composeBox);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }
}