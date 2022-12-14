package liquibase.util.xml;

import liquibase.GlobalConfiguration;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

@SuppressWarnings("java:S2755")
public class DefaultXmlWriter implements XmlWriter {

    @Override
    public void write(Document doc, OutputStream outputStream) throws IOException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            try {
                factory.setAttribute("indent-number", 4);
            } catch (Exception e) {
                //guess we can't set it, that's ok
            }

            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());

            //need to nest outputStreamWriter to get around JDK 5 bug.  See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            writer.flush();
            writer.close();
        } catch (TransformerException e) {
            throw new IOException(e.getMessage());
        }
    }
}
