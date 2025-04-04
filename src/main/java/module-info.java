module popint.yyoouu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;


    opens popint.yyoouu to javafx.fxml;
    exports popint.yyoouu;
    exports popint.whiteboard;
    opens popint.whiteboard to javafx.fxml;
}