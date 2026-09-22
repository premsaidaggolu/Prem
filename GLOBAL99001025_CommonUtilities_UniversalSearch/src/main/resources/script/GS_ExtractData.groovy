import com.sap.gateway.ip.core.customdev.util.Message
import java.util.zip.ZipInputStream
import java.util.zip.ZipEntry
import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

def Message processData(Message message) {
    // Get the body as an InputStream
    InputStream body = message.getBody(InputStream.class)
    
    // Create a ZipInputStream from the body
    ZipInputStream zipStream = new ZipInputStream(body)
    ZipEntry entry
    String fileContent = null
    String parametersContent = null
    StringBuilder entryNames = new StringBuilder()
    
    // Iterate through the entries in the ZIP file
    while ((entry = zipStream.getNextEntry()) != null) {
        // Append each entry name to the StringBuilder
        entryNames.append(entry.getName()).append("\n")
        
        // Find the .iflw file in the integrationflow folder
        if (entry.getName().matches("src/main/resources/scenarioflows/integrationflow/[^/]+\\.iflw\$")) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            byte[] buffer = new byte[1024]
            int len
            while ((len = zipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len)
            }
            fileContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        }
        
        // Find the parameters.prop file
        if (entry.getName().equals("src/main/resources/parameters.prop")) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            byte[] buffer = new byte[1024]
            int len
            while ((len = zipStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len)
            }
            parametersContent = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        }
    }
    zipStream.closeEntry()
    zipStream.close()
    
    // Set the entry names as a property
    message.setProperty("EntryNames", entryNames.toString())
    
    // Set parameters content as property if found
    if (parametersContent != null) {
        message.setProperty("parameter", parametersContent)
    } else {
        message.setProperty("parameter", "")
    }
    
    // Check if file content was found
    if (fileContent != null) {
        // Set the file content as the message body
        message.setBody(fileContent)
        message.setProperty("Status", "File content set as message body")
    } else {
        message.setProperty("Status", "Target file not found in ZIP")
    }
    
    return message
}
