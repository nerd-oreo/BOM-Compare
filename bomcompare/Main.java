package bomcompare;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private static String templateURL = "";
    private static String reportURL = "";

    // UI Controls
    private TextField loadTemplatePath, saveReportPath;
    private Button loadTemplateButton, saveReportButton;
    private Button compareButton, createNewTemplateButton;

    private ProgressBar progressBar;
    private Label progressStatus;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Top Pane

        // Middle Pane
        Label loadTemplateLabel = new Label("Load template:");
        loadTemplateLabel.setPrefWidth(90);
        loadTemplatePath = new TextField();
        loadTemplatePath.setPrefWidth(300);
        //loadTemplatePath.setEditable(false);
        loadTemplatePath.setFocusTraversable(false);
        loadTemplateButton = new Button("...");

        HBox loadTemplatePane = new HBox();
        loadTemplatePane.setSpacing(5);
        loadTemplatePane.setAlignment(Pos.CENTER);
        loadTemplatePane.getChildren().addAll(loadTemplateLabel, loadTemplatePath, loadTemplateButton);

        Label saveReportLabel = new Label("Save template:");
        saveReportLabel.setPrefWidth(90);
        saveReportPath = new TextField();
        saveReportPath.setPrefWidth(300);
        //saveReportPath.setEditable(false);
        saveReportPath.setFocusTraversable(false);
        saveReportButton = new Button("...");

        HBox saveTemplatePane = new HBox();
        saveTemplatePane.setSpacing(5);
        saveTemplatePane.setAlignment(Pos.CENTER);
        saveTemplatePane.getChildren().addAll(saveReportLabel, saveReportPath, saveReportButton);

        VBox templateWrapper = new VBox();
        templateWrapper.getChildren().addAll(loadTemplatePane, saveTemplatePane);
        templateWrapper.setSpacing(5);
        templateWrapper.setAlignment(Pos.CENTER);

        Image compareIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon/refresh.png"));
        ImageView ivCompareIcon = new ImageView(compareIcon);
        ivCompareIcon.setFitHeight(30);
        ivCompareIcon.setFitWidth(30);
        compareButton = new Button("Compare");
        compareButton.setTooltip(new Tooltip("Run Comparison"));
        compareButton.setGraphic(ivCompareIcon);
        compareButton.setContentDisplay(ContentDisplay.TOP);
        compareButton.setPrefSize(75, 75);

        Image newTemplateIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("icon/add.png"));
        ImageView ivNewTemplateIcon = new ImageView(newTemplateIcon);
        ivNewTemplateIcon.setFitHeight(30);
        ivNewTemplateIcon.setFitWidth(30);
        createNewTemplateButton = new Button("Template");
        createNewTemplateButton.setTooltip(new Tooltip("Create BOM Comparison template"));
        createNewTemplateButton.setGraphic(ivNewTemplateIcon);
        createNewTemplateButton.setContentDisplay(ContentDisplay.TOP);
        createNewTemplateButton.setPrefSize(75, 75);

        HBox middlePane = new HBox();
        middlePane.getChildren().addAll(templateWrapper, compareButton, createNewTemplateButton);
        middlePane.setSpacing(5);
        middlePane.setPrefSize(600, 70);
        middlePane.setPadding(new Insets(10));
        middlePane.setAlignment(Pos.CENTER);

        // Bottom Pane
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(600);
        progressStatus = new Label("Progress");
        VBox progressPane = new VBox();
        progressPane.setSpacing(5);
        progressPane.setPadding(new Insets(10));
        progressPane.setAlignment(Pos.CENTER_LEFT);
        progressPane.getChildren().addAll(progressStatus, progressBar);

        // Set buttons action
        loadTemplateButton.setOnAction(e -> {
            selectTemplateURL(primaryStage);
        });
        saveReportButton.setOnAction(e -> {
            selectReportURL(primaryStage);
        });
        createNewTemplateButton.setOnAction(e -> {
            createNewTemplate();
        });
        compareButton.setOnAction(e -> {
            runCompare();
        });

        // Root
        VBox root = new VBox();
        root.setSpacing(5);
        root.setPadding(new Insets(5));
        root.getChildren().addAll(middlePane, new Separator(), progressPane);

        Scene scene = new Scene(root, 630, 170);

        primaryStage.setScene(scene);
        primaryStage.setTitle("BOM Comparison v1.0");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void selectTemplateURL(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load BOM Comparison Template");
        fileChooser.setInitialDirectory(Paths.get(".").toAbsolutePath().normalize().toFile());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel workbook", "*.xlsx"));
        File template = fileChooser.showOpenDialog(stage);

        if (template != null) {
            templateURL = template.getAbsolutePath();
            loadTemplatePath.setText(templateURL);
        } else {
            loadTemplatePath.setText(templateURL);
        }
    }

    private void selectReportURL(Stage stage) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Save BOM Report in");
        dirChooser.setInitialDirectory(Paths.get(".").toAbsolutePath().normalize().toFile());
        File reportDir = dirChooser.showDialog(stage);

        if (reportDir != null) {
            reportURL = reportDir.getAbsolutePath();
            saveReportPath.setText(reportURL);
        } else {
            saveReportPath.setText(reportURL);
        }

    }

    private void runCompare() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle("Error");
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);

        progressStatus.textProperty().unbind();

        if (templateURL.equals("") || templateURL == null) {
            alert.setContentText("Please provide BOM Comparison template.");
            alert.showAndWait();
        } else if (reportURL.equals("") || reportURL == null) {
            alert.setContentText("Please choose a folder to save BOM report.");
            alert.showAndWait();
        } else {
            BOMComparison bomCompare = new BOMComparison(templateURL, reportURL);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);

            progressStatus.textProperty().unbind();
            progressStatus.textProperty().bind(bomCompare.messageProperty());

            progressBar.progressProperty().bind(bomCompare.progressProperty());

            loadTemplateButton.setDisable(true);
            saveReportButton.setDisable(true);
            compareButton.setDisable(true);
            createNewTemplateButton.setDisable(true);

            bomCompare.setOnSucceeded(e -> {
                progressStatus.textProperty().unbind();
                progressStatus.setText("Completed");
                loadTemplateButton.setDisable(false);
                saveReportButton.setDisable(false);
                compareButton.setDisable(false);
                createNewTemplateButton.setDisable(false);
            });
            new Thread(bomCompare).start();

        }

    }

    private void createNewTemplate() {
        Alert alert;

        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();

        InputStream defaultTemplate = this.getClass().getClassLoader().getResourceAsStream("excel/BomComparisonTemplate.xlsx");
        File newTemplate = new File(cwd + "\\BomComparisonTemplate.xlsx");

        try {
            //FileUtils.copyInputStreamToFile(defaultTemplate, newTemplate);;
            Files.copy(defaultTemplate, newTemplate.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (newTemplate.exists()) {
                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText(null);
                alert.setContentText("Template has been created! \n" + newTemplate.getAbsolutePath());
                alert.showAndWait();
            } else {
                alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setTitle("Error");
                alert.setContentText("Failed to create new template.");
                alert.showAndWait();
            }
        } catch (Exception e1) {
            alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Failed to create new template.");
            alert.setTitle("Error");
            VBox dialogPaneContent = new VBox();
            Label label = new Label("Stack Trace:");
            String stackTrace = this.getStackTrace(e1);
            TextArea textArea = new TextArea();
            textArea.setText(stackTrace);
            textArea.setEditable(false);
            dialogPaneContent.getChildren().addAll(label, textArea);
            alert.getDialogPane().setContent(dialogPaneContent);
            alert.showAndWait();
        }
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        return s;
    }

}
