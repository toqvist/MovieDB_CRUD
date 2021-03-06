package com.example.objectLists;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.example.beans.MovieBean;
import com.example.helpers.jsonHelper;

public class Movie {
	private Connection connection;
	private ArrayList<MovieBean> movies;
	
    private String query_createMovie = "CALL sp_add_movie(?,?,?,?,?, ?);";
    private String query_selectMovie = "SELECT * FROM movie;";
    private String query_updateScore = "UPDATE movie SET score = ? WHERE movie.title = ?;";
    private String query_deleteMovie = "DELETE FROM movie WHERE movie.title = ?;";


	public Movie(Connection connection) {
		this.connection = connection;
		this.movies = new ArrayList<MovieBean>();
		getMovies();
	}

	public void addMovie(String title, int runtime, float score, String rating, String director_fname, String director_lname) {

		try (PreparedStatement sqlQuery = this.connection.prepareStatement(query_createMovie)) {
			sqlQuery.setString(1, title);
			sqlQuery.setInt(2, runtime);
			sqlQuery.setFloat(3, score);
            sqlQuery.setString(4, rating);
            sqlQuery.setString(5, director_fname);
            sqlQuery.setString(6, director_lname);
			
			System.out.println(sqlQuery);

			sqlQuery.executeUpdate();

		} catch (SQLException e) {
			System.out.println("addMovie exception");
			e.printStackTrace();
		}

		String result = "Movie added: " + title;
		System.out.println(result);
	}

	public ArrayList<MovieBean> getMovies() {
		if (this.movies.size() > 0) {
			return this.movies;
		}

		this.movies = new ArrayList<MovieBean>();
		try (PreparedStatement sqlQuery = this.connection.prepareStatement(query_selectMovie)) {
			runQuery(sqlQuery);
		} catch (SQLException e) {
			System.out.println("getmMovies exception");			e.printStackTrace();
		}

		return this.movies;
	}

	public void setScore(float newScore, String title) {

		try (PreparedStatement sqlQuery = this.connection.prepareStatement(query_updateScore)) {
            sqlQuery.setFloat(1, newScore);
            sqlQuery.setString(2, title);
			
			System.out.println(sqlQuery);

			sqlQuery.executeUpdate();
		} catch (SQLException e) {
			System.out.println("setScore exception");
			e.printStackTrace();
		}

		String result = "Set new score: " + newScore + " for " + title;
		System.out.println(result);
	}

	public void deleteMovie(String title) {

		try (PreparedStatement sqlQuery = this.connection.prepareStatement(query_deleteMovie)) {
			sqlQuery.setString(1, title);
			
			System.out.println(sqlQuery);

			sqlQuery.executeUpdate();

		} catch (SQLException e) {
			System.out.println("deleteMovie exception");
			e.printStackTrace();
		}

		String result = "Movie deleted: " + title;
		System.out.println(result);
	}

	public String getBeansAsJSON() {
		String beansContent = "";
		for (MovieBean movieBean : this.movies) {
			beansContent += movieBean.toJson() + ",";
		}
		
		String result = "{" + jsonHelper.toJsonArray("Movie", beansContent) + "}";
		return result;

	}


	private MovieBean buildMovieBean (ResultSet resultSet) {
		MovieBean movieBean = new MovieBean();

		try {
			movieBean.setId(resultSet.getInt("movie_id"));
			movieBean.setTitle(resultSet.getString("title"));
			movieBean.setRuntime(resultSet.getInt("runtime"));
			movieBean.setScore(resultSet.getFloat("score"));
            movieBean.setRating(resultSet.getString("rating"));
            movieBean.setDirectorId(resultSet.getInt("director_id"));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return movieBean;
	}

	private void buildMovieBeans(ResultSet resultSet) throws SQLException {
		while (resultSet.next()) { // rows
			this.movies.add(buildMovieBean(resultSet));
		}
	}

	private void runQuery(PreparedStatement query) {
		try (ResultSet resultSet = query.executeQuery()) {
			buildMovieBeans(resultSet);
		} catch (SQLException e) {
			System.out.println("getMovie exception for result set");
			e.printStackTrace();
		}
	}

	public MovieBean findMovieBean(String movieTitle) {
		for(MovieBean MovieBean : this.movies) {
			if (MovieBean.getTitle().equals(movieTitle)) {
				return MovieBean;
			}
		}
		return null;
	}

	
}
