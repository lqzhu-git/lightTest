import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * @author EFL-zlq
 */
public class lightTestController implements Initializable {

    @FXML
    private Spinner<Integer> readTimesSpinner;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button connectButton;

    @FXML
    private Spinner<Integer> lightIntensitySpinner;

    @FXML
    private ComboBox<String> dlpPortComboBox;


    @FXML
    private TextArea intensityArea;

    @FXML
    private LineChart lineChart;

    @FXML
    private Button stopReadButton;

    @FXML
    private Button readButton;

    LightReadPort lightReadPort = new LightReadPort();
    public SerialPort serialPort;
    public static lightTestController controller;

    private XYChart.Series<Number, Number> dlpIntensity;
    private boolean onReading = false;
    public double intensity;
    private long time;
    private int readTimes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ThreadTool.init();
        initPortComboBox();
        initChart();
        controller = this;
        lightIntensitySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (serialPort != null) {
                try {
                    if (lightIntensitySpinner.isFocused()) {
                        serialPort.writeString("WT+LEDS=" + newValue + "\r\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initChart() {
        dlpIntensity = new XYChart.Series<>();
        dlpIntensity.setName("光强");
        xAxis.setTickUnit(60 * 1000);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String toString(Number object) {
                return format.format(new Date(object.longValue()));
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });
        lineChart.getData().add(dlpIntensity);
    }

    public void connect() {
        lightReadPort.connect();
        connectButton.setDisable(true);
        readButton.setDisable(false);
        stopReadButton.setDisable(false);
        intensityArea.setText("");
    }

    @FXML
    public void readIntensity() {
        intensityArea.setText("");
        readTimes = readTimesSpinner.getValue();
        onReading = true;
        time = System.currentTimeMillis();
        ThreadTool.execute(()->{
            xAxis.setLowerBound(time);
            while (readTimes > 0) {
                readTimes --;
                time = System.currentTimeMillis();
                intensity = lightReadPort.read() / 1000;
                Platform.runLater(() -> {
                    intensityArea.appendText(intensity + "\n");
                    dlpIntensity.getData().add(new XYChart.Data<>(time, intensity));
                });
                xAxis.setUpperBound(time + 60 * 1000);
            }
            JOptionPane.showMessageDialog(null,"测试完成","测试完成提示框",JOptionPane.INFORMATION_MESSAGE);
        });
    }


    /**
     * 初始化或刷新端口下拉框函数
     * 在启动和点击下拉框时触发
     */
    public void initPortComboBox() {
        //查找目前可用的所有串口号
        String[] coms = SerialPortList.getPortNames("COM");
        //清空下拉框
        dlpPortComboBox.getItems().clear();
        //初始化串口下拉框
        dlpPortComboBox.getItems().addAll(coms);
    }

    public void openDLP() {
        try {
            lightIntensitySpinner.setDisable(false);
            int intensity = lightIntensitySpinner.getValue();
            serialPort.writeString("WT+LEDS=" + intensity + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeDLP() {
        if (serialPort != null) {
            try {
                serialPort.writeString("WT+LEDS=0" + "\r\n");
                lightIntensitySpinner.setDisable(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clickDlpPortComboBox() {
        String portName;
        if (dlpPortComboBox.getValue() == null) {
            return;
        } else if (serialPort == null) {
            portName = dlpPortComboBox.getValue();
            connectDLP(portName);
        } else {
            disConnectDLP();
            portName = dlpPortComboBox.getValue();
            connectDLP(portName);
        }
    }

    /**
     * 根据给定的端口名字和波特率来进行串口连接
     *
     * @param portName 端口号
     */
    private void connectDLP(String portName) {
        try {
            //实例化串口对象
            serialPort = new SerialPort(portName);
            //打开串口
            serialPort.openPort();
            //设置串口
            serialPort.setParams(115200, 8, 1, 0);
            //实例化串口监听器
            PortListener portListener = new PortListener();
            //添加串口监听器
            serialPort.addEventListener(portListener, 511 - 4);
            openDLP();
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    public void disConnectDLP() {
        try {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.closePort();
                serialPort = null;
            }
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    public void stopRead() {
        readTimes = 0;
        connectButton.setDisable(false);
        readButton.setDisable(true);
        stopReadButton.setDisable(true);
        lightReadPort.closePort();
    }
}
