import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * @author EFL-zlq
 */
public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            //关联fxml文件,根据xml文件实例化一个root对象
            Parent root = FXMLLoader.load(getClass().getResource("/lightTestFrame.fxml"));
            //设置窗口名称
            primaryStage.setTitle("光强测试");
            //根据root对象来实例化窗口
            primaryStage.setScene(new Scene(root, 1500, 500));
            //使窗口可见
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        lightTestController.controller.closeDLP();
        lightTestController.controller.disConnectDLP();
        System.exit(0);
    }
}
