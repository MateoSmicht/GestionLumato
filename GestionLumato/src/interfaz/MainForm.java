package interfaz;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import modelo.Empresa;
import modelo.Usuario;

public class MainForm extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private Empresa empresa;
    private Usuario usuarioLogueado;

    // Constructor modificado: Recibe también al Usuario ya logueado
    public MainForm(Empresa empresa, Usuario usuario) {
        this.empresa = empresa;
        this.usuarioLogueado = usuario;
        
        setTitle("Sistema: " + empresa.getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        JLabel lblBienvenida = new JLabel("Hola, " + usuario.getNombreCompleto());
        lblBienvenida.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblBienvenida.setBounds(20, 20, 400, 30);
        contentPane.add(lblBienvenida);
        
        JLabel lblRol = new JLabel("Rol: " + usuario.getNombreRol());
        lblRol.setBounds(20, 50, 300, 20);
        contentPane.add(lblRol);
        
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });
        btnCerrarSesion.setBounds(650, 20, 120, 30);
        contentPane.add(btnCerrarSesion);
        
        // Aquí agregarás los botones del menú (Ventas, Stock, etc.)
    }
    
    private void cerrarSesion() {
        this.dispose(); // Cierra el menú principal
        
        // Vuelve a abrir el Login
        LoginWindow login = new LoginWindow(empresa);
        login.setVisible(true);
    }
}