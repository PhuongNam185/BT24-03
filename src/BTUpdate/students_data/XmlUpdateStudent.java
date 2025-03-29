package BTUpdate.students_data;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class XmlUpdateStudent {

    private static final String FILENAME = "students_data.xml"; // Tên file XML

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "UTF-8"); // Sử dụng UTF-8 để hỗ trợ tiếng Việt

        // --- Tạo file XML mẫu nếu chưa tồn tại ---
        createSampleXmlIfNotExists();
        // ---------------------------------------

        System.out.print("Nhập ID của sinh viên cần cập nhật: ");
        String idToUpdate = scanner.nextLine().trim();

        if (idToUpdate.isEmpty()) {
            System.out.println("Lỗi: ID không được để trống.");
            scanner.close();
            return;
        }

        try {
            boolean updated = updateStudentById(idToUpdate, scanner);
            if (updated) {
                System.out.println("\nĐã cập nhật thành công thông tin cho sinh viên có ID: " + idToUpdate);
            } else {
                System.out.println("\nKhông tìm thấy sinh viên nào có ID: " + idToUpdate + " để cập nhật.");
            }
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            System.err.println("Đã xảy ra lỗi trong quá trình xử lý XML: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close(); // Đóng scanner
        }
    }

    /**
     * Tìm sinh viên theo ID, yêu cầu nhập thông tin mới và cập nhật vào file XML.
     *
     * @param idToUpdate ID của sinh viên cần cập nhật.
     * @param scanner    Đối tượng Scanner để đọc thông tin mới từ người dùng.
     * @return true nếu cập nhật thành công, false nếu không tìm thấy hoặc có lỗi.
     * @throws ParserConfigurationException Lỗi cấu hình trình phân tích XML.
     * @throws SAXException Lỗi khi phân tích cú pháp XML.
     * @throws IOException Lỗi I/O khi đọc/ghi file.
     * @throws TransformerException Lỗi trong quá trình chuyển đổi (ghi lại) XML.
     */
    private static boolean updateStudentById(String idToUpdate, Scanner scanner)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        File xmlFile = new File(FILENAME);
        if (!xmlFile.exists()) {
            System.err.println("Lỗi: File " + FILENAME + " không tồn tại để thực hiện cập nhật.");
            return false;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Element rootElement = doc.getDocumentElement();
        if (!rootElement.getNodeName().equals("students")) {
            System.err.println("Lỗi: File " + FILENAME + " không có thẻ gốc là 'students'.");
            return false;
        }

        NodeList studentList = rootElement.getElementsByTagName("student");
        boolean found = false; // Cờ đánh dấu đã tìm thấy sinh viên chưa

        // Duyệt qua danh sách để tìm thẻ student có id khớp
        for (int i = 0; i < studentList.getLength(); i++) {
            Node studentNode = studentList.item(i);
            if (studentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element studentElement = (Element) studentNode;
                String currentId = studentElement.getAttribute("id");

                if (idToUpdate.equals(currentId)) {
                    found = true; // Đánh dấu đã tìm thấy
                    System.out.println("\nTìm thấy sinh viên có ID: " + idToUpdate + ". Vui lòng nhập thông tin mới:");

                    // --- Lấy thông tin mới từ người dùng ---
                    System.out.print("Nhập tên mới: ");
                    String newName = scanner.nextLine().trim();

                    System.out.print("Nhập MSV mới: ");
                    String newMsv = scanner.nextLine().trim();

                    System.out.print("Nhập lớp mới: ");
                    String newClass = scanner.nextLine().trim();

                    // --- Cập nhật các thẻ con bên trong studentElement ---
                    updateTagValue("name", newName, studentElement);
                    updateTagValue("msv", newMsv, studentElement);
                    updateTagValue("class", newClass, studentElement);

                    System.out.println("Đang chuẩn bị ghi lại file...");
                    break; // Thoát vòng lặp vì đã tìm thấy và xử lý
                }
            }
        }

        // Nếu đã tìm thấy và thay đổi thông tin, ghi lại file
        if (found) {
            // --- Ghi lại Document đã được sửa đổi vào file ---
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            try {
                transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
                System.err.println("Lưu ý: Không thể cấu hình FEATURE_SECURE_PROCESSING cho TransformerFactory.");
            }

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            DOMSource source = new DOMSource(doc);
            try (FileOutputStream output = new FileOutputStream(xmlFile)) { // Ghi đè file
                StreamResult result = new StreamResult(output);
                transformer.transform(source, result);
            }
            return true; // Cập nhật thành công
        } else {
            return false; // Không tìm thấy sinh viên
        }
    }

    /**
     * Hàm tiện ích để cập nhật nội dung text của một thẻ con bên trong một thẻ cha.
     *
     * @param tagName Tên của thẻ con cần cập nhật giá trị.
     * @param newValue Giá trị mới cần đặt cho thẻ con.
     * @param parentElement Thẻ cha chứa thẻ con đó.
     * @return true nếu cập nhật thành công, false nếu thẻ con không tồn tại.
     */
    private static boolean updateTagValue(String tagName, String newValue, Element parentElement) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0); // Lấy phần tử đầu tiên tìm thấy
            if (node != null) {
                node.setTextContent(newValue); // Đặt nội dung text mới
                return true;
            }
        }
        System.err.println("Cảnh báo: Không tìm thấy thẻ '" + tagName + "' để cập nhật bên trong sinh viên này.");
        return false;
    }

    /**
     * Tạo file XML mẫu nếu nó chưa tồn tại.
     */
    private static void createSampleXmlIfNotExists() {
        File xmlFile = new File(FILENAME);
        if (!xmlFile.exists()) {
            System.out.println("File " + FILENAME + " không tồn tại. Đang tạo file mẫu...");
            try (FileOutputStream fos = new FileOutputStream(xmlFile)) {
                String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                        "<students>\n" +
                        "    <student id=\"SV001\">\n" +
                        "        <name>Nguyen Van A</name>\n" +
                        "        <msv>19020001</msv>\n" +
                        "        <class>CNTT1-K64</class>\n" +
                        "    </student>\n" +
                        "    <student id=\"SV002\">\n" +
                        "        <name>Tran Thi B</name>\n" +
                        "        <msv>19020002</msv>\n" +
                        "        <class>CNTT2-K64</class>\n" +
                        "    </student>\n" +
                        "    <student id=\"SV003\">\n" +
                        "        <name>Le Van C</name>\n" +
                        "        <msv>19020003</msv>\n" +
                        "        <class>CNTT1-K64</class>\n" +
                        "    </student>\n" +
                        "</students>";
                fos.write(xmlContent.getBytes("UTF-8"));
                System.out.println("Đã tạo file " + FILENAME + " thành công.");
            } catch (IOException e) {
                System.err.println("Lỗi khi tạo file XML mẫu: " + e.getMessage());
            }
        }
    }
}
