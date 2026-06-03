import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class Main {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {

        // ===== FRAME =====
        JFrame frame = new JFrame("Criminal Face Detection System");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // ===== MAIN PANEL =====
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(30, 30, 45));
        frame.add(panel);

        // ===== TITLE =====
        JLabel title = new JLabel("CRIMINAL FACE DETECTION");
        title.setBounds(60, 20, 400, 40);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(title);

        // ===== BUTTON STYLE FUNCTION =====
        Font btnFont = new Font("Arial", Font.BOLD, 14);

        JButton addBtn = new JButton("Add Criminal");
        addBtn.setBounds(150, 90, 200, 35);
        styleButton(addBtn, btnFont);
        panel.add(addBtn);

        JButton detectBtn = new JButton("Detect Face");
        detectBtn.setBounds(150, 140, 200, 35);
        styleButton(detectBtn, btnFont);
        panel.add(detectBtn);

        JButton viewBtn = new JButton("View Criminals");
        viewBtn.setBounds(150, 190, 200, 35);
        styleButton(viewBtn, btnFont);
        panel.add(viewBtn);

        JButton exitBtn = new JButton("Exit");
        exitBtn.setBounds(150, 240, 200, 35);
        styleButton(exitBtn, btnFont);
        exitBtn.setBackground(new Color(200,50,50));
        panel.add(exitBtn);

        frame.setVisible(true);

        // ===== DATABASE =====
        String url = "jdbc:mysql://localhost:3306/Criminaldb";
        String user = "root";
        String pass = "chotadon";

        // ===== ADD CRIMINAL =====
        addBtn.addActionListener(e -> {
            try (Connection con = DriverManager.getConnection(url, user, pass)) {

                String name = JOptionPane.showInputDialog(frame,"Enter Name");
                String crime = JOptionPane.showInputDialog(frame,"Enter Crime");

                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select Criminal Photo");

                if(fc.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION){

                    File file = fc.getSelectedFile();
                    String destPath = "images/" + file.getName();
                    file.renameTo(new File(destPath));

                    String sql = "INSERT INTO criminals (name,crime,photo_path) VALUES (?,?,?)";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1,name);
                    ps.setString(2,crime);
                    ps.setString(3,destPath);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(frame,"Criminal Added Successfully!");
                }

            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // ===== VIEW =====
        viewBtn.addActionListener(e -> {
            try (Connection con = DriverManager.getConnection(url, user, pass)) {

                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM criminals");

                StringBuilder sb = new StringBuilder();

                while(rs.next()){
                    sb.append(rs.getInt("id")).append(" - ")
                      .append(rs.getString("name")).append(" - ")
                      .append(rs.getString("crime")).append("\n");
                }

                JOptionPane.showMessageDialog(frame,sb.toString());

            } catch(Exception ex){ ex.printStackTrace(); }
        });

        // ===== EXIT =====
        exitBtn.addActionListener(e -> System.exit(0));

        // ===== DETECT FACE =====
        detectBtn.addActionListener(e -> {

            new Thread(() -> {

                VideoCapture camera = new VideoCapture(0);

                if(!camera.isOpened()){
                    JOptionPane.showMessageDialog(frame,"Camera not opened!");
                    return;
                }

                CascadeClassifier faceDetector =
                        new CascadeClassifier("haarcascade_frontalface_alt.xml");

                Mat frameMat = new Mat();
                MatOfRect faces = new MatOfRect();

                HighGui.namedWindow("Face Detection");

                while(true){

                    camera.read(frameMat);
                    if(frameMat.empty()) continue;

                    faceDetector.detectMultiScale(frameMat, faces);

                    for(Rect rect : faces.toArray()){
                        Imgproc.rectangle(frameMat,
                                rect.tl(), rect.br(),
                                new Scalar(0,255,0),3);
                    }

                    HighGui.imshow("Face Detection", frameMat);

                    if(HighGui.waitKey(30)==27) break;
                }

                camera.release();
                HighGui.destroyAllWindows();

            }).start();
        });
    }

    // ===== BUTTON DESIGN =====
    private static void styleButton(JButton btn, Font font){
        btn.setFont(font);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(70,130,180));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder());
    }
}