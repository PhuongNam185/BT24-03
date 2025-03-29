package BTSV;

// XmlWriter.java
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class XmlWriter {

    private static final String FILENAME = "students.xml"; // Tên file XML

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Student student = getStudentDetails(scanner);
            if (student == null) {
                continue; // Nếu nhập liệu không hợp lệ, yêu cầu nhập lại
            }

            try {
                addStudentToXml(student);
                System.out.println("\nĐã thêm thông tin sinh viên có ID '" + student.getId() + "' vào file " + FILENAME);
            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                System.err.println("Lỗi khi ghi vào file XML: " + e.getMessage());
                e.printStackTrace(); // In chi tiết lỗi để debug
            }

            System.out.print("\nBạn có muốn thêm sinh viên khác không? (nhập 'c' để tiếp tục, ký tự khác để thoát): ");
            String continueInput = scanner.nextLine().trim().toLowerCase();
            if (!continueInput.equals("c")) {
                break;
            }
        }

        scanner.close();
        System.out.println("Chương trình kết thúc.");
    }

    /**
     * Lấy thông tin chi tiết của sinh viên từ người dùng.
     * @param scanner Đối tượng Scanner để đọc input.
     * @return Đối tượng Student hoặc null nếu nhập liệu không hợp lệ.
     */
    private static Student getStudentDetails(Scanner scanner) {
        System.out.println("\n--- Nhập thông tin sinh viên ---");
        String id = "";
        while (id.isEmpty()) {
            System.out.print("Nhập ID sinh viên: ");
            id = scanner.nextLine().trim();
            if (id.isEmpty()) {
                System.out.println("Lỗi: ID không được để trống.");
            }
        }


        System.out.print("Nhập tên sinh viên: ");
        String name = scanner.nextLine().trim();

        int age = 0;
        while (age <= 0) {
            System.out.print("Nhập tuổi sinh viên: ");
            try {
                age = scanner.nextInt();
                if (age <= 0) {
                    System.out.println("Lỗi: Tuổi phải là một số dương.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Lỗi: Vui lòng nhập một số nguyên hợp lệ cho tuổi.");
                scanner.next(); // Xóa input không hợp lệ khỏi buffer
            } finally {
                // Luôn đọc dòng mới sau khi đọc số để tránh lỗi ở lần đọc chuỗi tiếp theo
                if(scanner.hasNextLine()) scanner.nextLine();
            }
        }


        System.out.print("Nhập chuyên ngành: ");
        String major = scanner.nextLine().trim();

        return new Student(id, name, age, major);
    }

    /**
     * Thêm thông tin sinh viên vào file XML.
     * Nếu file không tồn tại, tạo file mới.
     * Nếu file tồn tại, đọc và thêm vào cấu trúc hiện có.
     *
     * @param student Đối tượng Student chứa thông tin cần thêm.
     * @throws ParserConfigurationException Lỗi cấu hình trình phân tích XML.
     * @throws TransformerException Lỗi trong quá trình chuyển đổi XML.
     * @throws IOException Lỗi I/O khi đọc/ghi file.
     * @throws SAXException Lỗi khi phân tích cú pháp XML hiện có.
     */
    private static void addStudentToXml(Student student)
            throws ParserConfigurationException, TransformerException, IOException, SAXException {

        File xmlFile = new File(FILENAME);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc;
        Element rootElement;

        // Kiểm tra file tồn tại và đọc hoặc tạo mới
        if (xmlFile.exists() && xmlFile.length() > 0) {
            // File tồn tại và không trống, cố gắng đọc nó
            try {
                doc = dBuilder.parse(xmlFile);
                rootElement = doc.getDocumentElement();
                // Đảm bảo thẻ gốc là "students"
                if (!rootElement.getNodeName().equals("students")) {
                    System.err.println("Cảnh báo: File " + FILENAME + " có thẻ gốc không phải 'students'. Sẽ tạo lại cấu trúc.");
                    // Tạo lại cấu trúc nếu thẻ gốc sai
                    doc = dBuilder.newDocument();
                    rootElement = doc.createElement("students");
                    doc.appendChild(rootElement);
                }
            } catch (SAXException | IOException e) {
                // Nếu file bị lỗi (không phải XML hợp lệ), tạo mới
                System.err.println("Cảnh báo: File " + FILENAME + " bị lỗi hoặc không hợp lệ. Sẽ tạo lại cấu trúc. Lỗi: " + e.getMessage());
                doc = dBuilder.newDocument();
                rootElement = doc.createElement("students");
                doc.appendChild(rootElement);
            }

        } else {
            // File không tồn tại hoặc trống, tạo mới
            doc = dBuilder.newDocument();
            rootElement = doc.createElement("students"); // Thẻ gốc <students>
            doc.appendChild(rootElement);
            if (!xmlFile.exists()) {
                System.out.println("File '" + FILENAME + "' không tồn tại. Đang tạo file mới...");
            } else {
                System.out.println("File '" + FILENAME + "' trống. Đang tạo cấu trúc XML...");
            }
        }

        // Tạo element <student> và đặt thuộc tính id
        Element studentElement = doc.createElement("student");
        studentElement.setAttribute("id", student.getId());
        rootElement.appendChild(studentElement); // Thêm <student> vào <students>

        // Tạo và thêm các element con vào <student>
        Element nameElement = doc.createElement("name");
        nameElement.appendChild(doc.createTextNode(student.getName()));
        studentElement.appendChild(nameElement);

        Element ageElement = doc.createElement("age");
        ageElement.appendChild(doc.createTextNode(String.valueOf(student.getAge()))); // Chuyển int sang String
        studentElement.appendChild(ageElement);

        Element majorElement = doc.createElement("major");
        majorElement.appendChild(doc.createTextNode(student.getMajor()));
        studentElement.appendChild(majorElement);

        // --- Ghi lại Document vào file XML ---
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // Cấu hình để tránh lỗi "TransformerFactory security processing limitation" trên một số JDK
        try {
            transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException e) {
            // Bỏ qua nếu không hỗ trợ, không nghiêm trọng
            System.err.println("Lưu ý: Không thể cấu hình FEATURE_SECURE_PROCESSING cho TransformerFactory.");
        }

        Transformer transformer = transformerFactory.newTransformer();

        // Cấu hình để output đẹp (thụt lề)
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // Số dấu cách thụt lề
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // Đảm bảo encoding UTF-8

        DOMSource source = new DOMSource(doc);
        // Sử dụng FileOutputStream để đảm bảo ghi đè file đúng cách
        try (FileOutputStream output = new FileOutputStream(xmlFile)) {
            StreamResult result = new StreamResult(output);
            transformer.transform(source, result);
        }
    }
}
