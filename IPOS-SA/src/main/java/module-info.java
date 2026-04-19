module com.prototype.ipossa {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;

    exports com.prototype.ipossa;
    exports com.prototype.ipossa.ui;
    exports com.prototype.ipossa.ui.pages;
    exports com.prototype.ipossa.systems;
    exports com.prototype.ipossa.systems.ACC;
    exports com.prototype.ipossa.systems.CAT;
    exports com.prototype.ipossa.systems.ORD;
    exports com.prototype.ipossa.systems.RPRT;

    opens com.prototype.ipossa to javafx.fxml, javafx.graphics;
    opens com.prototype.ipossa.ui to javafx.fxml, javafx.graphics;
    opens com.prototype.ipossa.ui.pages to javafx.fxml, javafx.graphics;
    opens com.prototype.ipossa.systems.ACC to javafx.fxml, javafx.graphics;
    opens com.prototype.ipossa.systems.ORD to javafx.fxml, javafx.graphics;
    opens com.prototype.ipossa.systems.CAT to javafx.fxml, javafx.graphics;
}
