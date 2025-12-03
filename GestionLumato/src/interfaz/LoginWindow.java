package interfaz;



import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import modelo.Empresa;
import modelo.Usuario;

public class LoginWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    
    // Referencia al sistema
    private Empresa empresa;

    // Constructor que recibe la empresa
    public LoginWindow(Empresa empresa) {
        this.empresa = empresa;
        
        setTitle("Acceso al Sistema");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300); // Ventana más chica para login
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        setLocationRelativeTo(null); // Centrar en pantalla

        JLabel lblTitulo = new JLabel("INICIAR SESIÓN");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblTitulo.setBounds(10, 20, 414, 30);
        contentPane.add(lblTitulo);
        
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setBounds(50, 70, 80, 20);
        contentPane.add(lblUser);
        
        txtUsuario = new JTextField();
        txtUsuario.setBounds(140, 70, 200, 20);
        contentPane.add(txtUsuario);
        
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setBounds(50, 110, 80, 20);
        contentPane.add(lblPass);
        
        txtPassword = new JPasswordField();
        txtPassword.setBounds(140, 110, 200, 20);
        contentPane.add(txtPassword);
        
        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autenticar();
            }
        });
        btnIngresar.setBounds(140, 160, 120, 30);
        contentPane.add(btnIngresar);
    }
    
    private void autenticar() {
        String user = txtUsuario.getText();
        String pass = new String(txtPassword.getPassword());
        
        Usuario u = empresa.login(user, pass);
        
        if (u != null) {
            // 1. Cerrar esta ventana (Login)
            this.dispose();
            
            // 2. Abrir la ventana principal (MainForm)
            // Le pasamos la empresa Y el usuario que acaba de entrar
            MainForm principal = new MainForm(empresa, u); 
            principal.setVisible(true);
            principal.setLocationRelativeTo(null);
            
        } else {
            JOptionPane.showMessageDialog(this, "Datos incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}