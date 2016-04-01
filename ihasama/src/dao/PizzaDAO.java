package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import luokat.Pizza;
import luokat.Tayte;



public class PizzaDAO {

	// Tietokanta-ajurin lataus siirretty constructoriin

	public PizzaDAO() {

		try {
			Class.forName("org.mariadb.jdbc.Driver");
		} catch (java.lang.ClassNotFoundException ex) {
			System.out.print("ClassNotFoundException: ");
			System.out.println(ex.getMessage());
		}
	}

	public ArrayList<Pizza> pizzalista = new ArrayList<Pizza>();
	public ArrayList<Tayte> taytelista = new ArrayList<Tayte>();
	Connection yhteys = null;

	public void avaaYhteys() {
		// K�ytt�j�tiedot ja DB:n osoite - lis�� puuttuvat tiedot!
		String username = "a1500925";
		String password = "syKA4t68r";
		String url = "jdbc:mariadb://localhost/a1500925";

		try {
			// Yhdistet��n tietokantaan
			yhteys = DriverManager.getConnection(url, username, password);
		} catch (SQLException ex) {
			System.out.println("Virhe yhteyden avaamisessa");
		}

	}

	public void suljeYhteys() {
		try {
			if (yhteys != null && !yhteys.isClosed())
				yhteys.close();
		} catch (Exception e) {
			System.out.println("Tietokantayhteys ei jostain syystä suostu menemään kiinni.");
			e.printStackTrace();
		}
	}

	public List<Pizza> haePizzat() throws NumberFormatException, SQLException {
		String sql = "SELECT p.pizzaid, p.pizzanimi, p.hinta, GROUP_CONCAT(t.tayteid) as tayteidt, GROUP_CONCAT(t.taytenimi) as taytenimet FROM Pizza p LEFT JOIN Pizzantaytteet pt ON p.pizzaid = pt.pizzaid LEFT JOIN Tayte t ON t.tayteid = pt.tayteid GROUP BY p.pizzanimi order by pizzanimi, null";
		Statement haku = yhteys.createStatement();
		ResultSet tulokset = haku.executeQuery(sql);

		while (tulokset.next()) {
			int id = tulokset.getInt("pizzaid");
			String nimi = tulokset.getString("pizzanimi");
			double hinta = tulokset.getDouble("hinta");
			String taytteet = tulokset.getString("taytenimet");
			// lis�t��n pizza listaan
			Pizza pizza = new Pizza(id, nimi, hinta, taytteet);
			pizzalista.add(pizza);
		}

		return pizzalista;

	}
	
	public List<Tayte> haeTaytteet() throws NumberFormatException, SQLException {
		String sql = "SELECT * FROM Tayte ORDER BY taytenimi";
		Statement haku = yhteys.createStatement();
		ResultSet tulokset = haku.executeQuery(sql);

		while (tulokset.next()) {
			int tayteid = tulokset.getInt("tayteid");
			String taytenimi = tulokset.getString("taytenimi");
			boolean saatavilla = tulokset.getBoolean("saatavilla");
			// lis�t��n pizza listaan
			Tayte tayte = new Tayte(tayteid, taytenimi, saatavilla);
			taytelista.add(tayte);
		}

		return taytelista;

	}

	public void lisaaPizza(Pizza p) {

		try {
			// alustetaan sql-lause
			String sql = "INSERT INTO Pizza(pizzanimi, hinta) VALUES(?,?)";
			PreparedStatement lause = yhteys.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS);

			// t�ydennet��n puuttuvat tiedot (eli pizzan nimi ja hinta)
			lause.setString(1, p.getPizzanimi());
			lause.setDouble(2, p.getHinta());

			// suoritetaan lause
			lause.executeUpdate();

			int vaikutetutrowit = lause.executeUpdate();
			
			if (vaikutetutrowit == 0){
				throw new SQLException("Pizzan luominen ep�onnistui, mihink��n rowiin ei tullut mit��n");
			}
			try (ResultSet generatedKeys = lause.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                p.setPizzaid(generatedKeys.getInt(1));
	            }
	            else {
	                throw new SQLException("Pizzan luominen ep�onnistui, ei saatu ID:t�");
	            }
			
			}
			System.out.println("Pizza" + p + "lis�tty tietokantaan");
		} catch (Exception e) {
			// Tapahtui jokin virhe
			System.out
					.println("Pizzan lis��misyritys aiheutti virheen pizzanlisaysvaiheessa!");
		}
		
	}

	public void lisaaPizzantaytteet(Pizza a, String[] taytteidenIdt) {
		// T�� TEHD��N LOPPUUN KUN SAADAN ARRAYLISTIT TOIMIMAAN
		// heheh

		try {
			// alustetaan sql-lause
			String sql = "INSERT INTO Pizzantaytteet(pizzaid, tayteid) VALUES(?,?)";
			PreparedStatement lause = yhteys.prepareStatement(sql);

			for (String s : taytteidenIdt) {

				System.out.println("toimii for-loopin alussa");
				// t�ydennet��n puuttuvat tiedot (eli pizzan nimi ja hinta)
				lause.setInt(1, a.getPizzaid());
				lause.setInt(2, Integer.parseInt(s));
				System.out.println("toimi kun lis�ttiin lauseeseen juttuja");
				// suoritetaan lause
				lause.executeUpdate();
				System.out.println("Toimi kun teloitettiin update");
			}
			System.out.println("Pizza" + a + "lis�tty tietokantaan");
		} catch (Exception e) {
			// Tapahtui jokin virhe
			System.out
					.println("Pizzan lis��misyritys aiheutti virheen pizzantaytevaiheessa!");
		}
	}

}
