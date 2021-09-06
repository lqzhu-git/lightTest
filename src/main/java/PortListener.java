import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.time.LocalDateTime;

/**
 * @author EFL-zlq
 */
public class PortListener implements SerialPortEventListener {

    private int i = 0;
    private String buffer = "";


    /**
     * 监听方法，当串口中有数据输出时，自动调用该方法
     * @param serialPortEvent 串口对象存在输入输出时返回的对象
     */
    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        int bufferLength = serialPortEvent.getEventValue();
        try {
            //存储监听得到的数据
            buffer = buffer + lightTestController.controller.serialPort.readString(bufferLength);
            System.out.println("光机返回值---->"+buffer);
            buffer = "";
        }catch (SerialPortException ex){
            ex.printStackTrace();
        }
    }
}
