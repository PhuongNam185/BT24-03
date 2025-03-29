package BTFind;

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

public class XmlDeleteStudent {

    private static final String FILENAME = "students_data.xml"; // Tên file XML

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // --- Tạo file XML mẫu nếu chưa tồn tại ---
        createSampleXmlIfNotExists();
        // ---------------------------------------

        System.out.print("Nhập ID của sinh viên cần xóa: ");
        String idToDelete = scanner.nextLine().trim();

        if (idToDelete.isEmpty()) {
            System.out.println("Lỗi: ID không được để trống.");
            scanner.close();
            return;
        }

        try {
            boolean deleted = deleteStudentById(idToDelete);
            if (deleted) {
                System.out.println("Đã xóa thành công sinh viên có ID: " + idToDelete);
            } else {
                System.out.println("Không tìm thấy sinh viên nào có ID: " + idToDelete);
            }
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            System.err.println("Đã xảy ra lỗi trong quá trình xử lý XML: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close(); // Đóng scanner
        }
    }

    /**
     * Tìm và xóa phần tử student trong file XML dựa trên thuộc tính id.
     *
     * @param idToDelete ID của sinh viên cần xóa.
     * @return true nếu xóa thành công, false nếu không tìm thấy.
     * @throws ParserConfigurationException Lỗi cấu hình trình phân tích XML.
     * @throws SAXException Lỗi khi phân tích cú pháp XML.
     * @throws IOException Lỗi I/O khi đọc/ghi file.
     * @throws TransformerException Lỗi trong quá trình chuyển đổi (ghi lại) XML.
     */
    private static boolean deleteStudentById(String idToDelete)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        File xmlFile = new File(FILENAME);
        if (!xmlFile.exists()) {
            System.err.println("Lỗi: File " + FILENAME + " không tồn tại để thực hiện xóa.");
            return false; // Không thể xóa nếu file không tồn tại
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);

        // Chuẩn hóa cấu trúc Document
        doc.getDocumentElement().normalize();

        // Lấy thẻ gốc (<students>)
        Element rootElement = doc.getDocumentElement();
        if (!rootElement.getNodeName().equals("students")) {
            System.err.println("Lỗi: File " + FILENAME + " không có thẻ gốc là 'students'.");
            return false;
        }

        // Lấy danh sách tất cả các thẻ <student>
        NodeList studentList = rootElement.getElementsByTagName("student");

        Element studentToDelete = null; // Biến lưu trữ thẻ student cần xóa

        // Duyệt qua danh sách để tìm thẻ student có id khớp
        for (int i = 0; i < studentList.getLength(); i++) {
            Node studentNode = studentList.item(i);
            if (studentNode.getNodeType() == Node.ELEMENT_NODE) {
                Element studentElement = (Element) studentNode;
                String currentId = studentElement.getAttribute("id");
                if (idToDelete.equals(currentId)) {
                    studentToDelete = studentElement; // Tìm thấy thẻ cần xóa
                    break; // Thoát vòng lặp vì đã tìm thấy
                }
            }
        }

        // Nếu tìm thấy phần tử cần xóa
        if (studentToDelete != null) {
            // Lấy thẻ cha (là thẻ <students>) và xóa thẻ con (thẻ <student> đã tìm thấy)
            Node parentNode = studentToDelete.getParentNode();
            parentNode.removeChild(studentToDelete);
            System.out.println("Đã tìm thấy và chuẩn bị xóa sinh viên ID: " + idToDelete);

            // --- Ghi lại Document đã được sửa đổi vào file ---
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            // Cấu hình để tránh lỗi "TransformerFactory security processing limitation"
            try {
                transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerConfigurationException e) {
                System.err.println("Lưu ý: Không thể cấu hình FEATURE_SECURE_PROCESSING cho TransformerFactory.");
            }

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Định dạng output đẹp
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // Đảm bảo standalone="no" để tương thích với việc có thể sửa đổi file
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            DOMSource source = new DOMSource(doc);
            // Ghi đè lên file cũ
            try (FileOutputStream output = new FileOutputStream(xmlFile)) {
                StreamResult result = new StreamResult(output);
                transformer.transform(source, result);
            }
            return true; // Xóa thành công
        } else {
            return false; // Không tìm thấy phần tử để xóa
        }
    }

    /**
     * Tạo file XML mẫu nếu nó chưa tồn tại.
     * Chỉ để tiện cho việc chạy thử chương trình lần đầu.
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