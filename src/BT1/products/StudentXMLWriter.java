package BT1.products;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StudentXMLWriter {
    public static void main(String[] args) {
        try {
            File xmlFile = new File("students.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc;

            if (xmlFile.exists()) {
                doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
            } else {
                doc = dBuilder.newDocument();
                Element rootElement = doc.createElement("students");
                doc.appendChild(rootElement);
            }

            // Nhập thông tin sinh viên
            String id = "2";  // Ví dụ
            String name = "Jane Doe";
            String age = "22";
            String major = "Mathematics";

            Element newStudent = doc.createElement("student");
            newStudent.setAttribute("id", id);

            Element studentName = doc.createElement("name");
            studentName.appendChild(doc.createTextNode(name));
            newStudent.appendChild(studentName);

            Element studentAge = doc.createElement("age");
            studentAge.appendChild(doc.createTextNode(age));
            newStudent.appendChild(studentAge);

            Element studentMajor = doc.createElement("major");
            studentMajor.appendChild(doc.createTextNode(major));
            newStudent.appendChild(studentMajor);

            doc.getDocumentElement().appendChild(newStudent);

            // Lưu file XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            System.out.println("Thông tin sinh viên đã được ghi vào file students.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}