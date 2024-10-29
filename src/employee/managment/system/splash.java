package employee.managment.system;

import javax.swing.*;
import java.awt.*;


public class splash extends JFrame {

    splash(){

        JLabel author = new JLabel("nishant.tarun");
        author.setBounds(1000,540,150,30);
        author.setFont(new Font("SAN_SERIF", Font.BOLD,15));
        author.setForeground(Color.WHITE);
        add(author);

        JLabel version = new JLabel("version: 1.3.7");
        version.setBounds(1000,560,150,30);
        version.setFont(new Font("SAN_SERIF", Font.BOLD,15));
        version.setForeground(Color.WHITE);
        add(version);

        ImageIcon i1x = new ImageIcon(ClassLoader.getSystemResource("icons/anim.gif"));
        Image i2x= i1x.getImage();
        ImageIcon i3x=new ImageIcon(i2x);
        JLabel imgx=new JLabel(i3x);
        imgx.setBounds(550,450,102,104);
        add(imgx);


        ImageIcon i1= new ImageIcon(ClassLoader.getSystemResource("icons/front.jpeg"));
        Image i2 = i1.getImage().getScaledInstance(1170,650, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel image =new JLabel(i3);
        image.setBounds(0,0,1170,650);
        add(image);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1170,650);
        setLocationRelativeTo(null);
        setLayout(null);
        setVisible(true);

        try{
            Thread.sleep(5000);
            setVisible(false );
            new login();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new splash();
    }

}
