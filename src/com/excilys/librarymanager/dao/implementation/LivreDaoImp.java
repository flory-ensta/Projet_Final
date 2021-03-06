package com.excilys.librarymanager.dao.implementation;

import java.util.List;
import java.util.ArrayList;

import java.sql.*;

import com.excilys.librarymanager.persistence.ConnectionManager;

import com.excilys.librarymanager.dao.*;
import com.excilys.librarymanager.exception.DaoException;
import com.excilys.librarymanager.model.*;

public class LivreDaoImp implements LivreDao {

	private static LivreDaoImp instance;
	private LivreDaoImp() { }	
	public static LivreDaoImp getInstance() {
		if(instance == null) {
			instance = new LivreDaoImp();
		}
		return instance;
	}

	private static final String SELECT_ALL_QUERY = "SELECT * FROM livre;";
	private static final String SELECT_ONE_QUERY = "SELECT * FROM livre WHERE id= ? ;";
	private static final String CREATE_QUERY = "INSERT INTO livre (titre, auteur, isbn) VALUES (?, ?, ?);";
	private static final String UPDATE_QUERY = "UPDATE livre SET titre=?, auteur=?, isbn=? WHERE id=?;";
	private static final String DELETE_QUERY = "DELETE FROM livre WHERE id=?;";
	private static final String COUNT_QUERY = "SELECT COUNT(id) AS count FROM livre";

	@Override
	public List<Livre> getList() throws DaoException {
		List<Livre> livres = new ArrayList<>();

		try (
				Connection connection = ConnectionManager.getConnection();
				PreparedStatement statement = connection.prepareStatement(SELECT_ALL_QUERY);
				ResultSet res = statement.executeQuery();
				){ 
			while(res.next() ) {
				Livre l = new Livre(res.getInt("id"), res.getString("titre"), res.getString("auteur"), res.getString("isbn"));
				livres.add(l);
			}
			System.out.println("GET: " + livres);
		} catch (SQLException e) {
			throw new DaoException("Probleme lors de la recuperation des livres.");
		}
		return livres;

	}

	@Override
	public Livre getById(int id) throws DaoException {
		Livre livre = new Livre();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet res = null;

		try {
			connection = ConnectionManager.getConnection();
			statement = connection.prepareStatement(SELECT_ONE_QUERY);
			statement.setInt(1, id);
			res = statement.executeQuery();
			if (res.next()) {
				livre.setId(res.getInt("id"));
				livre.setauteur(res.getString("auteur"));
				livre.settitre(res.getString("titre"));
				livre.setIsbn(res.getString("isbn"));
			}
			System.out.println("GET: " + livre);
		} catch (SQLException e){
			throw new DaoException("Probleme dans la recuperation du livre");
		} finally {
			try {
				res.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return livre;
	}

	@Override
	public int create(String titre, String auteur, String isbn) throws DaoException {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		int id = -1;

		try {
			connection = ConnectionManager.getConnection();
			statement = connection.prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, titre);
			statement.setString(2, auteur);
			statement.setString(3, isbn);
			statement.executeUpdate();
			res = statement.getGeneratedKeys();
			if (res.next()) {
				id = res.getInt(1);
			}
			Livre livre = new Livre(id, titre, auteur, isbn);
			System.out.println("CREATE: " + livre);
		} catch (SQLException e){
			throw new DaoException("Probleme dans la creation du livre");
		} finally {
			try {
				res.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return id;
	};

	@Override
	public void update(Livre livre) throws DaoException {
		Connection connection = null;
		PreparedStatement statement = null;	
		try {
			connection = ConnectionManager.getConnection();
			statement = connection.prepareStatement(UPDATE_QUERY);
			statement.setString(1, livre.gettitre());
			statement.setString(2, livre.getauteur());
			statement.setString(3, livre.getIsbn());
			statement.setInt(4, livre.getId());
			statement.executeUpdate();
			System.out.println("UPDATE: " + livre);
		} catch (SQLException e){
			throw new DaoException("Probleme dans la mise a jour du livre");
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void delete(int id) throws DaoException {
		Connection connection = null;
		PreparedStatement statement = null;	
		try {
			Livre livre = new Livre();
			livre = getById(id);
			connection = ConnectionManager.getConnection();
			statement = connection.prepareStatement(DELETE_QUERY);
			statement.setInt(1, livre.getId());
			statement.executeUpdate();
			statement.close();
			System.out.println("DELETE: " + livre);
		} catch (SQLException e){
			throw new DaoException("Probleme dans la suppression du livre");
		} finally {
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int count() throws DaoException {
		int nbLivres = 0;
		try (
				Connection connection = ConnectionManager.getConnection();
				PreparedStatement statement = connection.prepareStatement(COUNT_QUERY);
				ResultSet res = statement.executeQuery();
				){ 
			while(res.next() ) {
				nbLivres = res.getInt("count");
			}
			System.out.println("COUNT: " + nbLivres);
		} catch (SQLException e) {
			throw new DaoException("Probleme lors du comptage des livres.");
		}
		return nbLivres;
	};

	public static void main(String[] args) {
		LivreDaoImp daoImp = getInstance();
		try {		
			int id;
			String titre = "Martine va a la plage";
			String auteur = "moi";
			String isbn ="ojhvogci1554";
			daoImp.getList();
			id = daoImp.create(titre, auteur, isbn);
			daoImp.getById(id);
			Livre livre = new Livre(id, titre, "un autre", isbn);
			daoImp.update(livre);
			daoImp.delete(id);
			daoImp.count();
		} catch (DaoException e) {
			System.out.println("c'est la merde");
		}
	}	
}
