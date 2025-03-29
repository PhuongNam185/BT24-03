package BT1.products;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class StudentXML {
    public static void addStudentToXML(String fileName, String studentID, String name, int age, String major) {
        try {
            File xmlFile = new File(fileName);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;

            if (xmlFile.exists()) {
                doc = builder.parse(xmlFile);
            } else {
                doc = builder.newDocument();
                Element root = doc.createElement("students");
                doc.appendChild(root);
            }

            Element root = doc.getDocumentElement();

            // Create student element
            Element student = doc.createElement("student");
            student.setAttribute("id", studentID);

            Element nameElem = doc.createElement("name");
            nameElem.setTextContent(name);
            student.appendChild(nameElem);

            Element ageElem = doc.createElement("age");
            ageElem.setTextContent(String.valueOf(age));
            student.appendChild(ageElem);

            Element majorElem = doc.createElement("major");
            majorElem.setTextContent(major);
            student.appendChild(majorElem);

            root.appendChild(student);

            // Save changes to the XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);

            System.out.println("Student information added to " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String fileName = "students.xml";
        addStudentToXML(fileName, "1", "Nguyen Van A", 21, "Computer Science");
    }
}