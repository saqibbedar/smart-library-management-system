import UI.LoginUI;

public class Main {
    public static void main(String[] args) {
        String dbPath = "./SLMS-DB.accdb";
        new LoginUI(dbPath).setVisible(true);
    }
}
