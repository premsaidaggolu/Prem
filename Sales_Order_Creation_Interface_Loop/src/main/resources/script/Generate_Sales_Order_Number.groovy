import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def randomOrderNumber = (100000000 + new Random().nextInt(900000000)).toString();
    message.setProperty('SalesOrderNumber', randomOrderNumber);
    return message;
}