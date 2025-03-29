package BTRead;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;

public class XmlReader {

    private static final String FILENAME = "company.xml"; // Tên file XML

    public static void main(String[] args) {
        File xmlFile = new File(FILENAME);

        // Kiểm tra xem file XML có tồn tại không
        if (!xmlFile.exists()) {
            System.err.println("Lỗi: File '" + FILENAME + "' không tìm thấy.");
            System.err.println("Vui lòng tạo file " + FILENAME + " với nội dung XML mẫu trước khi chạy.");
            return; // Thoát chương trình nếu file không tồn tại
        }

        try {
            // 1. Tạo đối tượng DocumentBuilderFactory và DocumentBuilder
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // 2. Phân tích (Parse) file XML thành đối tượng Document
            Document doc = dBuilder.parse(xmlFile);

            // 3. Chuẩn hóa cấu trúc Document (quan trọng để xử lý text nodes)
            doc.getDocumentElement().normalize();

            // 4. Lấy thẻ gốc (<company>)
            Element root = doc.getDocumentElement();
            System.out.println("Thẻ gốc: " + root.getNodeName());
            System.out.println("-------------------------------------");

            // 5. Lấy danh sách tất cả các thẻ <employee>
            NodeList employeeList = root.getElementsByTagName("employee");

            // 6. Duyệt qua từng Node trong danh sách employeeList
            for (int i = 0; i < employeeList.getLength(); i++) {
                Node employeeNode = employeeList.item(i);

                // Đảm bảo rằng Node là một Element (thẻ XML)
                if (employeeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element employeeElement = (Element) employeeNode;

                    // Lấy thuộc tính 'id'
                    String id = employeeElement.getAttribute("id");
                    System.out.println("Nhân viên ID: " + id);

                    // Lấy thẻ <name> bên trong <employee>
                    String name = getTagValue("name", employeeElement);
                    System.out.println("  Tên: " + name);

                    // --- Xử lý thẻ <contact> ---
                    NodeList contactList = employeeElement.getElementsByTagName("contact");
                    if (contactList.getLength() > 0) {
                        Element contactElement = (Element) contactList.item(0); // Chỉ lấy thẻ contact đầu tiên nếu có nhiều
                        String email = getTagValue("email", contactElement);
                        String phone = getTagValue("phone", contactElement);
                        System.out.println("  Liên lạc:");
                        System.out.println("    Email: " + email);
                        System.out.println("    Điện thoại: " + phone);
                    } else {
                        System.out.println("  Liên lạc: (Không có thông tin)");
                    }

                    // --- Xử lý thẻ <department> ---
                    NodeList departmentList = employeeElement.getElementsByTagName("department");
                    if (departmentList.getLength() > 0) {
                        Element departmentElement = (Element) departmentList.item(0);
                        String deptName = getTagValue("name", departmentElement);
                        String location = getTagValue("location", departmentElement);
                        System.out.println("  Phòng ban:");
                        System.out.println("    Tên phòng: " + deptName);
                        System.out.println("    Địa điểm: " + location);
                    } else {
                        System.out.println("  Phòng ban: (Không có thông tin)");
                    }

                    System.out.println("-------------------------------------"); // Ngăn cách giữa các nhân viên
                }
            }

        } catch (ParserConfigurationException e) {
            System.err.println("Lỗi cấu hình trình phân tích XML: " + e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("Lỗi khi phân tích cú pháp XML: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Lỗi đọc file XML: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Đã xảy ra lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hàm tiện ích để lấy nội dung text của một thẻ con bên trong một thẻ cha.
     * Xử lý trường hợp thẻ con không tồn tại hoặc không có nội dung.
     *
     * @param tagName Tên của thẻ con cần lấy giá trị.
     * @param parentElement Thẻ cha chứa thẻ con đó.
     * @return Nội dung text của thẻ con, hoặc chuỗi rỗng nếu không tìm thấy/không có nội dung.
     */
    private static String getTagValue(String tagName, Element parentElement) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0); // Lấy phần tử đầu tiên tìm thấy
            if (node != null && node.getFirstChild() != null) {
                // Node.getTextContent() lấy toàn bộ text, kể cả của thẻ con cháu
                // Node.getFirstChild().getNodeValue() chỉ lấy text trực tiếp của node này
                return node.getTextContent().trim();
            }
        }
        return "N/A"; // Trả về "N/A" nếu thẻ không tồn tại hoặc trống
    }
}